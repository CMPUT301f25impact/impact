package com.example.impact.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.model.EntrantHistoryItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Displays entrant event history entries in a recycler view.
 */
public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.HistoryViewHolder> {
    private final List<EntrantHistoryItem> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    /**
     * Inflates a history row.
     *
     * @param parent parent recycler
     * @param viewType unused view type
     * @return view holder instance
     */
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_history, parent, false);
        return new HistoryViewHolder(view);
    }

    /**
     * Binds a history row.
     *
     * @param holder   view holder
     * @param position adapter position
     */
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    /**
     * @return number of history entries tracked
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Replaces displayed history entries.
     *
     * @param newItems list of history items
     */
    public void setItems(List<EntrantHistoryItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /**
     * Renders a single history entry row.
     */
    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView statusText;
        private final TextView dateText;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textViewHistoryEventName);
            statusText = itemView.findViewById(R.id.textViewHistoryStatus);
            dateText = itemView.findViewById(R.id.textViewHistoryDate);
        }

        /**
         * Binds an entrant history item to the row.
         *
         * @param item history entry to display
         */
        void bind(EntrantHistoryItem item) {
            nameText.setText(item.getEventName());
            statusText.setText(itemView.getContext().getString(R.string.event_details_status_label, item.getStatus()));
            Date timestamp = item.getTimestamp();
            dateText.setText(timestamp != null ? dateFormat.format(timestamp) : "");
        }
    }
}
