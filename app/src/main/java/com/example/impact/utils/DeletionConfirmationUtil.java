package com.example.impact.utils;

import android.app.AlertDialog;
import android.content.Context;

import com.example.impact.R;

/**
 * Utility wrapper that builds a consistent deletion confirmation dialog.
 */
public class DeletionConfirmationUtil {

    private final AlertDialog dialog;

    /**
     * Creates a new confirmation dialog for destructive actions.
     *
     * @param context   context used to build the dialog
     * @param item      descriptive name of the resource being deleted
     * @param onSuccess runnable executed when the user confirms deletion
     */
    public DeletionConfirmationUtil(Context context, String item, Runnable onSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String text = context.getResources().getString(R.string.delete_dialog_text, item);

        builder.setTitle("Confirm Deletion");
        builder.setMessage(text);

        builder.setPositiveButton("DELETE", (dialog, which) -> {
            onSuccess.run();
            dialog.dismiss(); // Close the dialog
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel(); // Close the dialog
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Displays the dialog to the user.
     */
    public void show() {
        dialog.show();
    }
}
