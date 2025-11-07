package com.example.impact.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter used for rendering entrant profiles in the admin dashboard.
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.AdminProfileViewHolder> {

    /**
     * Notifies when an admin requests to remove a profile.
     */
    public interface DeleteListener {
        /**
         * Called when an admin taps delete on a profile card.
         *
         * @param position adapter position
         * @param user  profile slated for deletion
         */
        void onDeleteProfileClicked(int position, User user);
    }

    private final List<User> profiles;
    private final DeleteListener deleteListener;

    /**
     * Builds an adapter capable of deleting profiles.
     *
     * @param deleteListener invoked when delete is requested
     */
    public AdminProfileAdapter(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
        this.profiles = new ArrayList<>();
    }

    /**
     * Inflates a profile card row.
     *
     * @param parent parent recycler
     * @param viewType unused view type
     * @return view holder instance
     */
    @NonNull
    @Override
    public AdminProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
        return new AdminProfileViewHolder(view);
    }

    /**
     * Binds a profile row.
     *
     * @param holder   view holder
     * @param position adapter position
     */
    @Override
    public void onBindViewHolder(@NonNull AdminProfileViewHolder holder, int position) {
        holder.bind(profiles.get(position), position);
    }

    /**
     * @return number of profiles displayed
     */
    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * Replaces the current profile dataset.
     *
     * @param newProfiles entrant list to display (may be {@code null})
     */
    public void setProfiles(List<User> newProfiles) {
        profiles.clear();
        if (newProfiles != null) {
            profiles.addAll(newProfiles);
        }
        notifyDataSetChanged();
    }

    /**
     * Holds references to profile card widgets.
     */
    class AdminProfileViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final TextView roleTextView;
        final TextView emailTextView;
        final TextView phoneTextView;
        final TextView idTextView;
        final Button deleteButton;

        /**
         * @param itemView inflated profile card view
         */
        AdminProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.admin_profile_name);
            roleTextView = itemView.findViewById(R.id.admin_profile_role);
            emailTextView = itemView.findViewById(R.id.admin_profile_email);
            phoneTextView = itemView.findViewById(R.id.admin_profile_phone);
            idTextView = itemView.findViewById(R.id.admin_profile_id);
            deleteButton = itemView.findViewById(R.id.admin_profile_delete_button);
        }

        /**
         * Populates the card with entrant details.
         *
         * @param user user profile bound to the row
         * @param position adapter position
         */
        void bind(User user, int position) {
            String name = user.getName() != null ? user.getName() : itemView.getContext().getString(R.string.admin_profile_list_name_placeholder);
            String role = user.getRole() != null ? user.getRole() : "Unknown role";
            String email = user.getEmail() != null ? user.getEmail() : itemView.getContext().getString(R.string.admin_profile_list_email_placeholder);
            String phone = user.getPhone() != null ? user.getPhone() : itemView.getContext().getString(R.string.admin_profile_list_phone_placeholder);

            nameTextView.setText(name);
            roleTextView.setText(itemView.getContext().getString(R.string.admin_profile_list_role_format, role));
            emailTextView.setText(itemView.getContext().getString(R.string.admin_profile_list_email_format, email));
            phoneTextView.setText(itemView.getContext().getString(R.string.admin_profile_list_phone_format, phone));
            idTextView.setText(itemView.getContext().getString(R.string.admin_profile_list_id_format, user.getId()));

            deleteButton.setOnClickListener(v -> deleteListener.onDeleteProfileClicked(position, user));
        }
    }
}
