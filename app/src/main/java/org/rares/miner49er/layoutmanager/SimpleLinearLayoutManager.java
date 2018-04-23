package org.rares.miner49er.layoutmanager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import lombok.Setter;

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

    @Setter
    protected int selectedPosition = -1;    // currently adapter _selected_ position

    @Setter
    protected int maxItemElevation = 0;

    @Setter
    protected int itemCollapsedSelectedWidth = -1;

    public void resizeSelectedView(View itemView, boolean expandToMatchParent) {
    }
}
