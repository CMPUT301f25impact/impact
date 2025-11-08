package com.example.impact.view;
import com.example.impact.model.Image;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.impact.R;
import com.example.impact.controller.EventController;
import com.example.impact.controller.ImageController;
import com.example.impact.model.Event;
import com.example.impact.utils.ImageUtil;
import com.example.impact.utils.QrUtil;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.WriterException;

import java.io.InputStream;
import java.util.Date;

public class OrganizerToolsFragment extends Fragment {

    private EditText etName, etDesc, etCapacity;
    private Button btnStart, btnEnd, btnCreate, btnUploadPoster;
    private ImageView imgQr;
    private Date startDate, endDate;
    private ImageView imgPosterPreview;
    private String uploadedImageId = null;


    private final EventController controller = new EventController();
    private final ImageController imageController = new ImageController();
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    String organizerEmail = null;

    @SuppressLint({"CutPasteId", "MissingInflatedId"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_organizer_tools, container, false);

        etName = v.findViewById(R.id.etEventName);
        etDesc = v.findViewById(R.id.etEventDescription);
        etCapacity = v.findViewById(R.id.etCapacity);
        btnStart = v.findViewById(R.id.btnPickStart);
        btnEnd = v.findViewById(R.id.btnPickEnd);
        imgQr = v.findViewById(R.id.imgQrPreview);
        btnCreate = v.findViewById(R.id.btnCreateEvent);
        btnUploadPoster = v.findViewById(R.id.btnUploadPoster);
        imgPosterPreview = v.findViewById(R.id.imgPosterPreview);

        if (getArguments() != null) {
            organizerEmail = getArguments().getString("organizerEmail");
        }

        if (organizerEmail == null) {
            Toast.makeText(requireContext(), "Organizer email missing", Toast.LENGTH_SHORT).show();
            btnCreate.setEnabled(false);
        } else {
            btnCreate.setEnabled(true);
        }

        btnUploadPoster.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Opening image picker...", Toast.LENGTH_SHORT).show();
            pickImageLauncher.launch("image/*");
        });

        btnStart.setOnClickListener(view -> pickDate(true));
        btnEnd.setOnClickListener(view -> pickDate(false));
        btnCreate.setOnClickListener(view -> createEvent());
        return v;
    }

    private void onImagePicked(Uri uri) {
        if (uri == null) return;
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            if (bmp != null) {
                imgPosterPreview.setImageBitmap(bmp);
                imgPosterPreview.setVisibility(View.VISIBLE);
            }

            // Convert bmp to base64 and upload
            String base64 = ImageUtil.bitmapToBase64(bmp);
            String fileName = queryFileName(uri);
            String mime = requireContext().getContentResolver().getType(uri);
            if (mime == null) mime = "image/jpeg";

            Image imageModel = new Image();
            imageModel.setFileName(fileName != null ? fileName : "poster.jpg");
            imageModel.setMimeType(mime);
            imageModel.setBase64Content(base64);

            btnUploadPoster.setEnabled(false);
            imageController.createImage(imageModel, imageId -> {
                uploadedImageId = imageId;
                btnUploadPoster.setEnabled(true);
                Toast.makeText(requireContext(), "Poster uploaded", Toast.LENGTH_SHORT).show();
            }, err -> {
                btnUploadPoster.setEnabled(true);
                Toast.makeText(requireContext(), "Upload failed: " + (err != null ? err.getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
            });

        } catch (Exception ex) {
            Toast.makeText(requireContext(), "Failed to read image: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String queryFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = requireContext().getContentResolver()
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

    private void pickDate(boolean isStart) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
        picker.addOnPositiveButtonClickListener(ms -> {
            if (isStart) {
                startDate = new Date(ms);
                btnStart.setText("Start: " + picker.getHeaderText());
            } else {
                endDate = new Date(ms);
                btnEnd.setText("End: " + picker.getHeaderText());
            }
        });
        picker.show(getParentFragmentManager(), isStart ? "reg_start" : "reg_end");
    }

    private void createEvent() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name) || startDate == null || endDate == null) {
            toast("Name + registration dates required");
            return;
        }
        if (!startDate.before(endDate)) {
            toast("Start date must be before end date");
            return;
        }

        Integer capacity = null;
        try {
            String cap = etCapacity.getText().toString().trim();
            if (!TextUtils.isEmpty(cap)) capacity = Integer.parseInt(cap);
        } catch (NumberFormatException ignored) { }

        Event e = new Event();
        e.setName(name);
        e.setDescription(etDesc.getText().toString().trim());
        e.setStartDate(startDate); // US 02.01.04
        e.setEndDate(endDate);     // US 02.01.04
        e.setOrganizerEmail(organizerEmail);
        e.setCapacity(capacity);

        if (uploadedImageId != null) {
            e.setPosterUrl(uploadedImageId);   // ensure Event has a posterUrl field
        }

        btnCreate.setEnabled(false);

        controller.createEvent(e, eventId -> {

            if (uploadedImageId != null) {
                controller.updatePosterUrl(eventId, uploadedImageId,
                        v -> { /* optional success */ },
                        err -> toast("Saved event but poster update failed: " + (err != null ? err.getMessage() : "unknown"))
                );
            }
            // Build the QR payload (deep link or just the eventId)
            String payload = "impact://event/" + eventId;

            // Save QR payload string in Firestore (NOT an image URL)
            controller.updateQrPayload(eventId, payload,
                    v -> { /* optional: payload saved */ },
                    err -> toast("Saved event, but QR payload update failed: " + err.getMessage())
            );

            // Generate and preview QR locally (no Storage)
            try {
                Bitmap bmp = QrUtil.generateQr(payload);
                imgQr.setImageBitmap(bmp);
                toast("Event created");
            } catch (WriterException ex) {
                toast("QR generation failed: " + ex.getMessage());
            } finally {
                btnCreate.setEnabled(true);
            }

        }, err -> {
            toast("Create failed: " + err.getMessage());
            btnCreate.setEnabled(true);
        });
    }

    private void toast(String s) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
    }
}
