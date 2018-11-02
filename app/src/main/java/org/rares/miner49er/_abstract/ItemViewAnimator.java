package org.rares.miner49er._abstract;

import android.animation.ValueAnimator;

public interface ItemViewAnimator {

    /**
     *
     * @param reverse
     * @param selected
     * @param animationTime
     */
    ValueAnimator animateItem(boolean reverse, boolean selected, int animationTime);

    void validateItem(boolean collapsed, boolean selected);

}
