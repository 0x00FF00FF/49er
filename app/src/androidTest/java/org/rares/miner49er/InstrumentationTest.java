package org.rares.miner49er;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle.State;
import org.junit.Before;
import org.junit.Test;
import org.rares.miner49er.BaseInterfaces.ActionFragmentDependencyProvider;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.entries.ui.actions.add.TimeEntryAddFormFragment;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.fakes.AsyncGenericDaoFake;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.actionmode.ActionEnforcer.FragmentResultListener;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.rares.miner49er.domain.issues.IssuesInterfaces.KEY_ISSUE_ID;
import static org.rares.miner49er.testutils.TestUtil.getResourceInt;

//import org.robolectric.Robolectric;

public class InstrumentationTest {
    private TEAFF_Provider provider;
    private UserData loggedInUser;
    private ProjectData projectData;
    private IssueData issueData;
    private TEAFF_Factory fragmentFactory;
    private String comments;

    private final int minHours = getResourceInt(R.integer.min_hours);
    private final int maxHours = getResourceInt(R.integer.max_hours);
    private final int maxCharacters = getResourceInt(R.integer.comment_max_length);
    @Before
    public void setup() {
        comments = "Captain's Log, Stardate 43125.8. " +
                "We have entered a spectacular binary star system in the Kavis Alpha sector...";

        provider = new TEAFF_Provider();

        loggedInUser = new UserData();
        loggedInUser.id = 1L;
        loggedInUser.setName("J.L Picard");
        provider.cache.loggedInUser = loggedInUser;

        projectData = new ProjectData();
        projectData.id = 1L;
        projectData.setName("Tony Selevision");
        projectData.setOwner(loggedInUser);

        issueData = new IssueData();
        issueData.id = 1L;
        issueData.setName("Headphones not working.");
        issueData.setOwnerId(loggedInUser.id);
        issueData.parentId = projectData.id;

        provider.fakePdao.object = projectData;
        provider.fakeIdao.object = issueData;
        provider.fakeUdao.object = loggedInUser;

        Bundle args = new Bundle();
        args.putLong(KEY_ISSUE_ID, 1);

        fragmentFactory = new TEAFF_Factory(provider);

        FragmentScenario<TimeEntryAddFormFragment> scenario =
                FragmentScenario.launchInContainer(TimeEntryAddFormFragment.class, args, R.style.AppTheme, fragmentFactory);

        scenario.moveToState(State.RESUMED);
    }

    /*
     *  Given   a new time entry being added
     *  When    the time entry edit text is clicked
     *  Then    the date picker is shown
     */
    @Test
    public void testChangeDate() throws InterruptedException {
        onView(withId(R.id.work_date_edit_text)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.mdtp_date_picker_header)).inRoot(isDialog()).check(matches(isCompletelyDisplayed()));
        Thread.sleep(3000);
        onView(withId(R.id.mdtp_day_picker_selected_date_layout)).inRoot(isDialog()).check(matches(isCompletelyDisplayed()));
        Thread.sleep(3000);
        onView(withId(R.id.mdtp_date_picker_month_and_day)).inRoot(isDialog()).check(matches(isCompletelyDisplayed()));
        Thread.sleep(3000);
        onView(withId(R.id.mdtp_date_picker_month)).inRoot(isDialog()).check(matches(isCompletelyDisplayed()));
        Thread.sleep(3000);
        onView(withId(R.id.mdtp_date_picker_day)).inRoot(isDialog()).check(matches(isDisplayed()));
        Thread.sleep(3000);
        onView(withId(R.id.mdtp_date_picker_year)).inRoot(isDialog()).check(matches(isCompletelyDisplayed()));
        Thread.sleep(3000);
        onView(withId(R.id.mdtp_ok)).inRoot(isDialog()).check(matches(isCompletelyDisplayed()));
        Thread.sleep(3000);
        onView(withId(R.id.mdtp_cancel)).inRoot(isDialog()).check(matches(isCompletelyDisplayed()));
        Thread.sleep(3000);
        onView(withId(R.id.mdtp_cancel)).inRoot(isDialog()).perform(click());
    }

    @Test
    public void testSnackbarFailUndoAdd() throws InterruptedException {
        provider.fakeTEdao.booleanToReturn = false;
        assertNull(provider.fakeTEdao.object);

        // when
        onView(withId(R.id.project_name_edit_text)).check(matches(withText(projectData.getName())));
        onView(withId(R.id.issue_name_edit_text)).check(matches(withText(issueData.getName())));

        String hours = "1";

        onView(withId(R.id.hours_edit_text)).perform(replaceText(hours), closeSoftKeyboard());
        onView(withId(R.id.comments_edit_text)).perform(replaceText(comments), closeSoftKeyboard());

        onView(withId(R.id.hours_edit_text)).check(matches(withText(hours)));
        onView(withId(R.id.comments_edit_text)).check(matches(withText(comments)));

        onView(withId(R.id.btn_add)).perform(scrollTo()).perform(click());

        TimeEntryData timeEntryData = provider.fakeTEdao.object;

        assertNotNull(timeEntryData);

        assertEquals(comments, timeEntryData.getComments());
        assertEquals(Integer.valueOf(hours).intValue(), timeEntryData.getHours());
        assertEquals(issueData.id, timeEntryData.parentId);

        onView(withText(R.string.success_time_entry_add)).check(matches(isCompletelyDisplayed()));
        onView(withText(R.string.action_undo)).perform(click());

        Thread.sleep(500);
        // then
        assertEquals(timeEntryData.id, provider.fakeTEdao.object.id);
        onView(withText(R.string.err_entry_not_removed)).check(matches(isCompletelyDisplayed()));
    }

    private class TEAFF_Factory extends FragmentFactory {
        final ActionFragmentDependencyProvider provider;
        Fragment fragment = null;

        TEAFF_Factory(ActionFragmentDependencyProvider provider) {
            this.provider = provider;
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
            Class clazz = loadFragmentClass(classLoader, className);

            if (clazz == TimeEntryAddFormFragment.class) {
                fragment = new TimeEntryAddFormFragment(provider);
            } else {
                fragment = super.instantiate(classLoader, className);
            }

            return fragment;
        }
    }

    private class TEAFF_Provider implements ActionFragmentDependencyProvider {
        AsyncGenericDaoFake<ProjectData> fakePdao = new AsyncGenericDaoFake<>();
        AsyncGenericDaoFake<IssueData> fakeIdao = new AsyncGenericDaoFake<>();
        AsyncGenericDaoFake<TimeEntryData> fakeTEdao = new AsyncGenericDaoFake<>();
        AsyncGenericDaoFake<UserData> fakeUdao = new AsyncGenericDaoFake<>();
        ViewModelCache cache = ViewModelCacheSingleton.getInstance();

        @Override
        public ViewModelCache getCache() {
            return cache;
        }

        @Override
        public Cache<ProjectData> getProjectDataCache() {
            return cache.getCache(ProjectData.class);
        }

        @Override
        public Cache<IssueData> getIssueDataCache() {
            return cache.getCache(IssueData.class);
        }

        @Override
        public Cache<TimeEntryData> getTimeEntryDataCache() {
            return cache.getCache(TimeEntryData.class);
        }

        @Override
        public Cache<UserData> getUserDataCache() {
            return cache.getCache(UserData.class);
        }

        @Override
        public AsyncGenericDao<ProjectData> getProjectsDAO() {
            return fakePdao;
        }

        @Override
        public AsyncGenericDao<IssueData> getIssuesDAO() {
            return fakeIdao;
        }

        @Override
        public AsyncGenericDao<TimeEntryData> getTimeEntriesDAO() {
            return fakeTEdao;
        }

        @Override
        public AsyncGenericDao<UserData> getUsersDAO() {
            return fakeUdao;
        }

        @Override
        public View getReplacedView() {
//            HomeScrollingActivity activity = Robolectric.buildActivity(HomeScrollingActivity.class).create().get();
//            View toReturn = activity.findViewById(R.id.scroll_views_container);
            return null;
        }

        @Override
        public FragmentResultListener getResultListener() {
            return new FragmentResultListener() {
                @Override
                public void onFragmentDismiss() {
                    System.out.println("Fragment dismiss called.");
                }
            };
        }
    }

}
