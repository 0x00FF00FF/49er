package org.rares.miner49er;


import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.util.TextUtils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.rares.miner49er.testutils.Matchers.childAtPosition;
import static org.rares.miner49er.testutils.Matchers.rotationAwareWithText;
import static org.rares.miner49er.testutils.TestUtil.copyAsset;
import static org.rares.miner49er.testutils.TestUtil.getResourceString;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Miner49er_e2e {

    @BeforeClass
    public static void beforeClass() {
        InstrumentationRegistry.getInstrumentation().getTargetContext().deleteDatabase(BaseInterfaces.DB_NAME);
    }

    @Rule
    public ActivityTestRule<HomeScrollingActivity> activityTestRule = new ActivityTestRule<>(HomeScrollingActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE");

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void teardown() {
        Intents.release();
    }

    @Test
    public void homeScrollingActivityTest() throws InterruptedException {

        // sign up (create user)

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(HomeScrollingActivity.class.getPackage().getName(), HomeScrollingActivity.class.getName()));
        activityTestRule.launchActivity(intent);

        ViewInteraction extendedFloatingActionButton = onView(withId(R.id.sign_up_button));
        extendedFloatingActionButton.perform(click());

        ViewInteraction addPhotoLabel = onView(withId(R.id.user_image_hint_text));

        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        Intent resultData = new Intent();
        Uri fileUri = Uri.fromFile(copyAsset(assetManager, "pugster.jpeg"));

        resultData.setData(fileUri);
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, resultData);

        intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(result);

        addPhotoLabel.perform(click());

        intended(hasAction(Intent.ACTION_CHOOSER));

        String userName = "pugster";
        String userEmail = "pug@ster";
        String failPassword = "1234";
        String goodPassword = "123123";

        ViewInteraction userNameEditText = onView(withId(R.id.user_name_edit_text));
        userNameEditText.perform(replaceText(userName), closeSoftKeyboard());
        userNameEditText.perform(pressImeActionButton());

        ViewInteraction userEmailEditText = onView(withId(R.id.user_email_edit_text));
        userEmailEditText.perform(replaceText(userEmail), closeSoftKeyboard());
        userEmailEditText.perform(pressImeActionButton());

        ViewInteraction userPasswordEditText = onView(withId(R.id.user_password_edit_text));
        userPasswordEditText.perform(replaceText(failPassword), closeSoftKeyboard());
        userPasswordEditText.perform(pressImeActionButton());

        ViewInteraction createAccountBtn = onView(withId(R.id.create_account_btn));
        createAccountBtn.perform(click());

        onView(withText(R.string.create_user_failed_no_password))
//                .inRoot(withDecorView(not(activityTestRule.getActivity().getWindow().getDecorView())))
                .check(matches(isDisplayed()));

        userPasswordEditText.perform(replaceText(goodPassword));
        userPasswordEditText.perform(closeSoftKeyboard());
        userPasswordEditText.perform(pressImeActionButton());

        createAccountBtn.perform(click());

        ViewInteraction rvContainer = onView(withId(R.id.scroll_views_container));

        Thread.sleep(2000);

        rvContainer.check(matches(isCompletelyDisplayed()));

        ViewInteraction projectsRv = onView(withId(R.id.rv_projects_list));

        projectsRv.check(matches(hasChildCount(0)));

        // adding a project

        ViewInteraction menuMore = onView(withContentDescription("More options"));
        menuMore.perform(click());

        ViewInteraction menuAddProject = onView(withSubstring(getResourceString(R.string.action_add_project)));
        menuAddProject.perform(click());

        ViewInteraction projectNameEditText = onView(withId(R.id.project_name_input_layout_edit));
        ViewInteraction projectDescriptionEditText = onView(withId(R.id.project_description_input_layout_edit));
        ViewInteraction projectIconEditText = onView(withId(R.id.project_icon_input_layout_edit));
        ViewInteraction editUserBtn = onView(withId(R.id.btn_edit_users));
        ViewInteraction uploadImageMenuEntry = onView(withSubstring(getResourceString(R.string.menu_title_upload_icon)));

        String projectName = "test project";
        String projectDescription = "test project description";

        projectNameEditText.perform(replaceText(projectName));
        projectDescriptionEditText.perform(replaceText(projectDescription));
        projectIconEditText.perform(click());
        uploadImageMenuEntry.perform(click());

        intended(hasAction(Intent.ACTION_CHOOSER), times(2));

        editUserBtn.perform(click());

        ViewInteraction userCardContainer = onView(withChild(withText(userName)));
        ViewInteraction userStatus = onView(allOf(withId(R.id.img_status), hasSibling(withText(userName))));
        ViewInteraction smallUserList = onView(withId(R.id.rv_small_users_list));
        ViewInteraction addUserBtn = onView(withId(R.id.btn_add_users));

        smallUserList.check(matches(not(isCompletelyDisplayed())));

        userStatus.check(matches(not(isCompletelyDisplayed())));

        userCardContainer.perform(click());

        userStatus.check(matches(isCompletelyDisplayed()));
        smallUserList.check(matches(isCompletelyDisplayed()));

        addUserBtn.perform(click());

        ViewInteraction projectTeamMembersRv = onView(withId(R.id.users_rv));

        projectTeamMembersRv.check(matches(hasChildCount(1)));

        onView(allOf(
                childAtPosition(withId(R.id.users_rv), 0),
                withChild(withChild(withText(userName)))))
                .check(matches(isCompletelyDisplayed()));

        ViewInteraction addProjectBtn = onView(withId(R.id.btn_add_project));
        addProjectBtn.perform(scrollTo()).perform(click());

        projectsRv.check(matches(allOf(
                hasChildCount(1),
                withChild(withChild(
                        rotationAwareWithText(withId(R.id.resizeable_list_item_container), projectName))))));

        // adding an issue

        String issueName = "test issue name";

        ViewInteraction issuesRv = onView(withId(R.id.rv_issues_list));
        ViewInteraction testProjectRow = onView(withId(R.id.resizeable_list_item_container));
        ViewInteraction infoLabel = onView(withSubstring("Issues"));
        ViewInteraction addIssueMenuEntry = onView(withSubstring(getResourceString(R.string.action_add_issue)));
        ViewInteraction issueNameEditText = onView(withId(R.id.issue_name_input_layout_edit));
        ViewInteraction addIssueBtn = onView(withId(R.id.btn_add_issue));

        Thread.sleep(30);

        infoLabel.check(matches(withSubstring("Issues: 0")));

        testProjectRow.perform(click());

        menuMore.perform(click());
        addIssueMenuEntry.perform(click());

        issueNameEditText.perform(replaceText(issueName)).perform(closeSoftKeyboard());
        addIssueBtn.perform(click());

        issuesRv.check(matches(allOf(
                hasChildCount(1),
                withChild(withChild(
                        rotationAwareWithText(withId(R.id.resizeable_list_item_container), TextUtils.capitalize(issueName)))))));

        // add time entry

        String timeEntryHoursWorked = "1";
        String timeEntryComments = "Time entry comments";

        ViewInteraction issueRow = onView(rotationAwareWithText(withId(R.id.resizeable_list_item_container), TextUtils.capitalize(issueName)));
        ViewInteraction timeEntriesRv = onView(withId(R.id.rv_time_entries_list));

        issueRow.perform(click());

        timeEntriesRv.check(matches(hasChildCount(0)));

        menuMore.perform(click());
        ViewInteraction addTimeEntryMenuEntry = onView(withSubstring(getResourceString(R.string.action_add_time_entry)));

        addTimeEntryMenuEntry.perform(click());

        onView(withId(R.id.owner_edit_text)).check(matches(allOf(not(isEnabled()), withText(userName))));
        onView(withId(R.id.issue_name_edit_text)).check(matches(allOf(not(isEnabled()), withText(issueName))));
        onView(withId(R.id.project_name_edit_text)).check(matches(allOf(not(isEnabled()), withText(projectName))));

        ViewInteraction hoursWorkedEditText = onView(withId(R.id.hours_edit_text));
        hoursWorkedEditText.perform(replaceText(timeEntryHoursWorked)).perform(closeSoftKeyboard());

        onView(withId(R.id.comments_edit_text)).perform(replaceText(timeEntryComments)).perform(closeSoftKeyboard());

        Thread.sleep(2000); // wait for the snackbar to disappear

        onView(withId(R.id.btn_add)).perform(click());

        timeEntriesRv.check(matches(allOf(
                hasChildCount(1),
                withChild(withChild(withText(TextUtils.extractInitials(userName)))),
                withChild(withChild(withChild(allOf(withSubstring(timeEntryHoursWorked), withSubstring("hour")))))
        )));

        Thread.sleep(1000);
    }
}
