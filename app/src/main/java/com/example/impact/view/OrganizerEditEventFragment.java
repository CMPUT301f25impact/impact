package com.example.impact.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.example.impact.utils.QrUtil;
import com.google.zxing.WriterException;

import java.util.Date;
import java.util.Locale;

/**
 * Provides Organizers with a UI to edit event details after it's creation
 */
public class OrganizerEditEventFragment extends Fragment {

    public static final String EXTRA_ORGANIZER_ID = "organizer_id";
    public static final String EXTRA_EVENT = "event";

    private EditText etName, etDesc, etCapacity;
    private Button btnStart, btnEnd, btnEdit, btnUploadPoster;
    private ImageView imgQr;
    private Date startDate, endDate;
    private ImageView imgPosterPreview;

    private Event event;
    private String organizerId;

    private final EventController eventController = new EventController();
    private final ImageController imageController = new ImageController();

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
        btnStart = v.findViewById(R.id.btnPickStart);
        btnEnd = v.findViewById(R.id.btnPickEnd);
        imgQr = v.findViewById(R.id.imgQrPreview);
        btnEdit = v.findViewById(R.id.btnCreateEvent);
        btnUploadPoster = v.findViewById(R.id.btnUploadPoster);
        imgPosterPreview = v.findViewById(R.id.imgPosterPreview);

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
        Date startDate = event.getStartDate();
        Date endDate = event.getEndDate();
        String posterId = event.getPosterUrl();
        String qrPayload = event.getQrCodePayload();

        etName.setText(event.getName());
        etDesc.setText(event.getDescription());

        if (capacity != null) {
            etCapacity.setText(String.format(Locale.ENGLISH, "%d", capacity));
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
                    final Bitmap bmp = img.decodeBase64ToBitmap();
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

    private void loadQRPreview(String qrPayload) {
        try {
            Bitmap bmp = QrUtil.generateQr(qrPayload);
            imgQr.setImageBitmap(bmp);
        } catch (WriterException e) {
            Log.e("EditEventFragment", "Could not load QR code", e);
            imgQr.setVisibility(View.GONE);
        }
    }

    /**
     * Helper method to get the FragmentManager and remove this Fragment.
     */
    private void cancelFragment() {
        if (isAdded()) {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .remove(this)
                    .commit();
        }
    }
}
