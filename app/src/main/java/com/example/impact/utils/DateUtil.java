package com.example.impact.utils;

import com.example.impact.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class containing utilities working with Date objects
 */
public class DateUtil {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    /**
     * Formats a date range for an event
     * @param event event
     * @return formatted date
     */
    public static String formatDateRange(Event event) {
        Date start = event.getStartDate();
        Date end = event.getEndDate();
        if (start != null && end != null) {
            return dateFormat.format(start) + " - " + dateFormat.format(end);
        }
        if (start != null) {
            return dateFormat.format(start);
        }
        if (end != null) {
            return dateFormat.format(end);
        }
        return "";
    }
}
