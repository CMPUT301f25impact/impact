package com.example.impact.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.model.Entrant;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter used for rendering entrant profiles in the admin dashboard.
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.AdminProfileViewHolder> {

    public interface DeleteListener {
        void onDeleteProfileClicked(int position, Entrant entrant);
    }

    private final List<Entrant> profiles;
    private final DeleteListener deleteListener;

    public AdminProfileAdapter(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
        this.profiles = new ArrayList<>();
    }

    @NonNull
    @Override
    public AdminProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
        return new AdminProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProfileViewHolder holder, int position) {
        holder.bind(profiles.get(position), position);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    public void setProfiles(List<Entrant> newProfiles) {
        profiles.clear();
        if (newProfiles != null) {
            profiles.addAll(newProfiles);
        }
        notifyDataSetChanged();
    }

    class AdminProfileViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final TextView emailTextView;
        final TextView phoneTextView;
        final TextView idTextView;
        final Button deleteButton;

        AdminProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.admin_profile_name);
            emailTextView = itemView.findViewById(R.id.admin_profile_email);
            phoneTextView = itemView.findViewById(R.id.admin_profile_phone);
            idTextView = itemView.findViewById(R.id.admin_profile_id);
            deleteButton = itemView.findViewById(R.id.admin_profile_delete_button);
        }

        void bind(Entrant entrant, int position) {
            String name = entrant.getName() != null ? entrant.getName() : itemView.getContext().getString(R.string.admin_profile_list_name_placeholder);
            String email = entrant.getEmail() != null ? entrant.getEmail() : itemView.getContext().getString(R.string.admin_profile_list_email_placeholder);
            String phone = entrant.getPhone() != null ? entrant.getPhone() : itemView.getContext().getString(R.string.admin_profile_list_phone_placeholder);

            nameTextView.setText(name);
            emailTextView.setText(itemView.getContext().getString(R.string.admin_profile_list_email_format, email));
            phoneTextView.setText(itemView.getContext().getString(R.string.admin_profile_list_phone_format, phone));
            idTextView.setText(itemView.getContext().getString(R.string.admin_profile_list_id_format, entrant.getId()));

            deleteButton.setOnClickListener(v -> deleteListener.onDeleteProfileClicked(position, entrant));
        }
    }
}
