package org.rares.miner49er;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import lombok.Setter;

// TODO: 07.04.2018 | send info from adapter to lm -> selected item
// TODO: 07.04.2018 | check what's happening with the first item

/**
 * @author rares
 * @since 29.03.2018
 */

public class StickyLinearLayoutManager extends RecyclerView.LayoutManager {

    public static final String TAG = StickyLinearLayoutManager.class.getSimpleName();

    @Setter
    private int selectedPosition = -1;    // currently adapter _selected_ position

    @Setter
    private int maxItemElevation = 0;

    @Setter
    private int itemCollapsedSelectedWidth = -1;

    private int
            decoratedChildWidth = 0,
            decoratedChildHeight = 0,
            firstVisiblePosition = 0,
            itemsNumber = 0,
            lastVisiblePosition = 0,
            lastTopY = 0;

    private View selectedView = null;

    StickyLinearLayoutManager() {
        setItemPrefetchEnabled(true);
    }

    /**
     * The rv.lp contain additional information about the view.
     * Be sure to check out its methods; they offer hints about
     * the view's state inside the rv.
     *
     * @return a new set of {@link RecyclerView.LayoutParams}
     */
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        RecyclerView.LayoutParams layoutParameters =
                new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT);
        layoutParameters.setMargins(0, 0, 0, 0);
        return layoutParameters;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        detachAndScrapAttachedViews(recycler);

        if (getItemCount() == 0) {
            return;
        }

        Log.v(TAG, "onLayoutChildren: " + state);

        Log.i(TAG, "onLayoutChildren: state count: " + state.getItemCount());
        Log.i(TAG, "onLayoutChildren: child count: " + getChildCount());
        Log.v(TAG, "onLayoutChildren: adapter count: " + getItemCount());
        Log.i(TAG, "onLayoutChildren: getFocusedChild: " + getFocusedChild());


//        List<RecyclerView.ViewHolder> scrap = recycler.getScrapList();
//        for (int i = 0; i < scrap.size(); i++) {
//            ResizeableViewHolder holder = (ResizeableViewHolder) scrap.get(i);
//            Log.i(TAG, "onLayoutChildren: recyclable holder? " + holder.isRecyclable());
//            Log.i(TAG, "onLayoutChildren: custom id: " + holder.getItemProperties().getItemContainerCustomId());
//            Log.i(TAG, "onLayoutChildren: selected: " + holder.getItemProperties().isSelected());
//            Log.d(TAG, "onLayoutChildren: removed: " + ((RecyclerView.LayoutParams) holder.itemView.getLayoutParams()).isItemRemoved());
//        }

        Log.d(TAG, "onLayoutChildren: --------------s-t-a-r-t-------------------------");

        View labRatView = recycler.getViewForPosition(0);
        measureChildWithMargins(labRatView, 0, 0);
        itemsNumber = Math.min(getItemCount(), getHeight() / getDecoratedMeasuredHeight(labRatView) + 1);

        decoratedChildWidth = getDecoratedMeasuredWidth(labRatView);
        decoratedChildHeight = getDecoratedMeasuredHeight(labRatView);

        drawChildren(itemsNumber, recycler, decoratedChildWidth, decoratedChildHeight, 0);

        Log.d(TAG, "onLayoutChildren: -------------------e-n-d----------------------");
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {

//        Log.i(TAG, "scrollVerticallyBy: " + dy);
        int maxScroll = getItemCount() * (decoratedChildHeight - 2);

        if (lastTopY + dy <= 0) {
            dy = -lastTopY;
        }

/*
//      virtual space at the beginning.

        if (lastTopY + yScrollOffset <= -getHeight() + 3 * itemHeight) { // perhaps needs improvement
//            Log.i(TAG, "drawChildren: ltop " + lastTopY + "|" + getHeight() +
//                    "|" + 3 * itemHeight + "|" + yScrollOffset);
            yScrollOffset = -getHeight() + 3 * itemHeight - lastTopY;
        }
*/

//      virtual space at the end
        if (lastTopY + dy + decoratedChildHeight >= maxScroll) {
            dy = maxScroll - decoratedChildHeight - lastTopY;
        }

        lastTopY += dy;
//        Log.d(TAG, "scrollVerticallyBy() returned: " + lastTopY + "|" + maxScroll);

        offsetChildrenVertical(-dy);

        Log.i(TAG, "scrollVerticallyBy: global first: " + firstVisiblePosition + " last: " + lastVisiblePosition);
        firstVisiblePosition = lastTopY / decoratedChildHeight;

        final int localFirst = firstVisiblePosition;
        final int localLast = lastTopY /decoratedChildHeight*itemsNumber;
        Log.i(TAG, "scrollVerticallyBy: local first: " + localFirst + " last: " + localLast);
        boolean scrollDown = dy > 0;

        int removeStart, removeEnd;

        if (scrollDown) {
            removeStart = getPosition(getChildAt(0));
            removeEnd = firstVisiblePosition;
        }else{
            removeStart = localLast - Math.abs(dy)/decoratedChildHeight;
            removeEnd = localLast;
        }

        Log.d(TAG, "scrollVerticallyBy: remove: " + removeStart + " -> " + removeEnd);

        for (int i = removeStart; i < removeEnd; i++) {
            View child = getChildAt(i);
            if (child == null) {
                break;
            }
            int pos = getPosition(child);
            Log.i(TAG, "scrollVerticallyBy: " +
                    "firstVisiblePosition: " + firstVisiblePosition + " " +
                    "getChild Position: " + pos
            );
            if (pos < firstVisiblePosition - 1) {
//                detachAndScrapView(child, recycler);
                removeAndRecycleView(child, recycler);
                Log.v(TAG, "scrollVerticallyBy: removed view: " + i);
            }
        }

        return drawChildren(itemsNumber, recycler, decoratedChildWidth, decoratedChildHeight, dy);
    }

    private int drawIteration = 0;

    @Override
    public void collectAdjacentPrefetchPositions(int dx, int dy, RecyclerView.State state,
                                                 LayoutPrefetchRegistry layoutPrefetchRegistry) {
        Log.i(TAG, "collectAdjacentPrefetchPositions: ---------------------------------------------- ");

        boolean listGoingDown = dy > 0;
        View lastView = getChildAt(getChildCount() - 1);

        int lastPos = lastView == null ? 0 : getPosition(lastView);
        Log.i(TAG, "collectAdjacentPrefetchPositions: " +firstVisiblePosition + " -> " + lastPos);
        int from = getChildCount() == 0 ? 0 : (listGoingDown ?
                Math.min(lastPos + 1, state.getItemCount()) :
                Math.max(0, getPosition(getChildAt(0)) - 1));

        int prefetchNumber = 1;
        int itemsNumber = dy / decoratedChildHeight * prefetchNumber;

        if (listGoingDown) {
            Log.i(TAG, "V : from " + from + " to " + Math.max(from+1, from + itemsNumber));
            for (int i = from, addedItems = 0; i < Math.min(state.getItemCount(), Math.max(from+1, from + itemsNumber)); i++, addedItems++) {
                Log.v(TAG, "fetching position: " + i + " [already added items: " + addedItems + "]");
                layoutPrefetchRegistry.addPosition(i, addedItems * decoratedChildHeight);
                if (addedItems == prefetchNumber) {
                    break;
                }
            }
        } else {
            Log.i(TAG, "^ : from " + from + " to " + Math.max(from-1, from + itemsNumber));
            for (int i = from, addedItems = 0; i > Math.max(0, Math.min(from-1, from + itemsNumber)); i--, addedItems++) {
                Log.v(TAG, "fetching position: " + i + " [already added items: " + addedItems + "]");
                layoutPrefetchRegistry.addPosition(i, addedItems * decoratedChildHeight);
                if (addedItems == prefetchNumber) {
                    break;
                }
            }
        }
        Log.i(TAG, "collectAdjacentPrefetchPositions: ----------------------------------------------");
    }

    private int drawChildren(
            int itemsNumber,
            RecyclerView.Recycler recycler,
            int itemWidth, int itemHeight,
            int yScrollDelta
    ) {

        drawIteration += 1;

//        Log.e(TAG, "drawChildren: >>>>>>>>>>>>> " +yScrollDelta);

//        Log.i(TAG, "drawChildren: " + getChildCount());

        boolean listGoingDown = yScrollDelta > 0;
        View lastView = getChildAt(getChildCount() - 1);

        int lastPos = lastView == null ? 0 : getPosition(lastView);
        int from = getChildCount() == 0 ? 0 : (listGoingDown ? lastPos : getPosition(getChildAt(0)));


        detachAndScrapAttachedViews(recycler);


        firstVisiblePosition = lastTopY / itemHeight;
//        int lastVisiblePosition = firstVisiblePosition + itemsNumber -1;

//        Log.i(TAG, "drawChildren: firstVisiblePosition: " + firstVisiblePosition);
//        Log.i(TAG, "drawChildren: lastVisiblePosition " + lastVisiblePosition);


//        for (int i = 0; i < firstVisiblePosition; i++) {
//            removeAndRecycleView(recycler.getViewForPosition(i), recycler);
//            Log.v(TAG, "drawChildren: removed view: " + i);
//        }
        if (firstVisiblePosition + 1 > getItemCount()) {
            return 0;
        }

        lastVisiblePosition = firstVisiblePosition + itemsNumber + 1;

//        Log.e(TAG, "drawChildren: drawIteration: " + drawIteration);

        int to = Math.min(lastVisiblePosition, getItemCount());

//        Log.e(TAG, "drawChildren: from: " + from + " to " + to);
//        Log.e(TAG, "drawChildren: firstVisiblePosition: " + firstVisiblePosition);
//        Log.e(TAG, "drawChildren: lastVisiblePosition: " + lastVisiblePosition);

        for (int i = firstVisiblePosition; i < to; i++) {
            final View item = recycler.getViewForPosition(i);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                Log.i(TAG, "drawChildren: item.elevation: " + item.getElevation());
//            }
////// TODO: rv ignore view/holder
//            Log.d(TAG, "drawChildren: w/h: " + itemWidth + "/" + itemHeight);
//            Log.i(TAG, "drawChildren: width: " + item.getLayoutParams().width);
//            Log.i(TAG, "drawChildren: height: " + item.getLayoutParams().height);
//            Log.v(TAG, "drawChildren: rv width/height: " + getWidth() + "/" + getHeight());

            int l = (int) Math.pow(2, 6 - i) * 3;
            int inbetween = 0; // space between items | if item decorations wouldn't be enough
            int r, t, b;
            t = i * itemHeight + (inbetween * (i + 1)) - lastTopY;
            r = item.getLayoutParams().width == -1 ? getWidth() : item.getLayoutParams().width;
            b = (i + 1) * (itemHeight + inbetween) - lastTopY;
//            if (i == selectedPosition && i == firstVisiblePosition) {
//                t = 0;
//                b = itemHeight + inbetween;
//            }
//            if (i == selectedPosition) {
//                r = itemCollapsedSelectedWidth;
//            }
            addView(item);
            measureChildWithMargins(item, 0, 0);
            layoutDecoratedWithMargins(item, 0, t, r, b);

//            Log.i(TAG, "drawChildren: i: " + i + " l: " + 0 + "; r: " + r + "; t: " + t + "; b: " + b);
        }

//        Log.e(TAG, "drawChildren: <<<<<<<<<<<<<<<<<<");

        return yScrollDelta;
    }

    public void resizeSelectedView(View itemView, boolean expandToMatchParent) {

        if (expandToMatchParent) {
            itemView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setElevation(0);
            }
        } else {
            itemView.getLayoutParams().width = itemCollapsedSelectedWidth;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setElevation(maxItemElevation);
            }
        }

        if (selectedView == null) {
            requestLayout();
            selectedView = itemView;
            return;
        }

        if (itemView != selectedView) {
            selectedView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                selectedView.setElevation(0);
            }
            requestLayout();
            selectedView = itemView;
        }
    }

}
