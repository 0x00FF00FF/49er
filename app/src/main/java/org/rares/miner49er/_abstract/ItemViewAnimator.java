package org.rares.miner49er._abstract;

public interface ItemViewAnimator {

    /**
     *
     * @param reverse
     * @param selected
     * @param animationTime
     */
    void animateItem(boolean reverse, boolean selected, int animationTime);

    void validateItem(boolean collapsed, boolean selected);

}
