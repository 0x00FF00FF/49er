package org.rares.miner49er.layoutmanager;

import android.view.View;

import java.util.List;

public interface ResizeableLayoutManager {

    void setSelected(int selectedPosition, View selectedView);
    void setMaxItemElevation(int maxItemElevation);
    void setItemCollapsedSelectedWidth(int itemCollapsedSelectedWidth);
    void setItemCollapsedWidth(int itemCollapsedWidth);
    void resetState(boolean resetSelectedView);

    List<ItemAnimationDto> resizeSelectedView(View itemView, boolean expandToMatchParent);
}
