package org.rares.miner49er.domain.entries.ui.actions.edit;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle.State;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.hamcrest.Matcher;
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
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.DATE_PATTERN;
import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.KEY_TIME_ENTRY_ID;
import static org.rares.miner49er.testutils.Matchers.textInputLayoutWithError;
import static org.rares.miner49er.testutils.TestUtil.getResourceInt;
import static org.rares.miner49er.testutils.TestUtil.getResourceString;

@RunWith(AndroidJUnit4.class)
public class TimeEntryEditFormFragmentTest {

    private ActionFragmentDependencyProviderFake provider;
    private UserData loggedInUser;
    private ProjectData projectData;
    private IssueData issueData;
    private TimeEntryData timeEntryData;
    private AFragmentFactory fragmentFactory;
    private String comments;

    @Mock
    private View replacedView;

    private final int minHours = getResourceInt(R.integer.min_hours);
    private final int maxHours = getResourceInt(R.integer.max_hours);
    private final int maxCharacters = getResourceInt(R.integer.comment_max_length);

    @Before
    public void setup() {

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

        timeEntryData = new TimeEntryData();
        timeEntryData.id = 1L;
        timeEntryData.setHours(1);
        timeEntryData.setUserId(loggedInUser.id);
        timeEntryData.setWorkDate(System.currentTimeMillis());
        timeEntryData.setDateAdded(System.currentTimeMillis());
        timeEntryData.setDeleted(false);
        timeEntryData.setUserName(loggedInUser.getName());
        timeEntryData.setUserPhoto(loggedInUser.getPicture());
        timeEntryData.parentId = issueData.id;

        provider.fakePdao.object = projectData;
        provider.fakeIdao.object = issueData;
        provider.fakeTEdao.object = timeEntryData;
        provider.fakeUdao.object = loggedInUser;

        Bundle args = new Bundle();
        args.putLong(KEY_TIME_ENTRY_ID, timeEntryData.id);

        fragmentFactory = new AFragmentFactory(provider);

        FragmentScenario<TimeEntryEditFormFragment> scenario =
                FragmentScenario.launchInContainer(TimeEntryEditFormFragment.class, args, R.style.AppTheme, fragmentFactory);

        scenario.moveToState(State.RESUMED);
    }

    /*
     *  Given   a new TimeEntryEditFormFragment with existing timeEntry data that has no comments
     *  When    correct time entry data is present
     *  And     SAVE action is triggered
     *  Then    the time entry data will be persisted
     */
    @Test
    public void testSaveTimeEntry() {

        // given [setup]
        assertNull(provider.fakeTEdao.object.getComments());
        Matcher<View> commentsEditTextById = withId(R.id.comments_edit_text);
        onView(commentsEditTextById).check(matches(withText("")));

        // when
        onView(withId(R.id.project_name_edit_text)).check(matches(withText(projectData.getName())));
        onView(withId(R.id.issue_name_edit_text)).check(matches(withText(issueData.getName())));

        final String comments = "I won't be able to see what I write here.";
        final int hours = 1;
        final String hrs = String.valueOf(hours);

        onView(withId(R.id.hours_edit_text)).perform(replaceText(hrs), closeSoftKeyboard());
        onView(withId(R.id.comments_edit_text)).perform(replaceText(comments), closeSoftKeyboard());

        onView(withId(R.id.hours_edit_text)).check(matches(withText(hrs)));
        onView(withId(R.id.comments_edit_text)).check(matches(withText(comments)));

        onView(withId(R.id.btn_add)).perform(click());

        TimeEntryData timeEntryData = provider.fakeTEdao.object;

        // then
        assertNotNull(timeEntryData);

        assertEquals(comments, timeEntryData.getComments());
        assertEquals(hours, timeEntryData.getHours());
        assertEquals(issueData.id, timeEntryData.parentId);

        assertNotEquals(this.timeEntryData, timeEntryData);
    }

    /*
     *  Given   a new TimeEntryEditFormFragment
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
                .check(matches(withText(timeEntryData.getUserName())))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.hours_edit_text))
                .check(matches(withText(String.valueOf(timeEntryData.getHours()))))
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
     *  Given   a new TimeEntryEditFormFragment with no data in the 'hours worked' field
     *  When    a user attempts to save the time entry
     *  Then    an error message appears [informing that 'hours worked' needs to be completed]
     *  And     the time entry is not saved
     */
    @Test
    public void testValidationFail_HoursWorkedNotEntered() {
        // given [setup]

        // when
        onView(withId(R.id.hours_edit_text)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.btn_add)).perform(click());

        // then
        TimeEntryData timeEntryData = provider.fakeTEdao.object;
        assertEquals(this.timeEntryData, timeEntryData);

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
        assertEquals(this.timeEntryData, timeEntryData);
        String error = String.format(getResourceString(R.string.error_time_entry_hours), minHours, maxHours);

        onView(withId(R.id.hours_input_layout)).check(matches(textInputLayoutWithError(withId(R.id.hours_input_layout), error)));
    }

    /*
     *  Given   a user with 16 hours already inserted for the day
     *  When    he tries to edit one of his time entries so that
     *          there are more than the max total hours for one day
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
        onView(withId(R.id.hours_edit_text)).perform(replaceText(String.valueOf(maxHours * 3 / 4)), closeSoftKeyboard());
        onView(withId(R.id.btn_add)).perform(click());

        // then
        TimeEntryData timeEntryData = provider.fakeTEdao.object;
        assertEquals(this.timeEntryData, timeEntryData);
        String error = String.format(getResourceString(R.string.error_time_entry_too_many_hours), maxHours);

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
        assertEquals(this.timeEntryData, timeEntryData);
        assertNull(this.timeEntryData.getComments());
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
        onView(withText(R.string.success_time_entry_save)).check(matches(isCompletelyDisplayed()));
    }

    /*          fixme | cannot find the second snackbar
     *  Given   a user saves a time entry with no errors
     *  When    the snackbar is displayed
     *  And     the user presses the dismiss button on the snackbar
     *  Then    snackbar does not exist anymore
     */
    @Test
    public void testSnackbarDismiss() {
        assertEquals(this.timeEntryData, provider.fakeTEdao.object);

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
        assertNotEquals(timeEntryData, this.timeEntryData);

        ViewInteraction successLabel = onView(withText(R.string.success_time_entry_save));
        successLabel.check(matches(isCompletelyDisplayed()));
        onView(withText(R.string.action_dismiss)).perform(click());

        // then

        successLabel.check(doesNotExist());
    }

    /*
     *  Given   the desire to acquire complete coverage
     *  When    a TimeEntryEditFormFragment is created using newInstance
     *  Then    a new TimeEntryAddFormFragment is created
     */
    @Test
    public void testNewInstance() {
        TimeEntryEditFormFragment fragment = TimeEntryEditFormFragment.newInstance();
        final int fragmentHashCode = fragment.hashCode();
        assertNotEquals(fragmentHashCode, TimeEntryEditFormFragment.newInstance());
    }

    /*
     *  Given   nothing
     *  When    TimeEntryEditFormFragment getActionTag is called
     *  Then    "TimeEntryEditFormFragment" is returned.
     */
    @Test
    public void testGetTag() {
        assertEquals(TimeEntryEditFormFragment.class.getSimpleName(),
                ((TimeEntryEditFormFragment) fragmentFactory.fragment).getActionTag());
    }

    /*
     *  Given   a TimeEntryEditFormFragment with no time entry id set in the bundle
     *  When    a new instance is created,
     *  Then    an IllegalStateException is thrown
     */
    @Test
    public void testISE_noTeId() {
        try {
            FragmentScenario.launchInContainer(
                    TimeEntryEditFormFragment.class,
                    null,
                    R.style.AppTheme,
                    fragmentFactory);
        } catch (Exception x) {
            assertTrue(x instanceof IllegalStateException);
        }
    }

    /*
     *  Given   a logged in user tries to edit another user's time entry
     *  When    the TimeEntryEditFormFragment is resumed,
     *  Then    none of the form fields are enabled
     *  And     onApply nothing changes
     */
    @Test
    public void testEditDisabled() {
        UserData otherUser = new UserData();
        otherUser.id = loggedInUser.id + 1;
        otherUser.setName("Rafaello Contondente");

        timeEntryData = new TimeEntryData();
        timeEntryData.id = 1L;
        timeEntryData.setHours(1);
        timeEntryData.setUserId(otherUser.id);
        timeEntryData.setWorkDate(System.currentTimeMillis());
        timeEntryData.setDateAdded(System.currentTimeMillis());
        timeEntryData.setDeleted(false);
        timeEntryData.setUserName(otherUser.getName());
        timeEntryData.setUserPhoto(otherUser.getPicture());
        timeEntryData.parentId = issueData.id;

        provider.fakePdao.object = projectData;
        provider.fakeIdao.object = issueData;
        provider.fakeTEdao.object = timeEntryData;
        provider.fakeUdao.object = otherUser;

        Bundle args = new Bundle();
        args.putLong(KEY_TIME_ENTRY_ID, timeEntryData.id);

        fragmentFactory = new AFragmentFactory(provider);

        FragmentScenario<TimeEntryEditFormFragment> scenario =
                FragmentScenario.launchInContainer(TimeEntryEditFormFragment.class, args, R.style.AppTheme, fragmentFactory);

        scenario.moveToState(State.RESUMED);

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
                .check(matches(withText(timeEntryData.getUserName())))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.hours_edit_text))
                .check(matches(withText(String.valueOf(timeEntryData.getHours()))))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.comments_edit_text))
                .check(matches(withText("")))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.work_date_edit_text))
                .check(matches(withText(date)))
                .check(matches(not(isEnabled())));
        onView(withId(R.id.date_added_edit_text))
                .check(matches(withText(date)))
                .check(matches(not(isEnabled())));

        ((TimeEntryEditFormFragment)fragmentFactory.fragment).applyAction();

        assertEquals(timeEntryData, provider.fakeTEdao.object);
    }

    private class AFragmentFactory extends FragmentFactory {
        final ActionFragmentDependencyProvider provider;
        Fragment fragment = null;

        AFragmentFactory(ActionFragmentDependencyProvider provider) {
            this.provider = provider;
        }

        @NonNull
        @Override
        public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
            Class clazz = loadFragmentClass(classLoader, className);

            if (clazz == TimeEntryEditFormFragment.class) {
                fragment = new TimeEntryEditFormFragment(provider);
            } else {
                fragment = super.instantiate(classLoader, className);
            }

            return fragment;
        }
    }

}