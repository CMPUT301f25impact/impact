package com.example.impact;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.impact.view.LoginActivity;
import com.example.impact.view.MainActivity;
import com.example.impact.view.EventListActivity;
import com.example.impact.view.EventHistoryActivity;
import com.example.impact.view.OrganizerActivity;
import com.example.impact.view.AdminActivity;
import com.example.impact.view.WaitingListActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ActivitySmokeTest {

    @Rule public ActivityScenarioRule<MainActivity> main =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule public ActivityScenarioRule<LoginActivity> login =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Rule public ActivityScenarioRule<EventListActivity> list =
            new ActivityScenarioRule<>(EventListActivity.class);

    @Rule public ActivityScenarioRule<EventHistoryActivity> history =
            new ActivityScenarioRule<>(EventHistoryActivity.class);

    @Rule public ActivityScenarioRule<OrganizerActivity> organizer =
            new ActivityScenarioRule<>(OrganizerActivity.class);

    @Rule public ActivityScenarioRule<AdminActivity> admin =
            new ActivityScenarioRule<>(AdminActivity.class);

    @Rule public ActivityScenarioRule<WaitingListActivity> waiting =
            new ActivityScenarioRule<>(WaitingListActivity.class);

    @Test
    public void launchers_NoCrash() {
        // If any activity crashes on launch, test fails automatically.
    }
}
