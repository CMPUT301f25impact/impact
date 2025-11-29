package com.example.impact.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.impact.R;

/**
 * Displays organizer notifications (placeholder until backend wiring is complete).
 */
public class OrganizerNotificationsFragment extends Fragment {

    private static final String ARG_ORGANIZER_ID = "organizer_id";

    /**
     * @param organizerId organizer identifier propagated via arguments
     * @return configured fragment instance
     */
    @NonNull
    public static OrganizerNotificationsFragment newInstance(@Nullable String organizerId) {
        OrganizerNotificationsFragment fragment = new OrganizerNotificationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORGANIZER_ID, organizerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_notifications, container, false);
        TextView message = view.findViewById(R.id.text_notifications_placeholder);
        message.setText(getString(R.string.organizer_nav_notifications_tab));
        return view;
    }
}
