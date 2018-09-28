package org.rares.miner49er.util;

import android.content.Context;
import android.graphics.Color;

/**
 * @author rares
 * @since 03.10.2017
 */

public class UiUtil {

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int getBrighterColor(int color, float extraBrightness) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
//        Log.i(TAG, "getBrighterColor: color: " + color + " hsv: " + Arrays.toString(hsv));
        if (hsv[1] < 0.5F) {
            hsv[1] -= extraBrightness;
        } else {
            hsv[1] -= 2 * extraBrightness;
        }
        hsv[2] += extraBrightness;
        return Color.HSVToColor(hsv);
    }
}
