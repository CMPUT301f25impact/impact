package com.example.impact.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.impact.R;
import com.example.impact.controller.EventController;
import com.example.impact.controller.ImageController;
import com.example.impact.model.Event;
import com.example.impact.model.Image;
import com.example.impact.utils.AppSession;
import com.example.impact.utils.DateUtil;
import com.example.impact.utils.ImageUtil;
import com.example.impact.utils.QrUtil;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.zxing.WriterException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

/**
 * Provides Organizers with a UI to edit event details after it's creation
 */
public class OrganizerEditEventFragment extends Fragment {

    // static keys
    public static final String EXTRA_ORGANIZER_ID = "organizer_id";
    public static final String EXTRA_EVENT = "event";

    // views
    private EditText etName, etDesc, etCapacity, etWaitlistCapacity;
    private Button btnStart, btnEnd, btnEdit, btnUploadPoster, btnSaveQr;
    private ImageView imgQr;
    private ImageView imgPosterPreview;

    // data
    private Date startDate, endDate;
    private Event event;
    private String organizerId;
    private Image loadedPoster;
    private Bitmap qrBitmap;
    private boolean posterUpdated;

    // controllers
    private final EventController eventController = new EventController();
    private final ImageController imageController = new ImageController();
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onPosterPicked);

    // Use a static factory method to create the fragment and set arguments
    public static OrganizerEditEventFragment newInstance(String organizerId, Event event) {
        OrganizerEditEventFragment fragment = new OrganizerEditEventFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ORGANIZER_ID, organizerId);
        args.putSerializable(EXTRA_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            event = (Event) args.getSerializable(EXTRA_EVENT);
            organizerId = args.getString(EXTRA_ORGANIZER_ID);
        }

        // attempt to get organizer id through app session
        if (organizerId == null || TextUtils.isEmpty(organizerId)) {
            organizerId = AppSession.getUserId();
        }

        if (organizerId == null) {
            Toast.makeText(requireContext(), "Organizer ID missing", Toast.LENGTH_SHORT).show();
            cancelFragment();
            return;
        }
        if (event == null) {
            Toast.makeText(requireContext(), "Event details missing", Toast.LENGTH_SHORT).show();
            cancelFragment();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        etName = v.findViewById(R.id.etEventName);
        etDesc = v.findViewById(R.id.etEventDescription);
        etCapacity = v.findViewById(R.id.etCapacity);
        etWaitlistCapacity = v.findViewById(R.id.etWaitlistCapacity);
        btnStart = v.findViewById(R.id.btnPickStart);
        btnEnd = v.findViewById(R.id.btnPickEnd);
        imgQr = v.findViewById(R.id.imgQrPreview);
        btnSaveQr = v.findViewById(R.id.btnSaveQr);
        btnEdit = v.findViewById(R.id.btnCreateEvent);
        btnUploadPoster = v.findViewById(R.id.btnUploadPoster);
        imgPosterPreview = v.findViewById(R.id.imgPosterPreview);

        btnSaveQr.setOnClickListener(view -> QrUtil.saveQrToGallery(qrBitmap, requireContext()));
        btnUploadPoster.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Opening image picker...", Toast.LENGTH_SHORT).show();
            pickImageLauncher.launch("image/*");
        });
        btnStart.setOnClickListener(view -> pickDate(true));
        btnEnd.setOnClickListener(view -> pickDate(false));
        btnEdit.setOnClickListener(view -> updateEvent());

        populateEventDetails();
        btnEdit.setText(R.string.event_details_edit_event_button);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity hostActivity = (AppCompatActivity) getActivity();
            if (hostActivity.getSupportActionBar() != null) {
                hostActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                if (event != null) {
                    hostActivity.getSupportActionBar().setTitle(R.string.event_details_edit_header);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity hostActivity = (AppCompatActivity) getActivity();
            if (hostActivity.getSupportActionBar() != null) {
                hostActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                hostActivity.getSupportActionBar().setTitle(R.string.entrant_nav_events_tab);
            }
        }
    }

    /**
     * Populates fields with the current event details
     */
    private void populateEventDetails() {
        Integer capacity = event.getCapacity();
        Integer waitlistCapacity = event.getWaitlistCapacity();
        startDate = event.getStartDate();
        endDate = event.getEndDate();
        String posterId = event.getPosterUrl();
        String qrPayload = event.getQrCodePayload();

        etName.setText(event.getName());
        etDesc.setText(event.getDescription());
        if (capacity != null) {
            etCapacity.setText(String.format(Locale.ENGLISH, "%d", capacity));
        }

        if (waitlistCapacity != null) {
            etWaitlistCapacity.setText(String.format(Locale.ENGLISH, "%d", waitlistCapacity));
        }

        if (startDate != null) {
            btnStart.setText(getString(
                    R.string.event_details_start_date_picker_button_filled,
                    DateUtil.formatDate(startDate)
            ));
        }
        if (endDate != null) {
            btnEnd.setText(getString(
                    R.string.event_details_end_date_picker_button_filled,
                    DateUtil.formatDate(endDate)
            ));
        }
        if (posterId != null) {
            loadPosterPreview(posterId);
            btnUploadPoster.setText(R.string.event_details_update_poster);
        }
        if (qrPayload != null) {
            loadQRPreview(qrPayload);
        } else {
            imgQr.setVisibility(View.GONE);
        }
    }

    /**
     * Loads and populates poster preview given non-null poster id
     * @param posterId poster id (not null)
     */
    private void loadPosterPreview(String posterId) {
        imageController.fetchImage(posterId, new com.google.android.gms.tasks.OnSuccessListener<Image>() {
            @Override
            public void onSuccess(Image img) {
                try {
                    if (img == null) {
                        return;
                    }
                    loadedPoster = img;
                    Bitmap bmp = img.decodeBase64ToBitmap();
                    if (bmp == null) {
                        Log.w("EditEventFragment", "decodeBase64ToBitmap returned null for id=" + posterId);
                        return;
                    }
                    // UI update on main thread
                    imgPosterPreview.post(() -> {
                        imgPosterPreview.setImageBitmap(bmp);
                        imgPosterPreview.setVisibility(View.VISIBLE);
                    });
                    Log.d("EditEventFragment", "Loaded poster for event=" + event.getId() + " posterId=" + posterId);
                } catch (Exception ex) {
                    Log.e("EditEventFragment", "Error while handling fetched image for id=" + posterId, ex);
                }
            }
        }, new com.google.android.gms.tasks.OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("EditEventFragment", "Failed to fetch image id=" + posterId, e);
                // keep the placeholder (update on UI thread)
                imgPosterPreview.post(() -> imgPosterPreview.setImageResource(android.R.drawable.ic_menu_report_image));
            }
        });
    }

    /**
     * Shows a material date picker and stores the chosen start/end date.
     */
    private void pickDate(boolean isStart) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setSelection(isStart ? startDate.getTime() : endDate.getTime())
                .build();
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
     * Displays a qr code with the provided payload in the QR preview
     * @param qrPayload payload to encode
     */
    private void loadQRPreview(String qrPayload) {
        try {
            qrBitmap = QrUtil.generateQr(qrPayload);
            imgQr.setImageBitmap(qrBitmap);
            imgQr.setVisibility(View.VISIBLE);
            btnSaveQr.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            Log.e("EditEventFragment", "Could not load QR code", e);
            imgQr.setVisibility(View.GONE);
            btnSaveQr.setVisibility(View.GONE);
        }
    }

    /**
     * Listener for when a new poster is picked by image picker
      * @param uri local URI to file
     */
    private void onPosterPicked(Uri uri) {
       if (uri == null) return;

       try {
           InputStream is = requireContext().getContentResolver().openInputStream(uri);
           Bitmap bmp = BitmapFactory.decodeStream(is);

           String fileName = ImageUtil.queryFileName(uri, requireContext());
           String mime = requireContext().getContentResolver().getType(uri);
           String base64 = ImageUtil.bitmapToBase64(bmp);
           if (mime == null) mime = "image/jpeg";
           if (fileName == null) fileName = "poster.jpg";

           loadedPoster = new Image(mime, fileName, base64);
           posterUpdated = true;

           imgPosterPreview.setImageBitmap(bmp);
           imgPosterPreview.setVisibility(View.VISIBLE);
       } catch (FileNotFoundException e) {
           Toast.makeText(requireContext(), "Failed to read image: " + e.getMessage(), Toast.LENGTH_LONG).show();
       }
    }

    /**
     * Listener for when update event is selected.
     * Attempts to update the event with all new information
     */
    private void updateEvent() {
        // Form validation
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name) || startDate == null || endDate == null) {
            Toast.makeText(requireContext(), "Name, start, and end date required!", Toast.LENGTH_LONG).show();
            return;
        }
        if (!startDate.before(endDate)) {
            Toast.makeText(requireContext(), "End date must be after start date!", Toast.LENGTH_LONG).show();
            return;
        }
        Integer capacity = getTextIntegerValue(etCapacity);
        if (capacity == null) {
            Toast.makeText(requireContext(), "Capacity required and must be an integer", Toast.LENGTH_LONG).show();
            return;
        }
        if (capacity < 0) {
            Toast.makeText(requireContext(), "Capacity must be a positive integer!", Toast.LENGTH_LONG).show();
            return;
        }

        Integer waitlistCapacity = getTextIntegerValue(etWaitlistCapacity);
        if (waitlistCapacity != null && waitlistCapacity < 0) {
            Toast.makeText(requireContext(), "Waitlist capacity must be a positive integer!", Toast.LENGTH_LONG).show();
            return;
        }
        if (waitlistCapacity != null && waitlistCapacity < capacity) {
            Toast.makeText(requireContext(), "Waitlist capacity must be larger than (or equal to) event capacity", Toast.LENGTH_LONG).show();
            return;
        }

        Event e = new Event();
        e.setId(event.getId()); // stays the same
        e.setPosterUrl(event.getPosterUrl()); // stays the same until new poster is uploaded
        e.setName(name);
        e.setDescription(etDesc.getText().toString().trim());
        e.setCapacity(capacity);
        e.setWaitlistCapacity(waitlistCapacity);
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        e.setOrganizerId(organizerId);

        btnEdit.setEnabled(false);
        eventController.updateEvent(event.getId(), e, eventId -> {
            if (loadedPoster != null && posterUpdated) {
                updateEventPoster();
            }
            // Note: QR payload will remain the same (event ID based)
            btnEdit.setEnabled(true);
            Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
            cancelFragment();
        }, err -> {
            Toast.makeText(requireContext(), "Event update failed: " + err.getMessage(), Toast.LENGTH_LONG).show();
            btnEdit.setEnabled(true);
        });
    }

    /**
     * Updates the event poster with the current loaded poster
     * Creates a new image and updates the event poster ID
     */
    private void updateEventPoster() {
        imageController.createImage(loadedPoster, imageId -> {
            eventController.updatePosterUrl(event.getId(), imageId,
                    v -> posterUpdated = false,
                    err -> Toast.makeText(requireContext(), "Event saved but poster upload failed: " + err.getMessage(), Toast.LENGTH_LONG).show()
            );
        }, err -> {
            Toast.makeText(requireContext(), "Event saved but poster upload failed: " + err.getMessage(), Toast.LENGTH_LONG).show();
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

    /**
     * Helper method to get the FragmentManager and remove this Fragment.
     */
    private void cancelFragment() {
        if (isAdded()) {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
           fragmentManager.popBackStack();
        }
    }
}
