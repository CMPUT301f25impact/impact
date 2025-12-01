package com.example.impact;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.impact.view.AdminActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Ensures the Admin activity displays the Events list when launched.
 */
@RunWith(AndroidJUnit4.class)
public class AdminEventListUITest {

    @Rule
    public ActivityScenarioRule<AdminActivity> rule =
            new ActivityScenarioRule<>(AdminActivity.class);

    /**
     * Checks that a {@link RecyclerView} is visible on the Events tab, indicating the event list
     * is bound to the UI.
     */
    @Test
    public void testEventRecyclerVisible() {
        Espresso.onView(isAssignableFrom(RecyclerView.class))
                .check(ViewAssertions.matches(isDisplayed()));
    }
}
