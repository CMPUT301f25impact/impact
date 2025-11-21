package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.impact.R;
import com.example.impact.controller.UserController;
import com.example.impact.model.User;
import com.example.impact.utils.AppSession;
import com.example.impact.utils.role.EntrantDb;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Entry point that auto-logs users based on stored device identifiers.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkSession();
    }

    /**
     * Determines whether an existing device-bound session can be reused.
     */
    private void checkSession() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) {
            proceedToLogin(false);
            return;
        }

        EntrantDb.findUserByDeviceId(deviceId)
                .addOnSuccessListener(userDoc -> {
                    if (userDoc == null) {
                        proceedToLogin(false);
                        return;
                    }
                    User user = UserController.mapSnapshotToUser(userDoc);
                    if (user == null) {
                        proceedToLogin(true);
                        return;
                    }
                    AppSession.initialize(user);
                    proceedToRole(user);
                })
                .addOnFailureListener(error -> proceedToLogin(true));
    }

    /**
     * Routes to the dashboard that matches the stored role.
     */
    private void proceedToRole(@Nullable User user) {
        if (user == null) {
            proceedToLogin(true);
            return;
        }
        String role = user.getRole();
        String email = user.getEmail();
        Intent intent;
        if ("admin".equals(role)) {
            intent = new Intent(this, AdminActivity.class);
        } else if ("organizer".equals(role)) {
            intent = new Intent(this, OrganizerActivity.class);
        } else {
            intent = new Intent(this, EntrantActivity.class);
        }
        intent.putExtra(LoginActivity.EXTRA_USER_ID, user.getId());
        if (email != null) {
            intent.putExtra("extra_user_email", email);
        }
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to the login screen, optionally surfacing an error toast.
     */
    private void proceedToLogin(boolean showError) {
        if (showError) {
            Toast.makeText(this, R.string.login_error_generic, Toast.LENGTH_SHORT).show();
        }
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
