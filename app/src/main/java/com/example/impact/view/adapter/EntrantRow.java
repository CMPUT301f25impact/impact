package com.example.impact.view.adapter;

/**
 * Lightweight view model for rendering entrant rows.
 */
public class EntrantRow {
    public final String name, email, joinedAt;

    /**
     * Builds a display row populated with entrant details.
     *
     * @param name     entrant display name
     * @param email    entrant email
     * @param joinedAt formatted date describing when they joined
     */
    public EntrantRow(String name, String email, String joinedAt) {
        this.name = name;
        this.email = email;
        this.joinedAt = joinedAt;
    }
}
