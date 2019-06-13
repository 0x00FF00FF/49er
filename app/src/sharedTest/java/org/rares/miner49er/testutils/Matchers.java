package org.rares.miner49er.testutils;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.rares.ratv.rotationaware.RotationAwareTextView;

public class Matchers {

    public static Matcher<View> textInputLayoutWithError(final Matcher<View> parentMatcher, final String errorText) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                TextInputLayout view = (TextInputLayout) item;
                return view != null &&
                        view.getError() != null &&
                        view.getError().toString().equals(errorText);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("View with error: %s", errorText));
                parentMatcher.describeTo(description);
            }

            @Override
            protected void describeMismatchSafely(View item, Description mismatchDescription) {
                String mismatch = "";
                if (item == null) {
                    mismatch = "View is null.";
                } else {
                    if (!(item instanceof TextInputLayout)) {
                        mismatch = String.format("View is not instance of TextInputLayout [%s]", item.getClass().getSimpleName());
                    } else {
                        TextInputLayout view = (TextInputLayout) item;
                        if (view.getError() == null) {
                            mismatch = "View has no error. [null]";
                        } else if (!view.getError().equals(errorText)) {
                            mismatch = String.format("View has different errot text [%s]", view.getError());
                        }
                    }
                }
                mismatchDescription.appendText(mismatch);
                parentMatcher.describeMismatch(item, mismatchDescription);
            }
        };
    }

    public static Matcher<View> rotationAwareWithText(final Matcher<View> matcher, String text) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                matcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(View item) {
                return item instanceof RotationAwareTextView && ((RotationAwareTextView) item).getText().equals(text);
            }
        };
    }

    public static Matcher<View> childAtPosition(
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

    public static Matcher<View> withText(final Matcher<View> parentMatcher, final String text) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if (item instanceof AppCompatEditText) {
                    return ((AppCompatEditText) item).getEditableText().toString().equals(text);
                }
                if (item instanceof TextView) {
                    return ((TextView) item).getText().equals(text);
                }
                if (item instanceof RotationAwareTextView) {
                    return ((RotationAwareTextView) item).getText().equals(text);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                parentMatcher.describeTo(description);
            }
        };
    }
}
