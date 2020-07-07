package org.rares.miner49er.domain.entries.ui.actions.add;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle.State;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rares.miner49er.BaseInterfaces.ActionFragmentDependencyProvider;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.fakes.ActionFragmentDependencyProviderFake;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.DATE_PATTERN;
import static org.rares.miner49er.domain.issues.IssuesInterfaces.KEY_ISSUE_ID;
import static org.rares.miner49er.testutils.Matchers.textInputLayoutWithError;
import static org.rares.miner49er.testutils.TestUtil.getResourceInt;
import static org.rares.miner49er.testutils.TestUtil.getResourceString;

@RunWith(AndroidJUnit4.class)
public class TimeEntryAddFormFragmentTest {

    private ActionFragmentDependencyProviderFake provider;
    private UserData loggedInUser;
    private ProjectData projectData;
    private IssueData issueData;
    private TEAFF_Factory fragmentFactory;
    private String comments;

    @Mock
    private View replacedView;

    private final int minHours = getResourceInt(R.integer.min_hours);
    private final int maxHours = getResourceInt(R.integer.max_hours);
    private final int maxCharacters = getResourceInt(R.integer.comment_max_length);

//    @Rule
//    public GrantPermissionRule permissionRule = GrantPermissionRule.grant("android.permission.DISABLE_KEYGUARD");

    @Before
    public void setup() {

//        KeyguardManager mKeyGuardManager =
//                (KeyguardManager) InstrumentationRegistry.getInstrumentation()
//                        .getTargetContext().getSystemService(Context.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock mLock = mKeyGuardManager.newKeyguardLock("<name>");
//        mLock.disableKeyguard();

        MockitoAnnotations.initMocks(this);

        comments = "Captain's Log, Stardate 43125.8. " +
                "We have entered a spectacular binary star system in the Kavis Alpha sector...";

        provider = new ActionFragmentDependencyProviderFake();
        provider.replacedView = replacedView;

//      HomeScrollingActivity activity = Robolectric.buildActivity(HomeScrollingActivity.class).create().get();
//      provider.replacedView = activity.findViewById(R.id.scroll_views_container);

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
     *  Given   a new TimeEntryAddFormFragment
     *  When    correct time entry data is present
     *  And     SAVE action is triggered
     *  Then    the time entry data will be persisted
     */
    @Test
    public void testSaveTimeEntry() {

        // given [setup]

        // when
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

        // then
        assertNotNull(timeEntryData);

        assertEquals(comments, timeEntryData.getComments());
        assertEquals(Integer.valueOf(hours).intValue(), timeEntryData.getHours());
        assertEquals(issueData.id, timeEntryData.parentId);
    }

    /*
     *  Given   a new TimeEntryAddFormFragment
     *  When    it is ready to use
     *  Then    the projectName is correct, edit text is not enabled
     *  And     the issueName is correct, edit text is not enabled
     *  And     the time entry owner name is correct, edit text is not enabled, edit text is enabled
     *  And     nothing is present in 'hours worked' field, edit text is enabled
     *  And     the current day is displayed in the 'time entry work date' field, edit text is enabled
     *  And     no text is present in 'comments' section
     *  And     the current day is displayed in the 'date added' field, edit text is not enabled
     *      *   the date format is 'Mon, 10 June, 2019'
     */
    @Test
    public void testFormDefaults() {

        // given, when [setup]
        String date = DateTime.now().toString(DATE_PATTERN);

        // then
        onView(withId(R.id.project_name_edit_text))
                .check(matches(withText(projectData.getName())))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.issue_name_edit_text))
                .check(matches(withText(issueData.getName())))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.owner_edit_text))
                .check(matches(withText(loggedInUser.getName())))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.hours_edit_text))
                .check(matches(withText("")))
                .check(matches(isEnabled()));
        onView(withId(R.id.comments_edit_text))
                .check(matches(withText("")))
                .check(matches(isEnabled()));
        onView(withId(R.id.work_date_edit_text))
                .check(matches(withText(date)))
                .check(matches(isEnabled()));
        onView(withId(R.id.date_added_edit_text))
                .check(matches(withText(date)))
                .check(matches(not(isEnabled())));
    }

    /*
     *  Given   a new TimeEntryAddFormFragment with no data in the 'hours worked' field
     *  When    a user attempts to save the time entry
     *  Then    an error message appears [informing that 'hours worked' needs to be completed]
     *  And     the time entry is not saved
     */
    @Test
    public void testValidationFail_HoursWorkedNotEntered() {
        // given [setup]

        // when
        onView(withId(R.id.comments_edit_text)).perform(replaceText(comments), closeSoftKeyboard());
        onView(withId(R.id.btn_add)).perform(click());

        // then
        TimeEntryData timeEntryData = provider.fakeTEdao.object;
        assertNull(timeEntryData);

        String error = String.format(getResourceString(R.string.error_time_entry_hours), minHours, maxHours);

        onView(withId(R.id.hours_input_layout)).check(matches(textInputLayoutWithError(withId(R.id.hours_input_layout), error)));
    }

    /*
     *  Given   a time entry form
     *  When    a user inserts more than 16 hours in the 'hours worked' section
     *  And     attempts to save the time entry
     *  Then    the time entry form will display an error message
     *  And     the time entry will not be saved
     */
    @Test
    public void testValidationFail_HoursWorked_MoreThanMax() {
        // given [setup]

        // when
        onView(withId(R.id.hours_edit_text)).perform(replaceText(String.valueOf(maxHours + 1)), closeSoftKeyboard());
        onView(withId(R.id.comments_edit_text)).perform(replaceText(comments), closeSoftKeyboard());
        onView(withId(R.id.btn_add)).perform(click());

        // then
        TimeEntryData timeEntryData = provider.fakeTEdao.object;
        assertNull(timeEntryData);
        String error = String.format(getResourceString(R.string.error_time_entry_hours), minHours, maxHours);

        onView(withId(R.id.hours_input_layout)).check(matches(textInputLayoutWithError(withId(R.id.hours_input_layout), error)));
    }

    /*
     *  Given   a user with 16 hours already inserted for the day
     *  When    he tries to add another time entry
     *  And     attempts to save the time entry
     *  Then    the time entry form will display an error message
     *  And     the time entry will not be saved
     */
    @Test
    public void testValidationFail_HoursWorked_AddMoreThanMax_AlreadyEntered() {
        // given [setup]
        List<TimeEntryData> list = new ArrayList<>();
        TimeEntryData ted1 = new TimeEntryData();
        ted1.id = 1L;
        ted1.setHours(maxHours / 2);
        TimeEntryData ted2 = new TimeEntryData();
        ted2.setHours(maxHours / 2);
        ted2.id = 2L;
        list.add(ted1);
        list.add(ted2);

        provider.fakeTEdao.list = list;

        // when
        onView(withId(R.id.comments_edit_text)).perform(replaceText(comments), closeSoftKeyboard());
        onView(withId(R.id.btn_add)).perform(click());

        // then
        TimeEntryData timeEntryData = provider.fakeTEdao.object;
        assertNull(timeEntryData);
        String error = String.format(getResourceString(R.string.error_time_entry_hours), minHours, maxHours);

        int viewId = R.id.hours_input_layout;
        onView(withId(viewId)).check(matches(textInputLayoutWithError(withId(viewId), error)));
    }

    /*
     *  Given   a user entering time information
     *  When    the user inserts more than 256 characters in the comments section
     *  And     attempts to save the time entry
     *  Then    the time entry form will display an error message
     *  And     the time entry will not be saved
     */
    @Test
    public void testValidationFail_LargerComments() {
        // given [setup]
        while (comments.length() < maxCharacters) {
            comments += comments;
        }
        // when
        onView(withId(R.id.comments_edit_text)).perform(replaceText(comments), closeSoftKeyboard());
        onView(withId(R.id.btn_add)).perform(click());

        // then
        TimeEntryData timeEntryData = provider.fakeTEdao.object;
        assertNull(timeEntryData);
        String error = String.format(getResourceString(R.string.error_field_number_of_characters), minHours, maxHours);

        int viewId = R.id.comments_input_layout;
        onView(withId(viewId)).check(matches(textInputLayoutWithError(withId(viewId), error)));
    }

    /*
     *  Given   a user saving a time entry with no errors
     *  When    the time entry is saved
     *  Then    a snackbar is displayed informing the user
     *          about the operation completion status
     *          [the time entry has been successfully inserted]
     */
    @Test
    public void testSnackbarAppearing() {
        // when
        onView(withId(R.id.project_name_edit_text)).check(matches(withText(projectData.getName())));
        onView(withId(R.id.issue_name_edit_text)).check(matches(withText(issueData.getName())));

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

        // then
        onView(withText(R.string.success_time_entry_add)).check(matches(isCompletelyDisplayed()));
    }

    /*          fixme | cannot find the second snackbar - works in instrumentation test
     *  Given   a user saves a time entry with no errors
     *  When    the snackbar is displayed
     *  And     the user presses the undo button on the snackbar
     *  Then    the time entry is deleted
     *  And     another snackbar is displayed informing the user
     *          about the operation completion status
     *          [the time entry has been successfully deleted]
     */
    @Test
    public void testSnackbarUndoAdd() {
        provider.fakeTEdao.booleanToReturn = true;
        assertNull(provider.fakeTEdao.object);

        // when
        onView(withId(R.id.project_name_edit_text)).check(matches(withText(projectData.getName())));
        onView(withId(R.id.issue_name_edit_text)).check(matches(withText(issueData.getName())));

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

        onView(withText(R.string.success_time_entry_add)).check(matches(isCompletelyDisplayed()));
        onView(withText(R.string.action_undo)).perform(click());

        // then

//        Thread.sleep(500);
        assertEquals(timeEntryData.id, provider.fakeTEdao.object.id);
//        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isCompletelyDisplayed()));
//        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.entry_removed)));
    }

    /*          fixme | cannot find the second snackbar - works in instrumentation test
     *  Given   a user saves a time entry with no errors
     *  When    the snackbar is displayed
     *  And     the user presses the undo button on the snackbar
     *  And     the dao could not complete the request
     *  Then    the time entry is not deleted
     *  And     another snackbar is displayed informing the user
     *          about the operation completion status
     *          [the time entry has not been successfully deleted]
     */
    @Test
    public void testSnackbarFailUndoAdd() {
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

        onView(withId(R.id.btn_add)).perform(click());

        TimeEntryData timeEntryData = provider.fakeTEdao.object;

        assertNotNull(timeEntryData);

        assertEquals(comments, timeEntryData.getComments());
        assertEquals(Integer.valueOf(hours).intValue(), timeEntryData.getHours());
        assertEquals(issueData.id, timeEntryData.parentId);

        onView(withText(R.string.success_time_entry_add)).check(matches(isCompletelyDisplayed()));
        onView(withText(R.string.action_undo)).perform(click());

        // then
        assertEquals(timeEntryData.id, provider.fakeTEdao.object.id);
//        onView(withText(R.string.err_entry_not_removed)).check(matches(isCompletelyDisplayed()));

    }

    /*
     *  Given   nothing
     *  When    a TimeEntryAddFormFragment is created using newInstance
     *  Then    a new TimeEntryAddFormFragment is created
     */
    @Test
    public void testNewInstance() {
        TimeEntryAddFormFragment fragment = TimeEntryAddFormFragment.newInstance();
        final int fragmentHashCode = fragment.hashCode();
        assertNotEquals(fragmentHashCode, TimeEntryAddFormFragment.newInstance());
    }

    /*
     *  Given   nothing
     *  When    TimeEntryAddFormFragment getTag is called
     *  Then    "TimeEntryAddFormFragment" is returned.
     */
    @Test
    public void testGetTag() {
        assertEquals(TimeEntryAddFormFragment.class.getSimpleName(),
                ((TimeEntryAddFormFragment) fragmentFactory.fragment).getActionTag());
    }

    /*  fixme | needs instrumentation test
     *  Given   a new time entry being added
     *  When    the time entry edit text is clicked
     *  Then    the date picker is shown
     */
    @Test
    public void testChangeDate() {
//        onView(withId(R.id.hours_edit_text)).perform(click()).perform(pressKey(KeyEvent.KEYCODE_TAB));
        ViewInteraction workDate = onView(withId(R.id.work_date_edit_text));
        workDate.check(matches(isDisplayed()));
        workDate.perform(click());
//        workDate.check(matches(not(hasFocus())));

//        onView(withId(R.id.mdtp_cancel)).inRoot(isDialog()).perform(click());
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

}