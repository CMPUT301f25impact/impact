package com.example.impact;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.impact.view.AdminActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AdminDeleteEventUITest {

    @Rule
    public ActivityScenarioRule<AdminActivity> rule =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Test
    public void testDeleteDialogShows() {
        // Click the first visible item in the first visible RecyclerView
        Espresso.onView(isAssignableFrom(RecyclerView.class))
                .perform(clickItemAtPosition(0));

        // Check a delete confirmation element appears (adjust text to your dialog label if needed)
        Espresso.onView(withText("Delete"))
                .check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Clicks the itemView of the ViewHolder at the given adapter position
     * without relying on espresso-contrib's RecyclerViewActions.
     */
    private static ViewAction clickItemAtPosition(final int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(RecyclerView.class);
            }

            @Override
            public String getDescription() {
                return "Click RecyclerView item at position " + position;
            }

            @Override
            public void perform(UiController uiController, View view) {
                RecyclerView recyclerView = (RecyclerView) view;
                // Ensure the target position is laid out
                recyclerView.scrollToPosition(position);
                uiController.loopMainThreadUntilIdle();

                RecyclerView.ViewHolder vh =
                        recyclerView.findViewHolderForAdapterPosition(position);

                if (vh == null) {
                    // Try once more after layout settles
                    uiController.loopMainThreadUntilIdle();
                    vh = recyclerView.findViewHolderForAdapterPosition(position);
                }
                if (vh != null) {
                    vh.itemView.performClick();
                    uiController.loopMainThreadUntilIdle();
                } else {
                    throw new AssertionError("No ViewHolder at position " + position);
                }
            }
        };
    }
}
