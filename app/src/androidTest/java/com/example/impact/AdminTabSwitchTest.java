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
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

@RunWith(AndroidJUnit4.class)
public class AdminTabSwitchTest {

    @Rule
    public ActivityScenarioRule<AdminActivity> rule =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Test
    public void adminTabsSwitchByClickingTitles() {
        // Start on first tab ("Events")
        Espresso.onView(withText("Events"))
                .check(ViewAssertions.matches(isDisplayed()));

        // Click to "Profiles"
        Espresso.onView(withText("Profiles")).perform(click());
        Espresso.onView(withText("Profiles"))
                .check(ViewAssertions.matches(isDisplayed()));

        // Click to "Images"
        Espresso.onView(withText("Images")).perform(click());
        Espresso.onView(withText("Images"))
                .check(ViewAssertions.matches(isDisplayed()));
    }
}
