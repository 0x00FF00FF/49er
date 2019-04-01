package org.rares.miner49er.util;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.ratv.rotationaware.RotationAwareTextView;

public class TextUtils {

    private static final byte MAX_LETTERS = 4;

    public static final String TAG = TextUtils.class.getSimpleName();

    public static String extractInitials(String s) {

        if (s == null || s.length() == 0) {
            return "";
        }

        s = clearNamePrefix(s);

        StringBuilder builder = new StringBuilder(2);
        s = s.trim().toUpperCase();
        if (s.length() == 0) {
            return "";
        }

        builder.append(s.charAt(0));

        if (s.length() > 1) {
            for (int i = 1; i < s.length(); i++) {
                char c = s.charAt(i);
//                Log.v(TAG, "extractInitials: " + c);
                if (Character.isWhitespace(c)) {
                    continue;
                }
                if (Character.isLetterOrDigit(c)) {
//                    Log.v(TAG, "extractInitials: letter or digit");
                    Character prevCh = s.charAt(i - 1);
                    if (Character.isWhitespace(prevCh)) {
//                        Log.i(TAG, "extractInitials: prev is whitespace " + prevCh);
                        builder.append(c);
                    }
                    if (i + 1 < s.length()) {
                        Character nextCh = s.charAt(i + 1);
//                        Log.i(TAG, "extractInitials: next is digit " + nextCh);
                        if (Character.isDigit(nextCh)) {
                            builder.append(nextCh);
                        }
                    }
                }
            }
        }
//        Log.i(TAG, "extractInitials: [" + builder.toString() + "]");
        String res = builder.toString();

        if (res.length() > MAX_LETTERS) {
            res = res.substring(0, MAX_LETTERS);
        }
        return res;
    }

    public static String extractVowels(String str) {
        String vowels = "[aeiouAEIOU\u00c4\u00e4\u00d6\u00f6\u00dc\u00fc\u00df]";
        String punctuation = "[`~!@#$%^&*()_+|=\\[\\]{};':\",/<>?\\-.]";
        String processed = str
                .substring(vowels.contains(String.valueOf(str.charAt(0))) ? 1 : 0)
                .replaceAll(vowels, "")
                .replaceAll(punctuation, "")
                .toUpperCase();
        return processed.length() > MAX_LETTERS ? processed.substring(0, MAX_LETTERS) : processed;
    }

    public static String capitalize(String str) {
        if (str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void setCenterGravity(TextView view) {
        view.setGravity(Gravity.CENTER);
    }

    public static void setCenterStartGravity(TextView view) {
        view.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
    }

    public static String getItemText(View view) {
        if (!(view instanceof ViewGroup)) {
            return "BAD VIEW!";
        }
        ViewGroup layout = (ViewGroup) view;    // constraint layout
        View childView = layout.getChildAt(0);
        if (childView instanceof TextView) {
            TextView tv = (TextView) childView;
            return tv.getText().toString();
        }
        if (childView instanceof ViewGroup) {
            View v = ((ViewGroup) childView).getChildAt(0);
            if (v instanceof RotationAwareTextView) {
                return ((RotationAwareTextView) v).getText();
            }
        }
        if (childView instanceof RotationAwareTextView) {
            return ((RotationAwareTextView) childView).getText();
        }
        throw new UnsupportedOperationException("Unsupported view: " + view.getClass().toString());
    }

    public static String getItemText(RecyclerView.ViewHolder holder) {
        if (holder instanceof ResizeableItemViewHolder) {
            return ((ResizeableItemViewHolder) holder).getItemText();
        }
        return "";
    }

    private static String clearNamePrefix(String s) {
        String res = s;

        res = res.replace("-", " ");
        res = res.replace("Baronin ", "");
        res = res.replace("Baron ", "");
        res = res.replace("Graf ", "");
        res = res.replace("Gräfin ", "");
        res = res.replace("von ", "");
        res = res.replace("vom ", "");
        res = res.replace("der ", "");
        res = res.replace("den ", "");
        res = res.replace("Fr. ", "");
        res = res.replace("Hr. ", "");
        res = res.replace("Dr. ", "");
        res = res.replace("Prof. ", "");
        res = res.replace("zu ", "");
        res = res.replace("Freiherrin ", "");
        res = res.replace("Freiherr ", "");
        res = res.replace("Dipl. ", "");
        res = res.replace("Ing. ", "");

        return res;
    }

    public static void hideKeyboardFrom(Context context, View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }
}
