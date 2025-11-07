package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.impact.R;
import com.example.impact.utils.FirebaseUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Handles Firestore-backed user login and role-based routing.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "extra_user_id";

    private EditText emailInput;
    private EditText passwordInput;
    private ProgressBar progressBar;
    private Button loginButton;
    private TextView goToSignup;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firestore = FirebaseUtil.getFirestore();

        emailInput = findViewById(R.id.editTextLoginEmail);
        passwordInput = findViewById(R.id.editTextLoginPassword);
        progressBar = findViewById(R.id.progressBarLogin);
        loginButton = findViewById(R.id.buttonLogin);
        goToSignup = findViewById(R.id.textViewGoToSignup);

        loginButton.setOnClickListener(v -> attemptLogin());
        goToSignup.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Validates input and performs a Firestore query for the user record.
     */
    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.login_missing_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);
        firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    setLoadingState(false);
                    if (snapshot.isEmpty()) {
                        Toast.makeText(this, R.string.login_invalid_credentials, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DocumentSnapshot userDoc = snapshot.getDocuments().get(0);
                    String storedPassword = userDoc.getString("password");
                    if (storedPassword == null || !storedPassword.equals(password)) {
                        Toast.makeText(this, R.string.login_invalid_credentials, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    handleSuccessfulLogin(userDoc);
                })
                .addOnFailureListener(error -> {
                    setLoadingState(false);
                    Toast.makeText(this, R.string.login_error_generic, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates device bindings and routes users to their dashboards.
     */
    private void handleSuccessfulLogin(DocumentSnapshot userDoc) {
        String role = userDoc.getString("role");
        if (role == null) {
            role = "entrant";
        }
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (!TextUtils.isEmpty(deviceId)) {
            String currentDeviceId = userDoc.getString("deviceId");
            if (!deviceId.equals(currentDeviceId)) {
                userDoc.getReference().update("deviceId", deviceId);
            }
        }

        Toast.makeText(this, R.string.login_success_message, Toast.LENGTH_SHORT).show();
        navigateToRole(role, userDoc.getId(), userDoc.getString("email"));
    }

    /**
     * Starts the activity that matches the provided role.
     */
    private void navigateToRole(String role, String userId, @Nullable String email) {
        Intent intent;
        switch (role != null ? role : "") {
            case "admin":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "organizer":
                intent = new Intent(this, OrganizerActivity.class);
                break;
            default:
                intent = new Intent(this, EntrantActivity.class);
                break;
        }
        intent.putExtra(EXTRA_USER_ID, userId);
        if (email != null) {
            intent.putExtra("extra_user_email", email);
        }
        startActivity(intent);
        finish();
    }

    /**
     * Enables/disables form elements while login is in progress.
     */
    private void setLoadingState(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        goToSignup.setEnabled(!loading);
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
    }
}
