package com.example.impact.utils;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.OutputStream;

/**
 * Helper for generating QR code bitmaps.
 */
public class QrUtil {
    /**
     * Creates a QR code bitmap for the provided payload.
     *
     * @param payload text encoded in the QR code
     * @return generated bitmap
     * @throws WriterException if encoding fails
     */
    public static Bitmap generateQr(String payload) throws WriterException {
        BitMatrix matrix = new MultiFormatWriter().encode(
                payload, BarcodeFormat.QR_CODE, 400, 400
        );
        return new BarcodeEncoder().createBitmap(matrix);
    }

    /**
     * Saves the provided QR code bitmap (or image bitmap) to the android phone's local gallery
     * @param qrBitmap bitmap of QR code
     * @param context app context
     */
    public static void saveQrToGallery(Bitmap qrBitmap, Context context) {
        if (qrBitmap == null) {
            Toast.makeText(context, "No QR to save", LENGTH_SHORT).show();
            return;
        }

        try {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME,
                    "event_qr_" + System.currentTimeMillis() + ".png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            // This puts it under Pictures/Impact in the gallery (API 29+)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Impact");

            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(context, "Could not save QR: No URI", LENGTH_LONG).show();
                return;
            }

            try (OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null || !qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    Toast.makeText(context, "Could not save QR", LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(context, "QR saved to gallery", LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to save QR: " + e.getMessage(), LENGTH_LONG).show();
        }
    }
}
