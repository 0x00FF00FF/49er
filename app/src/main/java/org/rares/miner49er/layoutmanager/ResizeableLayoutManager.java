package org.rares.miner49er.layoutmanager;

import android.view.View;

import java.util.List;

public interface ResizeableLayoutManager {

    void setSelectedPosition(int selectedPosition);
    void setMaxItemElevation(int maxItemElevation);
    void setItemCollapsedSelectedWidth(int itemCollapsedSelectedWidth);
    void setItemCollapsedWidth(int itemCollapsedWidth);
    void resetState(boolean resetSelectedView);
    void addMeasureCompleteListener(PreloadSizeConsumer listener);
    void removeMeasureCompleteListener(PreloadSizeConsumer listener);

    List<ItemAnimationDto> resizeSelectedView(View itemView, boolean expandToMatchParent);

    interface PreloadSizeConsumer {
        void onMeasureComplete(int[] dimensions);
    }
}
