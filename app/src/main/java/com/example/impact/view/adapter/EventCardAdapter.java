package com.example.impact.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.model.Event;
import com.example.impact.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Card-based adapter for showing organizer events with a CTA to view entrants.
 */
public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.VH> {

    /**
     * Callback for when the entrants button is pressed.
     */
    public interface OnViewEntrants {
        /**
         * Invoked when a user requests to view entrants for the event.
         *
         * @param event event tied to the card
         */
        void onClick(Event e);
    }

    private final List<Event> data = new ArrayList<>();
    private final OnViewEntrants cb;

    /**
     * Creates an adapter backed by the supplied callback.
     *
     * @param cb callback invoked when the entrants button is pressed
     */
    public EventCardAdapter(OnViewEntrants cb) { this.cb = cb; }

    /**
     * Replaces the current card list.
     *
     * @param events new list of events (may be {@code null})
     */
    public void submitList(List<Event> events) {
        data.clear();
        if (events != null) data.addAll(events);
        notifyDataSetChanged();
    }

    /**
     * Inflates an organizer event card.
     *
     * @param p parent recycler
     * @param vType unused view type
     * @return view holder instance
     */
    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_event_card, p, false);
        return new VH(v);
    }

    /**
     * Binds an event card row.
     *
     * @param h view holder
     * @param i adapter position
     */
    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Event e = data.get(i);
        h.title.setText(e.getName());
        String window = formatWindow(e.getStartDate(), e.getEndDate());
        h.window.setText("Registration: " + window);
        h.viewEntrants.setOnClickListener(v -> cb.onClick(e));
    }

    /**
     * @return number of cards rendered
     */
    @Override public int getItemCount() { return data.size(); }

    /**
     * View holder tracking card views.
     */
    static class VH extends RecyclerView.ViewHolder {
        TextView title, window; Button viewEntrants;
        /**
         * @param v inflated card view
         */
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            window = v.findViewById(R.id.tvRegWindow);
            viewEntrants = v.findViewById(R.id.btnViewEntrants);
        }
    }

    /**
     * Formats a registration window range.
     *
     * @param s registration start date
     * @param e registration end date
     * @return formatted range or "-" if missing
     */
    private static String formatWindow(@Nullable Date s, @Nullable Date e) {
        DateFormat df = new SimpleDateFormat("MMM d", Locale.getDefault());
        if (s == null || e == null) return "-";
        return df.format(s) + " - " + df.format(e);
    }
}
