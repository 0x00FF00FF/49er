package org.rares.miner49er.layoutmanager.postprocessing.rotation;

import android.view.View;
import android.view.ViewGroup;
import org.rares.miner49er.layoutmanager.postprocessing.ResizePostProcessor;

public interface ItemRotator extends ResizePostProcessor {
    enum ItemRotation {
        ROTATE_COUNTER_CLOCKWISE(-90),
        ROTATE_CLOCKWISE(90),
        NO_ROTATION(0);

        private int value;

        ItemRotation(int value) {
            this.value = value;
        }
    }
    void rotateItems(ViewGroup viewGroup);
    void rotateItem(View view, boolean clockwise);
    void validateViewRotation(View view, boolean closedState, boolean isViewSelected);
}
