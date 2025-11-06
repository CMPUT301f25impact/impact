package com.example.impact.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.EventController;
import com.example.impact.model.Event;
import com.example.impact.utils.DeletionConfirmationUtil;
import com.example.impact.view.adapter.AdminEventAdapter;

import java.util.List;

/**
 * This is the list fragment that renders lists of items in the admin dashboard.
 * It can be of multiple types (e.g profiles, images, events)
 */
public class AdminEventListFragment extends Fragment
        implements AdminEventAdapter.DeleteListener {

    private AdminEventAdapter currentAdapter;

    private EventController eventController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventController = new EventController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.admin_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        currentAdapter = new AdminEventAdapter(this);
        recyclerView.setAdapter(currentAdapter);

        loadEvents();
        return view;
    }

    /**
     * Loads events using EventController
     */
    private void loadEvents() {
        eventController.fetchAvailableEvents(this::onEventsLoaded,
                error -> Toast.makeText(getContext(), R.string.event_list_error_loading, Toast.LENGTH_SHORT).show());
    }

    /**
     * Callback when events are successfully loaded
     * @param events loaded events
     */
    private void onEventsLoaded(List<Event> events) {
        currentAdapter.setEvents(events);
    }

    /**
     * Callback when an event is successfully deleted
     */
    private void onEventDelete(String name) {
        String deleteText = getResources().getString(R.string.admin_event_list_deletion, name);
        Toast.makeText(getContext(), deleteText, Toast.LENGTH_SHORT).show();
        loadEvents(); // reload events after deletion
    }

    @Override
    public void onDeleteEventClicked(int position, Event event) {
        String eventId = event.getId();
        String eventName = event.getName();

        DeletionConfirmationUtil confirmation = new DeletionConfirmationUtil(getContext(), event.getName(),
                () -> {
                    eventController.deleteEvent(eventId, v -> onEventDelete(eventName),
                            error -> Toast.makeText(getContext(), R.string.admin_event_list_error_deletion, Toast.LENGTH_SHORT).show());
                });

        confirmation.show();
    }
}
