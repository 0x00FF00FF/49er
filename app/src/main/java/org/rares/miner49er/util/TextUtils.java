package org.rares.miner49er.util;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.ratv.rotationaware.RotationAwareTextView;

public class TextUtils {

    public static final String TAG = TextUtils.class.getSimpleName();

    public static String extractInitials(String s) {
        final byte MAX_LETTERS = 4;

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

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void setCenterGravity(TextView view) {
        view.setGravity(Gravity.CENTER);
    }

    public static void setCenterStartGravity(TextView view) {
        view.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
    }

    public static String getItemText(View view) {
        if (!(view instanceof LinearLayout)) {
            return "BAD VIEW!";
        }
        LinearLayout layout = (LinearLayout) view;
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
}
