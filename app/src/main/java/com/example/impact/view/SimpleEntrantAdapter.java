package com.example.impact.view;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.impact.R;
import com.example.impact.view.adapter.EntrantRow;

import java.util.ArrayList;
import java.util.List;

public class SimpleEntrantAdapter extends RecyclerView.Adapter<SimpleEntrantAdapter.VH> {
    private final List<EntrantRow> data = new ArrayList<>();

    public void submit(List<EntrantRow> rows) {
        data.clear();
        if (rows != null) data.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_event_history, p, false);
        // reusing your existing item layout (title/status/date fields). Map name/email/joined.
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        EntrantRow r = data.get(i);
        h.name.setText(r.name);
        h.status.setText(r.email);
        h.date.setText("Joined: " + r.joinedAt);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, status, date;
        VH(@NonNull View v) {
            super(v);
            name   = v.findViewById(R.id.textViewHistoryEventName);
            status = v.findViewById(R.id.textViewHistoryStatus);
            date   = v.findViewById(R.id.textViewHistoryDate);
        }
    }
}
