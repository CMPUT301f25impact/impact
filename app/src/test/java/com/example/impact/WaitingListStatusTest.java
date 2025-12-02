package com.example.impact;

import com.example.impact.model.WaitingListEntry;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

public class WaitingListStatusTest {

    @Test
    public void waitingList_status_updatesCorrectly() {
        WaitingListEntry entry = new WaitingListEntry(
                "E1",
                "Swim Lessons",
                "U1",
                new Date(),
                "PENDING"
        );

        // accept
        entry.setStatus("ACCEPTED");
        assertEquals("ACCEPTED", entry.getStatus());

        // decline
        entry.setStatus("DECLINED");
        assertEquals("DECLINED", entry.getStatus());
    }
}
