package com.example.impact.utils;

import android.app.AlertDialog;
import android.content.Context;

import com.example.impact.R;

/**
 * Class used to construct a basic deletion confirmation dialog
 */
public class DeletionConfirmationUtil {

    private final AlertDialog dialog;

    /**
     * Create a new confirm deletion dialog
     * @param context app context
     * @param item item string to confirm
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
     * Show the dialog
     */
    public void show() {
        dialog.show();
    }
}
