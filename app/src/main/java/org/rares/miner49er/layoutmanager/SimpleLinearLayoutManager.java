package org.rares.miner49er.layoutmanager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

public class SimpleLinearLayoutManager
        extends LinearLayoutManager
        implements ResizeableLayoutManager {

    public SimpleLinearLayoutManager(Context context) {
        super(context);
    }

    public SimpleLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public SimpleLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private int selectedPosition = -1;    // currently adapter _selected_ position

    @Setter
    protected int maxItemElevation = 0;

    @Setter
    protected int itemCollapsedSelectedWidth = -1;

    @Setter
    int itemCollapsedWidth = -1;

    @Override
    public void setSelected(int selectedPosition, View sv) {
        this.selectedPosition = selectedPosition;
    }

    @Override
    public void resetState(boolean resetSelectedView) {
        selectedPosition = -1;
        maxItemElevation = 0;
        itemCollapsedSelectedWidth = -1;
        itemCollapsedWidth = -1;
    }

    public List<ItemAnimationDto> resizeSelectedView(View itemView, boolean expandToMatchParent) {
        return Collections.emptyList();
    }
}
