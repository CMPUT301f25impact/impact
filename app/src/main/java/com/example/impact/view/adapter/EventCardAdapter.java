package com.example.impact.view.adapter;
import com.example.impact.model.Image;
import android.graphics.Bitmap;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.controller.ImageController;
import com.example.impact.model.Event;
import com.example.impact.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.VH> {

    public interface OnViewEntrants { void onClick(Event e); }

    private final List<Event> data = new ArrayList<>();
    private final OnViewEntrants cb;
    private final ImageController imageController = new ImageController();

    public EventCardAdapter(OnViewEntrants cb) { this.cb = cb; }

    public void submitList(List<Event> events) {
        data.clear();
        if (events != null) data.addAll(events);
        notifyDataSetChanged();
    }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_event_card, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Event e = data.get(i);
        h.title.setText(e.getName());
        String window = formatWindow(e.getStartDate(), e.getEndDate());
        h.window.setText("Registration: " + window);
        h.viewEntrants.setOnClickListener(v -> cb.onClick(e));

        // poster handling
        h.ivPoster.setVisibility(View.GONE);
        String posterId = e.getPosterUrl(); // keep consistent with your Event model
        if (posterId != null && !posterId.isEmpty()) {
            Log.d("EventAdapter", "Bind pos=" + i + " eventId=" + (e.getId()) + " posterId=" + e.getPosterUrl());
            imageController.fetchImage(posterId,
                    (Image img) -> {
                        Log.d("EventAdapter", "fetchImage success for id=" + posterId + " img=" + (img!=null));
                        if (img != null && img.getBase64Content() != null) {
                            Bitmap bmp = img.decodeBase64ToBitmap();
                            if (bmp != null) {
                                h.ivPoster.setImageBitmap(bmp);
                                h.ivPoster.setVisibility(View.VISIBLE);
                            }
                        }
                    },
                    (Exception err) -> {
                        Log.e("EventAdapter", "fetchImage failed for id=" + posterId, err);
                    }
            );
        }

    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, window; Button viewEntrants;
        ImageView ivPoster;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            window = v.findViewById(R.id.tvRegWindow);
            viewEntrants = v.findViewById(R.id.btnViewEntrants);
            ivPoster = itemView.findViewById(R.id.ivPoster);
        }
    }

    private static String formatWindow(@Nullable Date s, @Nullable Date e) {
        DateFormat df = new SimpleDateFormat("MMM d", Locale.getDefault());
        if (s == null || e == null) return "-";
        return df.format(s) + " - " + df.format(e);
    }
}
