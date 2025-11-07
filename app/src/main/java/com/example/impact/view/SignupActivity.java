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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Collects minimal credentials and creates entrant accounts in Firestore.
 */
public class SignupActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private ProgressBar progressBar;
    private Button signupButton;
    private TextView goToLogin;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firestore = FirebaseUtil.getFirestore();

        emailInput = findViewById(R.id.editTextSignupEmail);
        passwordInput = findViewById(R.id.editTextSignupPassword);
        progressBar = findViewById(R.id.progressBarSignup);
        signupButton = findViewById(R.id.buttonSignup);
        goToLogin = findViewById(R.id.textViewGoToLogin);

        signupButton.setOnClickListener(v -> attemptSignup());
        goToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Validates fields and checks for duplicate emails.
     */
    private void attemptSignup() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.signup_missing_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) {
            Toast.makeText(this, R.string.signup_error_generic, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);
        firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        setLoadingState(false);
                        Toast.makeText(this, R.string.signup_email_exists, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createUser(email, password, deviceId);
                })
                .addOnFailureListener(error -> {
                    setLoadingState(false);
                    Toast.makeText(this, R.string.signup_error_generic, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Persists a new entrant record in Firestore.
     */
    private void createUser(String email, String password, String deviceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.put("role", "entrant");
        data.put("deviceId", deviceId);
        data.put("createdAt", FieldValue.serverTimestamp());

        firestore.collection("users")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    setLoadingState(false);
                    Toast.makeText(this, R.string.signup_success_message, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(error -> {
                    setLoadingState(false);
                    Toast.makeText(this, R.string.signup_error_generic, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Toggles form interactions while network requests run.
     */
    private void setLoadingState(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        signupButton.setEnabled(!loading);
        goToLogin.setEnabled(!loading);
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
    }
}
