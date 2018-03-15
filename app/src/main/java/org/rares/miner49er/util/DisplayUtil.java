package org.rares.miner49er.util;

import android.content.Context;

/**
 * @author rares
 * @since 03.10.2017
 */

public class DisplayUtil {

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
