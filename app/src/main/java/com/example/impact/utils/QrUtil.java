package com.example.impact.utils;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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
}
