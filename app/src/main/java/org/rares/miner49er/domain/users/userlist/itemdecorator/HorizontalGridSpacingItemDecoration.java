package org.rares.miner49er.domain.users.userlist.itemdecorator;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

//todo needs more work.
public class HorizontalGridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private boolean includeEdge;

    public HorizontalGridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        int row = position % spanCount; // item row

        if (includeEdge) {
            outRect.left = spacing - row * spacing / spanCount; // spacing - row * ((1f / spanCount) * spacing)
            outRect.right = (row + 1) * spacing / spanCount; // (row + 1) * ((1f / spanCount) * spacing)

            outRect.bottom = spacing; // item bottom
        } else {
            outRect.left = spacing / 2;
            outRect.right = spacing / 2;
        }
    }
}
