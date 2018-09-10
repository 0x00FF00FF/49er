package org.rares.miner49er.util;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextUtils {

    public static final String TAG = TextUtils.class.getSimpleName();

    public static String extractInitials(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
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
        return builder.toString();
    }

    public static void setCenterGravity(TextView view) {
        view.setGravity(Gravity.CENTER);
    }

    public static void setCenterStartGravity(TextView view) {
        view.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
    }

    public static String getItemText(View v) {
        if (!(v instanceof LinearLayout)) {
            return "BAD VIEW!";
        }
        LinearLayout layout = (LinearLayout) v;
        TextView tv = (TextView) layout.getChildAt(0);
        return tv.getText().toString();
    }
}
