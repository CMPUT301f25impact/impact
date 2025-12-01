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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.impact.R;
import com.example.impact.controller.EventController;
import com.example.impact.controller.ImageController;
import com.example.impact.model.Event;
import com.example.impact.utils.AppSession;
import com.example.impact.utils.ImageUtil;
import com.example.impact.utils.QrUtil;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.zxing.WriterException;

import java.io.InputStream;
import java.util.Date;
import java.io.OutputStream;

/**
 * Provides organizers with a simple form to create events and preview QR codes.
 */
public class OrganizerCreateEventFragment extends Fragment {

    private EditText etName, etDesc, etCapacity, etWaitlistCapacity;
    private Button btnStart, btnEnd, btnCreate, btnUploadPoster;
    private ImageView imgQr;
    private Date startDate, endDate;
    private ImageView imgPosterPreview;
    private String uploadedImageId = null;
    private Bitmap qrBitmap;        // store the last generated QR
    private Button btnSaveQr;       // save-to-gallery button



    private final EventController controller = new EventController();
    private final ImageController imageController = new ImageController();
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    String organizerId = null;

    public static final String EXTRA_ORGANIZER_ID = "organizer_id";

    // Use a static factory method to create the fragment and set arguments
    public static OrganizerCreateEventFragment newInstance(String organizerId) {
        OrganizerCreateEventFragment fragment = new OrganizerCreateEventFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ORGANIZER_ID, organizerId);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint({"CutPasteId", "MissingInflatedId"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_organizer_event_details, container, false);

        etName = v.findViewById(R.id.etEventName);
        etDesc = v.findViewById(R.id.etEventDescription);
        etCapacity = v.findViewById(R.id.etCapacity);
        etWaitlistCapacity = v.findViewById(R.id.etWaitlistCapacity);
        btnStart = v.findViewById(R.id.btnPickStart);
        btnEnd = v.findViewById(R.id.btnPickEnd);
        imgQr = v.findViewById(R.id.imgQrPreview);
        btnCreate = v.findViewById(R.id.btnCreateEvent);
        btnUploadPoster = v.findViewById(R.id.btnUploadPoster);
        imgPosterPreview = v.findViewById(R.id.imgPosterPreview);
        btnSaveQr = v.findViewById(R.id.btnSaveQr);
        btnSaveQr.setVisibility(View.GONE);
        btnSaveQr.setOnClickListener(view -> saveQrToGallery());

        if (getArguments() != null) {
            organizerId = getArguments().getString(EXTRA_ORGANIZER_ID);
        }
        if (TextUtils.isEmpty(organizerId)) {
            organizerId = AppSession.getUserId();
        }

        if (organizerId == null) {
            Toast.makeText(requireContext(), "Organizer id missing", Toast.LENGTH_SHORT).show();
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
            String fileName = ImageUtil.queryFileName(uri, requireContext());
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

     /**
     * Shows a material date picker and stores the chosen start/end date.
     */
    private void pickDate(boolean isStart) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
        picker.addOnPositiveButtonClickListener(ms -> {
            if (isStart) {
                startDate = new Date(ms);
                btnStart.setText(getString(
                        R.string.event_details_start_date_picker_button_filled,
                        picker.getHeaderText())
                );
            } else {
                endDate = new Date(ms);
                btnEnd.setText(getString(
                        R.string.event_details_end_date_picker_button_filled,
                        picker.getHeaderText())
                );
            }
        });
        picker.show(getParentFragmentManager(), isStart ? "reg_start" : "reg_end");
    }

    /**
     * Validates the form, persists the event, and generates its QR payload.
     */
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

        Integer capacity = getTextIntegerValue(etCapacity);
        if (capacity == null) {
            toast("Capacity required and must be an integer");
            return;
        }
        if (capacity < 0) {
            toast("Capacity must be a positive integer");
            return;
        }

        Integer waitlistCapacity = getTextIntegerValue(etWaitlistCapacity);
        if (waitlistCapacity != null && waitlistCapacity < 0) {
            toast("Waitlist capacity must be a positive integer");
            return;
        }
        if (waitlistCapacity != null && waitlistCapacity < capacity) {
            toast("Waitlist capacity must be larger than (or equal to) event capacity");
            return;
        }

        Event e = new Event();
        e.setName(name);
        e.setDescription(etDesc.getText().toString().trim());
        e.setStartDate(startDate); // US 02.01.04
        e.setEndDate(endDate);     // US 02.01.04
        e.setOrganizerId(organizerId);
        e.setCapacity(capacity);
        e.setWaitlistCapacity(waitlistCapacity);

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
            String payload = e.getQrCodePayload();

            // Save QR payload string in Firestore (NOT an image URL)
            controller.updateQrPayload(eventId, payload,
                    v -> { /* optional: payload saved */ },
                    err -> toast("Saved event, but QR payload update failed: " + err.getMessage())
            );

            // Generate and preview QR locally (no Storage)
            try {
                qrBitmap = QrUtil.generateQr(payload);
                imgQr.setImageBitmap(qrBitmap);
                imgQr.setVisibility(View.VISIBLE);
                btnSaveQr.setVisibility(View.VISIBLE);
                toast("Event created");
            } catch (WriterException ex) {
                qrBitmap = null;
                btnSaveQr.setVisibility(View.GONE);
                toast("QR generation failed: " + ex.getMessage());
            } finally {
                btnCreate.setEnabled(true);
            }


        }, err -> {
            toast("Create failed: " + err.getMessage());
            btnCreate.setEnabled(true);
        });
    }

    /**
     * Helper method to get the integer value of TextView (or returns null)
     * @param tv text view
     * @return integer value or null
     */
    private Integer getTextIntegerValue(TextView tv) {
        int value;
        try {
            String textVal = tv.getText().toString().trim();
            value = Integer.parseInt(textVal);
        } catch (NumberFormatException e) {
            return null;
        }
        return value;
    }

    private void saveQrToGallery() {
        if (qrBitmap == null) {
            toast("No QR code to save yet");
            return;
        }

        try {
            ContentResolver resolver = requireContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME,
                    "event_qr_" + System.currentTimeMillis() + ".png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            // This puts it under Pictures/Impact in the gallery (API 29+)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Impact");

            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                toast("Failed to save QR: no URI");
                return;
            }

            try (OutputStream out = resolver.openOutputStream(uri)) {
                if (!qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    toast("Failed to save QR");
                    return;
                }
            }

            toast("QR code saved to gallery");
        } catch (Exception e) {
            toast("Failed to save QR: " + e.getMessage());
        }
    }

    /**
     * Convenience helper for short Toast messages.
     */
    private void toast(String s) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
    }
}
