package com.example.impact.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageUtil {

    public static String bitmapToBase64(Bitmap bmp) {
        if (bmp == null) return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
        byte[] bytes = out.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static Bitmap base64ToBitmap(String base64) {
        return com.example.impact.model.Image.decodeBase64ToBitmapStatic(base64);
        // Alternatively, call Image.decodeBase64ToBitmap() if you prefer instance method.
    }

    /**
     * Query an android filename from it's URI
     * @param uri file URI
     * @return filename or null if no name found
     */
    public static String queryFileName(Uri uri, Context context) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = context.getContentResolver()
                    .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(0);
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            String path = uri.getPath();
            if (path == null) return null;
            int cut = path.lastIndexOf('/');
            if (cut != -1) result = path.substring(cut + 1);
        }
        return result;
    }
}
