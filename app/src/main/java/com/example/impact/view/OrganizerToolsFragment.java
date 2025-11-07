package com.example.impact.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.impact.R;
import com.example.impact.controller.EventController;
import com.example.impact.model.Event;
import com.example.impact.utils.QrUtil;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.WriterException;

import java.util.Date;

public class OrganizerToolsFragment extends Fragment {

    private EditText etName, etDesc, etCapacity;
    private Button btnStart, btnEnd, btnCreate;
    private ImageView imgQr;
    private Date startDate, endDate;

    private final EventController controller = new EventController();

    // TODO: replace with real auth when available
    String organizerEmail = null;

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

        if (getArguments() != null) {
            organizerEmail = getArguments().getString("organizerEmail");
        }

        if (organizerEmail == null) {
            Toast.makeText(requireContext(), "Organizer email missing", Toast.LENGTH_SHORT).show();
            btnCreate.setEnabled(false);
        } else {
            btnCreate.setEnabled(true);
        }


        btnStart.setOnClickListener(view -> pickDate(true));
        btnEnd.setOnClickListener(view -> pickDate(false));
        btnCreate.setOnClickListener(view -> createEvent());
        return v;
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

        btnCreate.setEnabled(false);

        controller.createEvent(e, eventId -> {
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
