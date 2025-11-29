package com.example.impact.view;

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
import com.example.impact.model.User;
import com.example.impact.utils.AppSession;
import com.example.impact.view.adapter.EventAdapter;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Fragment displaying all events created by the logged-in organizer.
 * Provides a button to create new events and allows viewing entrants for each event.
 */
public class OrganizerEventListFragment extends Fragment implements EventAdapter.OnEventClickListener {

    private final EventController controller = new EventController();
    private EventAdapter adapter;
    private String organizerId;
    private ListenerRegistration reg;

    public static final String EXTRA_ORGANIZER_ID = "organizer_id";

    // Use a static factory method to create the fragment and set arguments
    public static OrganizerEventListFragment newInstance(String organizerId) {
        OrganizerEventListFragment fragment = new OrganizerEventListFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ORGANIZER_ID, organizerId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the organizer events list and wires up real-time listeners.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            organizerId = getArguments().getString(EXTRA_ORGANIZER_ID);
        }

        // Fallback/Session check logic should also be here
        if (TextUtils.isEmpty(organizerId)) {
            User currentUser = AppSession.getUser();
            if (currentUser != null) {
                organizerId = currentUser.getEmail();
            }
        }

        if (organizerId == null) {
            Toast.makeText(requireContext(), "Organizer ID missing", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        System.out.println(organizerId);
        adapter = new EventAdapter(this, Organizer.ROLE_KEY);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_organizer_events, container, false);

        RecyclerView rv = v.findViewById(R.id.recyclerEvents);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        return v;
    }

    /**
     * Loads events and sets ListenerRegistration
     */
    private void loadEvents() {
        FirebaseFirestore db = AppSession.db();

        // Step 1: verify that this email belongs to an organizer
        db.collection("users")
                .whereEqualTo(FieldPath.documentId(), organizerId)
                .whereEqualTo("role", "organizer")
                .limit(1)
                .get()
                .addOnSuccessListener(users -> {
                    if (!users.isEmpty()) {
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
    }

    @Override
    public void onStart() {
        super.onStart();
        if (reg == null && organizerId != null) {
            loadEvents();
        }
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
        WaitingListFragment fragment = WaitingListFragment.newInstance(event);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.dashboard_fragment_container, fragment)
                .addToBackStack(null) // This is crucial for back button support
                .commit();
    }

    /**
     * Also routes to waiting-list management when the entrants button is pressed.
     */
    @Override
    public void onViewEntrantsClicked(@NonNull Event event) {
        WaitingListFragment fragment = WaitingListFragment.newInstance(event);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.dashboard_fragment_container, fragment)
                .addToBackStack(null) // This is crucial for back button support
                .commit();
    }
}
