package com.example.impact.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

public class Invitation extends Notification {
    private Event related_event; // Not nullable any more
    public Invitation() {
        // Default constructor
    }
    public Invitation(User sender, ArrayList<User> recipients, Event related_event, String message) {
        super(sender, recipients, related_event, message);
    }
}
