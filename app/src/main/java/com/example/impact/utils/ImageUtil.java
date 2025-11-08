package com.example.impact.utils;

import android.graphics.Bitmap;
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
}
