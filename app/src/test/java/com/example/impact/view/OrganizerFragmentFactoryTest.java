package com.example.impact.view;

import androidx.fragment.app.Fragment;

import com.example.impact.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Verifies organizer tab selections map to the proper fragments.
 */
@RunWith(RobolectricTestRunner.class)
public class OrganizerFragmentFactoryTest {

    @Test
    public void createFragment_returnsEventsFragment() {
        Fragment fragment = OrganizerFragmentFactory.createFragment(R.id.organizer_nav_events, "org-1");
        assertThat(fragment, instanceOf(OrganizerEventListFragment.class));
    }
}
