package org.rares.miner49er.domain.entries.ui.actions.add;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle.State;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.BaseInterfaces.ActionFragmentDependencyProvider;
import org.rares.miner49er.HomeScrollingActivity;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.fakes.AsyncGenericDaoFake;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.actionmode.ActionEnforcer.FragmentResultListener;
import org.robolectric.Robolectric;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.rares.miner49er.domain.issues.IssuesInterfaces.KEY_ISSUE_ID;

@RunWith(AndroidJUnit4.class)
public class TimeEntryAddFormFragmentTest {

    /*
     *  Given   a new TimeEntryAddFormFragment
     *  When    correct time entry data is present
     *  And     SAVE action is triggered
     *  Then    the time entry data will be persisted
     */
    @Test
    public void testSaveTimeEntry() {

        TEAFF_Provider provider = new TEAFF_Provider();

        UserData loggedInUser = new UserData();
        loggedInUser.id = 1L;
        loggedInUser.setName("Ron Crox");
        provider.cache.loggedInUser = loggedInUser;

        ProjectData projectData = new ProjectData();
        projectData.id = 1L;
        projectData.setName("Tony Selevision");
        projectData.setOwner(loggedInUser);

        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issueData.setName("Headphones not working.");
        issueData.setOwnerId(loggedInUser.id);
        issueData.parentId = projectData.id;

        provider.fakePdao.object = projectData;
        provider.fakeIdao.object = issueData;
        provider.fakeUdao.object = loggedInUser;

        Bundle args = new Bundle();
        args.putLong(KEY_ISSUE_ID, 1);

        TEAFF_Factory fragmentFactory = new TEAFF_Factory(provider);

        FragmentScenario<TimeEntryAddFormFragment> scenario =
                FragmentScenario.launchInContainer(TimeEntryAddFormFragment.class, args, R.style.AppTheme, fragmentFactory);

        TimeEntryAddFormFragment fragment = (TimeEntryAddFormFragment) fragmentFactory.fragment;

        scenario.moveToState(State.RESUMED);

        onView(withId(R.id.project_name_edit_text)).check(matches(withText(projectData.getName())));
        onView(withId(R.id.issue_name_edit_text)).check(matches(withText(issueData.getName())));

        String comments = "I won't be able to see what I write here.";
        String hours = "1";

        onView(withId(R.id.hours_edit_text)).perform(replaceText(hours), closeSoftKeyboard());
        onView(withId(R.id.comments_edit_text)).perform(replaceText(comments), closeSoftKeyboard());

        onView(withId(R.id.hours_edit_text)).check(matches(withText(hours)));
        onView(withId(R.id.comments_edit_text)).check(matches(withText(comments)));

        onView(withId(R.id.btn_add)).perform(click());

        TimeEntryData timeEntryData = provider.fakeTEdao.object;

        assertNotNull(timeEntryData);

        assertEquals(comments, timeEntryData.getComments());
        assertEquals(Integer.valueOf(hours).intValue(), timeEntryData.getHours());
        assertEquals(issueData.id, timeEntryData.parentId);
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
            HomeScrollingActivity activity = Robolectric.buildActivity(HomeScrollingActivity.class).create().get();
            View toReturn = activity.findViewById(R.id.scroll_views_container);
            System.out.println("get replaced view: " + toReturn);
            return toReturn;
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