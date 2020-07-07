package org.rares.miner49er.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import com.google.android.material.appbar.AppBarLayout;

/**
 * @author rares
 * @since 09.03.2018
 */

public class BehaviorFix extends AppBarLayout.Behavior {


    public static final String TAG = BehaviorFix.class.getSimpleName();

    public BehaviorFix() {
        super();
    }

    public BehaviorFix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (dyUnconsumed >= 0) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed,
                    dxUnconsumed, dyUnconsumed, type);
        } else {
            if (dyConsumed < 0) {
                super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed,
                        dxUnconsumed, -dyConsumed, type);
                ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
            } else {
                super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed,
                        dxUnconsumed, dyUnconsumed, type);
            }
        }
//        Log.d(TAG, "onNestedScroll() called with: " +
////                "coordinatorLayout = [" + coordinatorLayout + "], " +
////                "child = [" + child + "], " +
//                "target = [" + target + "], " +
////                "dxConsumed = [" + dxConsumed + "], " +
//                "dyConsumed = [" + dyConsumed + "], " +
////                "dxUnconsumed = [" + dxUnconsumed + "], " +
//                "dyUnconsumed = [" + dyUnconsumed + "], ");
        stopNestedScrollIfNeeded(dyUnconsumed, child, target, type);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                                  View target, int dx, int dy, int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        stopNestedScrollIfNeeded(dy, child, target, type);
    }

    private void stopNestedScrollIfNeeded(int dy, AppBarLayout child, View target, int type) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            final int currOffset = getTopAndBottomOffset();
            if ((dy < 0 && currOffset == 0)
                    || (dy > 0 && currOffset == -child.getTotalScrollRange())) {
                ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
            }
        }
    }
}