package com.example.impact.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.time.LocalDateTime;

public class Notification implements Serializable {

    private User sender;
    private ArrayList<User> recipients;
    @Nullable
    private Event related_event;
    private String message;
//    private String sys_message; // I'm thinking we might want this to get specific with notification types
    private Date time_stamp;

    public Notification() {
        // Default constructor
    }

    public Notification(User sender, ArrayList<User> recipients, @Nullable Event related_event, String message) {
        this.sender = sender;
        this.recipients = recipients;
        this.related_event = related_event;
        this.message = message;
        this.time_stamp = new Date();
    }

    public ArrayList<User> getRecipients() {
        return recipients;
    }

    public Date getTime_stamp() {
        return time_stamp;
    }

    @Nullable
    public Event getRelated_event() {
        return related_event;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public User getSender() {
        return sender;
    }

    public String getPK() {
        return this.getSender().getEmail() + this.getTime_stamp().toString();
    }
}
