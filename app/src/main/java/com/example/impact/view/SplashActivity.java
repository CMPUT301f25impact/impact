package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.impact.R;
import com.example.impact.utils.FirebaseUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Entry point that auto-logs users based on stored device identifiers.
 */
public class SplashActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firestore = FirebaseUtil.getFirestore();
        checkSession();
    }

    private void checkSession() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) {
            proceedToLogin(false);
            return;
        }

        firestore.collection("users")
                .whereEqualTo("deviceId", deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        proceedToLogin(false);
                        return;
                    }
                    DocumentSnapshot userDoc = snapshot.getDocuments().get(0);
                    proceedToRole(userDoc);
                })
                .addOnFailureListener(error -> proceedToLogin(true));
    }

    private void proceedToRole(DocumentSnapshot userDoc) {
        String role = userDoc.getString("role");
        String email = userDoc.getString("email");
        Intent intent;
        if ("admin".equals(role)) {
            intent = new Intent(this, AdminActivity.class);
        } else if ("organizer".equals(role)) {
            intent = new Intent(this, OrganizerActivity.class);
        } else {
            intent = new Intent(this, EntrantActivity.class);
        }
        intent.putExtra(LoginActivity.EXTRA_USER_ID, userDoc.getId());
        if (email != null) {
            intent.putExtra("extra_user_email", email);
        }
        startActivity(intent);
        finish();
    }

    private void proceedToLogin(boolean showError) {
        if (showError) {
            Toast.makeText(this, R.string.login_error_generic, Toast.LENGTH_SHORT).show();
        }
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
