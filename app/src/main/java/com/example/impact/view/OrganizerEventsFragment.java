package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.impact.utils.role.OrganizerDb;
import com.example.impact.view.adapter.EventAdapter;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Fragment displaying all events created by the logged-in organizer. Real-time data is streamed
 * through {@link OrganizerDb#listenToEventsByEmail(String, com.google.firebase.firestore.EventListener)}
 * so no direct Firestore paths are constructed inside the UI layer.
 */
public class OrganizerEventsFragment extends Fragment implements EventAdapter.OnEventClickListener {

    private final EventController controller = new EventController();
    private EventAdapter adapter;
    private String organizerEmail = "";
    private ListenerRegistration reg;

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
        Button btnCreate = v.findViewById(R.id.btnCreateNewEvent);
        btnCreate.setOnClickListener(view -> {
            if (requireActivity() instanceof OrganizerActivity) {
                ((OrganizerActivity) requireActivity()).goToCreateTab();
            }
        });

        // --- RecyclerView Setup ---
        RecyclerView rv = v.findViewById(R.id.recyclerEvents);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getArguments() != null) {
            organizerEmail = getArguments().getString("organizerEmail");
        }
        if (TextUtils.isEmpty(organizerEmail)) {
            organizerEmail = AppSession.getUser().getEmail();
        }

        if (TextUtils.isEmpty(organizerEmail)) {
            Toast.makeText(requireContext(), "Organizer email missing", Toast.LENGTH_SHORT).show();
            return v;
        }

        adapter = new EventAdapter(this, Organizer.ROLE_KEY);
        rv.setAdapter(adapter);

        reg = OrganizerDb.listenToEventsByEmail(organizerEmail,
                (snap, err) -> {
                    if (err != null || snap == null) return;
                    List<Event> events = controller.mapEvents(snap);
                    adapter.setEvents(events);
                });

        return v;
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
        Intent intent = new Intent(requireContext(), WaitingListActivity.class);
        intent.putExtra("eventId", event.getId());
        startActivity(intent);
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
