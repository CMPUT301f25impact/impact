package com.example.impact.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.impact.R;
import com.example.impact.controller.UserController;
import com.example.impact.model.Entrant;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Activity that allows entrants to capture and persist their profile details.
 */
public class EntrantProfileFragment extends Fragment {
    public static final String EXTRA_ENTRANT_ID = "entrant_id";

    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private Button saveButton;
    private Button updateButton;
    private Button deleteButton;

    private UserController userController;
    private String entrantId;
    private boolean hasExistingProfile;

    public interface ProfileInteractionListener {
        void onProfileDeleted();
    }
    private ProfileInteractionListener listener;

    // Use a static factory method to create the fragment and set arguments
    public static EntrantProfileFragment newInstance(String entrantId) {
        EntrantProfileFragment fragment = new EntrantProfileFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ENTRANT_ID, entrantId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ProfileInteractionListener) {
            listener = (ProfileInteractionListener) context;
        } else {
             throw new RuntimeException(context + " must implement ProfileInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userController = new UserController();

        // Retrieve arguments
        if (getArguments() != null) {
            entrantId = getArguments().getString(EXTRA_ENTRANT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.entrant_profile_error_missing_id, Toast.LENGTH_SHORT).show();
            setButtonsEnabled(false);
            return;
        }

        nameInput = view.findViewById(R.id.editTextEntrantName);
        emailInput = view.findViewById(R.id.editTextEntrantEmail);
        phoneInput = view.findViewById(R.id.editTextEntrantPhone);
        saveButton = view.findViewById(R.id.buttonSaveProfile);
        updateButton = view.findViewById(R.id.buttonUpdateProfile);
        deleteButton = view.findViewById(R.id.buttonDeleteProfile);

        saveButton.setOnClickListener(v -> saveProfile());
        updateButton.setOnClickListener(v -> updateProfile());
        deleteButton.setOnClickListener(v -> confirmDeleteProfile());

        setButtonsEnabled(false);
        loadProfile();
    }

    /**
     * Sets whether buttons in view are enabled
     * @param enabled enable buttons
     */
    private void setButtonsEnabled(boolean enabled) {
        if (saveButton != null) saveButton.setEnabled(enabled);
        if (updateButton != null) updateButton.setEnabled(enabled);
        if (deleteButton != null) deleteButton.setEnabled(enabled);
    }

    /**
     * Loads any previously saved profile and toggles buttons accordingly.
     */
    private void loadProfile() {
        userController.fetchProfile(entrantId, entrant -> {
            if (entrant != null) {
                hasExistingProfile = true;
                nameInput.setText(entrant.getName());
                emailInput.setText(entrant.getEmail());
                phoneInput.setText(entrant.getPhone());
            } else {
                hasExistingProfile = false;
            }
            setButtonsEnabled(hasExistingProfile);
        }, error -> Toast.makeText(requireContext(), R.string.entrant_profile_error_load, Toast.LENGTH_SHORT).show());
    }

    /**
     * Attempts to create a profile from current field values.
     */
    private void saveProfile() {
        Entrant entrant = buildEntrantFromInputs();
        OnSuccessListener<Void> successListener = unused -> {
            hasExistingProfile = true;
            setButtonsEnabled(true);
            saveButton.setEnabled(false); // disable save button on success
            Toast.makeText(requireContext(), R.string.entrant_profile_saved_message, Toast.LENGTH_SHORT).show();
        };

        executeProfileMutation(() -> userController.saveProfileToFirestore(entrant, successListener, error ->
                Toast.makeText(requireContext(), R.string.entrant_profile_error_save_failed, Toast.LENGTH_SHORT).show()));
    }

    /**
     * Persists modifications to an existing profile.
     */
    private void updateProfile() {
        if (!hasExistingProfile) {
            Toast.makeText(requireContext(), R.string.entrant_profile_error_validation, Toast.LENGTH_SHORT).show();
            return;
        }
        Entrant entrant = buildEntrantFromInputs();
        OnSuccessListener<Void> successListener = unused ->
                Toast.makeText(requireContext(), R.string.entrant_profile_update_message, Toast.LENGTH_SHORT).show();

        executeProfileMutation(() -> userController.updateProfile(entrant, successListener, error ->
                Toast.makeText(requireContext(), R.string.entrant_profile_error_save_failed, Toast.LENGTH_SHORT).show()));
    }

    /**
     * Prompts the entrant to confirm profile deletion.
     */
    private void confirmDeleteProfile() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.entrant_profile_delete_confirm_title)
                .setMessage(R.string.entrant_profile_delete_confirm_message)
                .setPositiveButton(R.string.entrant_profile_delete_confirm_positive, this::deleteProfile)
                .setNegativeButton(R.string.entrant_profile_delete_confirm_negative, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Removes the profile document and closes the screen.
     */
    private void deleteProfile(DialogInterface dialogInterface, int which) {
        userController.deleteProfile(entrantId, unused -> {
            Toast.makeText(requireContext(), R.string.entrant_profile_deleted_message, Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onProfileDeleted();
            } else {
                getParentFragmentManager().popBackStack();
            }
        }, error -> Toast.makeText(requireContext(), R.string.entrant_profile_error_save_failed, Toast.LENGTH_SHORT).show());
    }

    /**
     * Creates an in-memory entrant model from the text fields.
     */
    private Entrant buildEntrantFromInputs() {
        String phoneValue = phoneInput.getText().toString().trim();
        phoneValue = phoneValue.isEmpty() ? null : phoneValue;

        return new Entrant(
                entrantId,
                nameInput.getText().toString().trim(),
                emailInput.getText().toString().trim(),
                phoneValue
        );
    }

    /**
     * Wraps mutations in validation handling so the UI can show errors.
     */
    private void executeProfileMutation(Runnable mutation) {
        try {
            mutation.run();
        } catch (IllegalArgumentException exception) {
            Toast.makeText(requireContext(), R.string.entrant_profile_error_validation, Toast.LENGTH_SHORT).show();
        }
    }
}
