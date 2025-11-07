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

    /**
     * Notified once logout cleanup completes.
     */
    public interface LogoutCallback {
        /**
         * Called when all logout operations finish (success or failure).
         */
        void onLoggedOut();
    }

    private final Context context;
    private final FirebaseFirestore firestore;

    /**
     * Builds a manager tied to the provided context.
     *
     * @param context activity or app context
     */
    public SessionManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseUtil.getFirestore();
    }

    /**
     * Removes device bindings from any logged-in user documents before executing the callback.
     *
     * @param callback invoked when local logout flow completes
     */
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
