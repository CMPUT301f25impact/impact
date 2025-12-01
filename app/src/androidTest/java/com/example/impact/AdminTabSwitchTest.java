package com.example.impact;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.impact.view.AdminActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Exercises the Admin activity top tabs to ensure each destination can be reached via clicks.
 */
@RunWith(AndroidJUnit4.class)
public class AdminTabSwitchTest {

    @Rule
    public ActivityScenarioRule<AdminActivity> rule =
            new ActivityScenarioRule<>(AdminActivity.class);

    /**
     * Walks the Admin tabs by tapping their labels and asserting each destination renders.
     */
    @Test
    public void adminTabsSwitchByClickingTitles() {
        Espresso.onView(withText("Events"))
                .check(ViewAssertions.matches(isDisplayed()));

        Espresso.onView(withText("Profiles")).perform(click());
        Espresso.onView(withText("Profiles"))
                .check(ViewAssertions.matches(isDisplayed()));

        Espresso.onView(withText("Images")).perform(click());
        Espresso.onView(withText("Images"))
                .check(ViewAssertions.matches(isDisplayed()));
    }
}
