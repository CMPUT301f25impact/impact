package com.example.impact.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.impact.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Utility for managing Firestore-backed sessions by clearing the deviceId binding on logout.
 */
public class SessionManager {

    public interface LogoutCallback {
        void onLoggedOut();
    }

    private final Context context;
    private final FirebaseFirestore firestore;

    public SessionManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseUtil.getFirestore();
    }

    public void clearSession(@NonNull LogoutCallback callback) {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) {
            callback.onLoggedOut();
            return;
        }

        firestore.collection("users")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.forEach(documentSnapshot ->
                                documentSnapshot.getReference().update("deviceId", null));
                    }
                    Toast.makeText(context, R.string.logout_success, Toast.LENGTH_SHORT).show();
                    callback.onLoggedOut();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(context, R.string.logout_error, Toast.LENGTH_SHORT).show();
                    callback.onLoggedOut();
                });
    }
}
