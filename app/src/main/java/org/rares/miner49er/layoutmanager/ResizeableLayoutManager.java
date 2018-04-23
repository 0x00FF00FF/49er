package org.rares.miner49er.layoutmanager;

import android.view.View;

public interface ResizeableLayoutManager {

    void setSelectedPosition(int selectedPosition);
    void setMaxItemElevation(int maxItemElevation);
    void setItemCollapsedSelectedWidth(int itemCollapsedSelectedWidth);

    void resizeSelectedView(View itemView, boolean expandToMatchParent);
}
