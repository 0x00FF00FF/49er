package org.rares.miner49er;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.domain.users.model.UserData;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class HomeScrollingActivityTest {

    @Before
    public void setup() {
        ViewModelCacheSingleton.getInstance().clear();
        ViewModelCacheSingleton.getInstance().loggedInUser = null;
    }

    /*
     *  Given   application with no user logged in
     *  When    the activity is started
     *  Then    the login page is visible
     *  And     the Terms and Conditions and Privacy Policy container is visible
     *  And     the landing layout is visible
     *  And     the project list view is not visible
     */
    @Test
    public void testActivityLanding() {
        // given
        assertNull(ViewModelCacheSingleton.getInstance().loggedInUser);

        // when
        try (ActivityScenario<HomeScrollingActivity> scenario =
                     ActivityScenario.launch(HomeScrollingActivity.class)) {
            scenario.onActivity(activity -> {

                // then
                onView(withId(R.id.container_main_login)).check(matches(isCompletelyDisplayed()));
                onView(withId(R.id.tac_pp)).check(matches(isCompletelyDisplayed()));
                onView(withId(R.id.container)).check(matches(isCompletelyDisplayed()));
                onView(withId(R.id.scroll_views_container)).check(matches(not(isCompletelyDisplayed())));
            });
        }
    }

    /*
     *  Given   application with no user logged in
     *  When    the activity is started
     *  Then    the projects list is visible
     *  And     the login page does not exist
     *  And     the Terms and Conditions and Privacy Policy container does not exist
     *  And     the landing layout does not exist
     */
    @Test
    public void testActivityProjectsList() {
        // given
        UserData loggedInUser = new UserData();
        loggedInUser.id = 1L;
        loggedInUser.setName("Freddie Mercunary");

        ViewModelCacheSingleton.getInstance().loggedInUser = loggedInUser;

        // when
        try (ActivityScenario<HomeScrollingActivity> scenario =
                     ActivityScenario.launch(HomeScrollingActivity.class)) {
            scenario.onActivity(activity -> {

                // then
                onView(withId(R.id.container_main_login)).check(doesNotExist());
                onView(withId(R.id.tac_pp)).check(doesNotExist());
                onView(withId(R.id.container)).check(doesNotExist());
                onView(withId(R.id.scroll_views_container)).check(matches(isCompletelyDisplayed()));
            });
        }
    }

    /*
    * java.lang.IllegalStateException: Illegal connection pointer 1. Current pointers for thread Thread
    * db is not closed properly
    * robolectric issue
    */
}