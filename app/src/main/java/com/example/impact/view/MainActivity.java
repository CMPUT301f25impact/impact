package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.impact.R;

/**
 * Temporary launcher to navigate between entrant demo screens.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button entrantButton = findViewById(R.id.buttonGotoEntrant);
        Button adminButton = findViewById(R.id.buttonGotoAdmin);
        Button organizerBtn = findViewById(R.id.btnOrganizer);

        entrantButton.setOnClickListener(v -> startActivity(new Intent(this, EntrantActivity.class)));
        adminButton.setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));
        organizerBtn.setOnClickListener(v -> startActivity(new Intent(this, OrganizerActivity.class)));
    }
}
