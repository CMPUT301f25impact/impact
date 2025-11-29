package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.impact.R;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * Lightweight activity that scans QR codes and returns the encoded event id to the caller.
 */
public class QrScannerActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";

    private DecoratedBarcodeView barcodeView;
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result == null || TextUtils.isEmpty(result.getText())) {
                return;
            }
            barcodeView.pause();
            Intent data = new Intent();
            data.putExtra(EXTRA_EVENT_ID, result.getText());
            setResult(RESULT_OK, data);
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        barcodeView = findViewById(R.id.barcodeScanner);
        barcodeView.decodeContinuous(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        barcodeView.pause();
        super.onPause();
    }
}
