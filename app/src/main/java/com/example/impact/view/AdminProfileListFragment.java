package com.example.impact.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.EntrantController;
import com.example.impact.model.Entrant;
import com.example.impact.utils.DeletionConfirmationUtil;
import com.example.impact.view.adapter.AdminProfileAdapter;

import java.util.List;

/**
 * Displays entrant profiles for administrators.
 */
public class AdminProfileListFragment extends Fragment implements AdminProfileAdapter.DeleteListener {

    private AdminProfileAdapter adapter;
    private EntrantController entrantController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entrantController = new EntrantController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.admin_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdminProfileAdapter(this);
        recyclerView.setAdapter(adapter);

        loadProfiles();
        return view;
    }

    private void loadProfiles() {
        entrantController.fetchAllEntrants(this::onProfilesLoaded,
                error -> showToast(R.string.admin_profile_list_error_loading));
    }

    private void onProfilesLoaded(List<Entrant> entrants) {
        adapter.setProfiles(entrants);
    }

    private void onProfileDelete(String name) {
        if (!isAdded()) {
            return;
        }
        showToast(getString(R.string.admin_profile_list_deletion, name));
        loadProfiles();
    }

    private void showToast(int stringResId) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), stringResId, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteProfileClicked(int position, Entrant entrant) {
        if (getContext() == null || !isAdded()) {
            return;
        }
        String displayName = !TextUtils.isEmpty(entrant.getName())
                ? entrant.getName()
                : getString(R.string.admin_profile_list_name_placeholder);

        DeletionConfirmationUtil confirmation = new DeletionConfirmationUtil(
                getContext(),
                displayName,
                () -> entrantController.deleteProfile(entrant.getId(),
                        unused -> onProfileDelete(displayName),
                        error -> showToast(R.string.admin_profile_list_error_deletion)));
        confirmation.show();
    }
}
