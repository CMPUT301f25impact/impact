package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.EventController;
import com.example.impact.model.Event;
import com.example.impact.model.Organizer;
import com.example.impact.utils.AppSession;
import com.example.impact.view.adapter.EventAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.example.impact.controller.ImageController;

import java.util.List;

/**
 * Fragment displaying all events created by the logged-in organizer.
 * Provides a button to create new events and allows viewing entrants for each event.
 */
public class OrganizerEventListFragment extends Fragment implements EventAdapter.OnEventClickListener {

    private final EventController controller = new EventController();
    private final ImageController imageController = new ImageController();
    private EventAdapter adapter;
    private String organizerId = "";
    private ListenerRegistration reg;
    private com.example.impact.model.Event eventBeingUpdatedPoster;
    private final androidx.activity.result.ActivityResultLauncher<String> posterPickerLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                    this::onPosterPicked
            );
    public static final String EXTRA_ORGANIZER_ID = "organizer_id";

    // Use a static factory method to create the fragment and set arguments
    public static OrganizerEventListFragment newInstance(String organizerId) {
        OrganizerEventListFragment fragment = new OrganizerEventListFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ORGANIZER_ID, organizerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        // No need to call reg here — listener starts in onCreateView
    }

    /**
     * Inflates the organizer events list and wires up real-time listeners.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_organizer_events, container, false);

        // --- Create Event Button ---
//        Button btnCreate = v.findViewById(R.id.btnCreateNewEvent);
//        btnCreate.setOnClickListener(view -> {
//            if (requireActivity() instanceof OrganizerActivity) {
//                ((OrganizerActivity) requireActivity()).goToCreateTab();
//            }
//        });

        // --- RecyclerView Setup ---
        RecyclerView rv = v.findViewById(R.id.recyclerEvents);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getArguments() != null) {
            organizerId = getArguments().getString(EXTRA_ORGANIZER_ID);
        }
        if (TextUtils.isEmpty(organizerId)) {
            organizerId = AppSession.getUserId();
        }

        if (TextUtils.isEmpty(organizerId)) {
            Toast.makeText(requireContext(), "Organizer id missing", Toast.LENGTH_SHORT).show();
            return v;
        }

        adapter = new EventAdapter(this, Organizer.ROLE_KEY);
        rv.setAdapter(adapter);

        FirebaseFirestore db = AppSession.db();

        // Step 1: verify that this id belongs to an organizer
        db.collection("users")
                .document(organizerId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists() && Organizer.ROLE_KEY.equals(userDoc.getString("role"))) {
                        // Step 2: load events for this organizer
                        reg = db.collection("events")
                                .whereEqualTo("organizerId", organizerId)
                                .addSnapshotListener((snap, err) -> {
                                    if (err != null || snap == null) return;
                                    List<Event> events = controller.mapEvents(snap);
                                    adapter.setEvents(events);
                                });
                    } else {
                        Toast.makeText(requireContext(), "Not an organizer account", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error verifying organizer", Toast.LENGTH_SHORT).show());

        return v;
    }

    /**
     * Called when the organizer picks a new poster image from the gallery.
     */
    private void onPosterPicked(@Nullable android.net.Uri uri) {
        if (uri == null) {
            eventBeingUpdatedPoster = null;
            return;
        }
        if (eventBeingUpdatedPoster == null) {
            Toast.makeText(requireContext(), "No event selected for poster update", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Decode bitmap from URI
            java.io.InputStream is = requireContext().getContentResolver().openInputStream(uri);
            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(is);
            if (bmp == null) {
                Toast.makeText(requireContext(), "Unable to read image", Toast.LENGTH_SHORT).show();
                eventBeingUpdatedPoster = null;
                return;
            }

            // Convert to base64
            String base64 = com.example.impact.utils.ImageUtil.bitmapToBase64(bmp);

            // Infer filename + mime type
            String fileName = queryFileName(uri);
            String mime = requireContext().getContentResolver().getType(uri);
            if (mime == null) mime = "image/jpeg";

            com.example.impact.model.Image imageModel = new com.example.impact.model.Image();
            imageModel.setFileName(fileName != null ? fileName : "poster.jpg");
            imageModel.setMimeType(mime);
            imageModel.setBase64Content(base64);

            Toast.makeText(requireContext(), "Uploading new poster…", Toast.LENGTH_SHORT).show();

            // 1) Upload image to "images" collection
            imageController.createImage(imageModel, imageId -> {
                // 2) Update event.posterUrl to point at the new imageId
                controller.updatePosterUrl(
                        eventBeingUpdatedPoster.getId(),
                        imageId,
                        v -> {
                            Toast.makeText(requireContext(), "Poster updated", Toast.LENGTH_SHORT).show();
                            eventBeingUpdatedPoster = null;
                            // No need to manually refresh list: the snapshot listener will fire
                        },
                        err -> {
                            Toast.makeText(
                                    requireContext(),
                                    "Poster saved, but event update failed: " +
                                            (err != null ? err.getMessage() : "unknown"),
                                    Toast.LENGTH_SHORT
                            ).show();
                            eventBeingUpdatedPoster = null;
                        }
                );
            }, err -> {
                Toast.makeText(
                        requireContext(),
                        "Poster upload failed: " + (err != null ? err.getMessage() : "unknown"),
                        Toast.LENGTH_SHORT
                ).show();
                eventBeingUpdatedPoster = null;
            });

        } catch (Exception ex) {
            Toast.makeText(requireContext(), "Failed to read image: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            eventBeingUpdatedPoster = null;
        }
    }

    /**
     * Helper to get a sensible file name from a content Uri.
     */
    @Nullable
    private String queryFileName(@NonNull android.net.Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = requireContext().getContentResolver()
                    .query(uri, new String[]{android.provider.OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(0);
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            String path = uri.getPath();
            if (path == null) return null;
            int cut = path.lastIndexOf('/');
            if (cut != -1) result = path.substring(cut + 1);
        }
        return result;
    }


    /**
     * Stops the snapshot listener when leaving the screen.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    /**
     * Opens the waiting list view when an event row itself is tapped.
     */
    @Override
    public void onEventClicked(@NonNull Event event) {
        eventBeingUpdatedPoster = event;
        Toast.makeText(requireContext(), "Choose a new poster image…", Toast.LENGTH_SHORT).show();
        posterPickerLauncher.launch("image/*");
    }

    /**
     * Also routes to waiting-list management when the entrants button is pressed.
     */
    @Override
    public void onViewEntrantsClicked(@NonNull Event event) {
        Intent intent = new Intent(requireContext(), WaitingListActivity.class);
        intent.putExtra("eventId", event.getId());
        startActivity(intent);
    }
}
