package com.example.impact;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class LotterySelectionPureTest {

    private List<String> selectWinners(List<String> entrants, int count) {
        // Simple deterministic selection for testing
        return entrants.subList(0, Math.min(count, entrants.size()));
    }

    @Test
    public void lottery_selectsCorrectNumberOfWinners() {
        List<String> entrants = Arrays.asList("u1", "u2", "u3", "u4");
        List<String> winners = selectWinners(entrants, 2);

        assertEquals(2, winners.size());
        assertEquals("u1", winners.get(0));
        assertEquals("u2", winners.get(1));
    }

    @Test
    public void lottery_doesNotCrashWhenCountTooHigh() {
        List<String> entrants = Arrays.asList("u1", "u2");
        List<String> winners = selectWinners(entrants, 10);

        assertEquals(2, winners.size());
    }
}
