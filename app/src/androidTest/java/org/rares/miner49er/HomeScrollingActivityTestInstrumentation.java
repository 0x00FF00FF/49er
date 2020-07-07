package org.rares.miner49er;

import android.content.ComponentName;
import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.storio.StorioFactory;

import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class HomeScrollingActivityTestInstrumentation {

    @Rule
    public ActivityTestRule<HomeScrollingActivity> activityTestRule = new ActivityTestRule<>(HomeScrollingActivity.class);

    @After
    public void cleanup() throws IOException {
        StorioFactory.INSTANCE.get().close();
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
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(HomeScrollingActivity.class.getPackage().getName(), HomeScrollingActivity.class.getName()));
        activityTestRule.launchActivity(intent);

        // then
        onView(withId(R.id.container_main_login)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.tac_pp)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.container)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.scroll_views_container)).check(matches(not(isCompletelyDisplayed())));
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
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(HomeScrollingActivity.class.getPackage().getName(), HomeScrollingActivity.class.getName()));
        activityTestRule.launchActivity(intent);

        // then
        onView(withId(R.id.container_main_login)).check(doesNotExist());
        onView(withId(R.id.tac_pp)).check(doesNotExist());
        onView(withId(R.id.container)).check(doesNotExist());
        onView(withId(R.id.scroll_views_container)).check(matches(isCompletelyDisplayed()));
    }
}