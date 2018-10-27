package org.rares.miner49er._abstract;

import android.animation.ValueAnimator;
import io.reactivex.processors.PublishProcessor;

public interface ItemViewAnimator {

    /**
     *
     * @param reverse
     * @param selected
     * @param animationTime
     */
    ValueAnimator animateItem(boolean reverse, boolean selected, int animationTime, PublishProcessor<Boolean> publishProcessor);

    void validateItem(boolean collapsed, boolean selected);

}
