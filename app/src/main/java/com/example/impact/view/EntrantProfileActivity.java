package com.example.impact.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.impact.R;
import com.example.impact.controller.EntrantController;
import com.example.impact.model.Entrant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Activity that allows entrants to capture and persist their profile details.
 */
public class EntrantProfileActivity extends AppCompatActivity {
    public static final String EXTRA_ENTRANT_ID = "entrant_id";

    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private Button updateButton;
    private Button deleteButton;

    private EntrantController entrantController;
    private String entrantId;
    private boolean hasExistingProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_profile);

        entrantController = new EntrantController();

        nameInput = findViewById(R.id.editTextEntrantName);
        emailInput = findViewById(R.id.editTextEntrantEmail);
        phoneInput = findViewById(R.id.editTextEntrantPhone);
        Button saveButton = findViewById(R.id.buttonSaveProfile);
        updateButton = findViewById(R.id.buttonUpdateProfile);
        deleteButton = findViewById(R.id.buttonDeleteProfile);

        entrantId = getIntent().getStringExtra(EXTRA_ENTRANT_ID);
        if (entrantId == null || entrantId.trim().isEmpty()) {
            Toast.makeText(this, R.string.entrant_profile_error_missing_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        saveButton.setOnClickListener(view -> saveProfile());
        updateButton.setOnClickListener(view -> updateProfile());
        deleteButton.setOnClickListener(view -> confirmDeleteProfile());

        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        loadProfile();
    }

    /**
     * Loads any previously saved profile and toggles buttons accordingly.
     */
    private void loadProfile() {
        entrantController.fetchProfile(entrantId, entrant -> {
            if (entrant != null) {
                hasExistingProfile = true;
                nameInput.setText(entrant.getName());
                emailInput.setText(entrant.getEmail());
                phoneInput.setText(entrant.getPhone());
            } else {
                hasExistingProfile = false;
            }
            updateButton.setEnabled(hasExistingProfile);
            deleteButton.setEnabled(hasExistingProfile);
        }, error -> Toast.makeText(this, R.string.entrant_profile_error_load, Toast.LENGTH_SHORT).show());
    }

    /**
     * Attempts to create a profile from current field values.
     */
    private void saveProfile() {
        Entrant entrant = buildEntrantFromInputs();
        OnSuccessListener<Void> successListener = unused -> {
            hasExistingProfile = true;
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
            Toast.makeText(this, R.string.entrant_profile_saved_message, Toast.LENGTH_SHORT).show();
        };

        executeProfileMutation(() -> entrantController.saveProfileToFirestore(entrant, successListener, error ->
                Toast.makeText(this, R.string.entrant_profile_error_save_failed, Toast.LENGTH_SHORT).show()));
    }

    /**
     * Persists modifications to an existing profile.
     */
    private void updateProfile() {
        if (!hasExistingProfile) {
            Toast.makeText(this, R.string.entrant_profile_error_validation, Toast.LENGTH_SHORT).show();
            return;
        }
        Entrant entrant = buildEntrantFromInputs();
        OnSuccessListener<Void> successListener = unused ->
                Toast.makeText(this, R.string.entrant_profile_update_message, Toast.LENGTH_SHORT).show();

        executeProfileMutation(() -> entrantController.updateProfile(entrant, successListener, error ->
                Toast.makeText(this, R.string.entrant_profile_error_save_failed, Toast.LENGTH_SHORT).show()));
    }

    /**
     * Prompts the entrant to confirm profile deletion.
     */
    private void confirmDeleteProfile() {
        new AlertDialog.Builder(this)
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
        entrantController.deleteProfile(entrantId, unused -> {
            Toast.makeText(this, R.string.entrant_profile_deleted_message, Toast.LENGTH_SHORT).show();
            finish();
        }, error -> Toast.makeText(this, R.string.entrant_profile_error_save_failed, Toast.LENGTH_SHORT).show());
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
            Toast.makeText(this, R.string.entrant_profile_error_validation, Toast.LENGTH_SHORT).show();
        }
    }
}
