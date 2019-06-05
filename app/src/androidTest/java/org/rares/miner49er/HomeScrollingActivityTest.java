package org.rares.miner49er;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class HomeScrollingActivityTest {

    @Rule
    public ActivityTestRule<HomeScrollingActivity> mActivityTestRule = new ActivityTestRule<>(HomeScrollingActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.READ_EXTERNAL_STORAGE");

    @Test
    public void homeScrollingActivityTest() {
        ViewInteraction extendedFloatingActionButton = onView(
                allOf(withId(R.id.sign_up_button), withText("Sign Up"),
                        childAtPosition(
                                allOf(withId(R.id.landing_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                3),
                        isDisplayed()));
        extendedFloatingActionButton.perform(click());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.user_image_hint_text), withText("Tap to add your photo"),
                        childAtPosition(
                                allOf(withId(R.id.signup_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                1),
                        isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.user_name_edit_text),
                        childAtPosition(
                                allOf(withId(R.id.signup_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                2),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("pugster"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.user_name_edit_text), withText("pugster"),
                        childAtPosition(
                                allOf(withId(R.id.signup_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                2),
                        isDisplayed()));
        appCompatEditText2.perform(pressImeActionButton());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.user_email_edit_text),
                        childAtPosition(
                                allOf(withId(R.id.signup_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                3),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("pug@ster"), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.user_email_edit_text), withText("pug@ster"),
                        childAtPosition(
                                allOf(withId(R.id.signup_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                3),
                        isDisplayed()));
        appCompatEditText4.perform(pressImeActionButton());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.user_password_edit_text),
                        childAtPosition(
                                allOf(withId(R.id.signup_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                4),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText("1234"), closeSoftKeyboard());

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.user_password_edit_text), withText("1234"),
                        childAtPosition(
                                allOf(withId(R.id.signup_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                4),
                        isDisplayed()));
        appCompatEditText6.perform(pressImeActionButton());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.user_password_edit_text), withText("1234"),
                        childAtPosition(
                                allOf(withId(R.id.signup_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                4),
                        isDisplayed()));
        appCompatEditText7.perform(replaceText("123456"));

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.user_password_edit_text), withText("123456"),
                        childAtPosition(
                                allOf(withId(R.id.signup_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                4),
                        isDisplayed()));
        appCompatEditText8.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText9 = onView(
                allOf(withId(R.id.user_password_edit_text), withText("123456"),
                        childAtPosition(
                                allOf(withId(R.id.signup_container),
                                        childAtPosition(
                                                withId(R.id.main_container),
                                                6)),
                                4),
                        isDisplayed()));
        appCompatEditText9.perform(pressImeActionButton());

        ViewInteraction overflowMenuButton = onView(
                allOf(withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar_c),
                                        1),
                                0),
                        isDisplayed()));
        overflowMenuButton.perform(click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
