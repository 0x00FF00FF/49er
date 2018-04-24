package org.rares.miner49er.layoutmanager;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import lombok.Setter;

// TODO: 07.04.2018 | send info from adapter to lm -> selected item

/**
 * @author rares
 * @since 29.03.2018
 */

public class StickyLinearLayoutManager
        extends RecyclerView.LayoutManager
        implements ResizeableLayoutManager {

    private final static String tag = StickyLinearLayoutManager.class.getSimpleName();
    private static String TAG = tag;

    private static final int BOTTOM = -1;
    //    public static final int NONE = 0;
    private static final int TOP = 1;

    @Setter
    private int selectedPosition = -1;    // currently adapter _selected_ position

    @Setter
    private int maxItemElevation = 0;

    @Setter
    private int itemCollapsedSelectedWidth = -1;


    // extraChildren: number of children drawn outside of bounds, both at top and bottom.
    // e.g. extraChildren = 1 will result in one child at top, and one at bottom.
    // this number will affect child recycling as well as drawing..
    private int
            extraChildren = 0,
            decoratedChildWidth = 0,
            decoratedChildHeight = 0,
            firstVisiblePosition = 0,
            itemsNumber = 0,
            lastVisiblePosition = 0,
            lastTopY = 0;

    private View selectedView = null;

//    StickyLinearLayoutManager() {
//        setItemPrefetchEnabled(true);
//    }

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

        //Log.v(TAG, "onLayoutChildren: " + state);

        //Log.i(TAG, "onLayoutChildren: state count: " + state.getItemCount());
        //Log.i(TAG, "onLayoutChildren: child count: " + getChildCount());
        //Log.v(TAG, "onLayoutChildren: adapter count: " + getItemCount());
        //Log.i(TAG, "onLayoutChildren: getFocusedChild: " + getFocusedChild());


//        List<RecyclerView.ViewHolder> scrap = recycler.getScrapList();
//        for (int i = 0; i < scrap.size(); i++) {
//            ResizeableViewHolder holder = (ResizeableViewHolder) scrap.get(i);
//            //Log.i(TAG, "onLayoutChildren: recyclable holder? " + holder.isRecyclable());
//            //Log.i(TAG, "onLayoutChildren: custom id: " + holder.getItemProperties().getItemContainerCustomId());
//            //Log.i(TAG, "onLayoutChildren: selected: " + holder.getItemProperties().isSelected());
//            //Log.d(TAG, "onLayoutChildren: removed: " + ((RecyclerView.LayoutParams) holder.itemView.getLayoutParams()).isItemRemoved());
//        }

        //Log.d(TAG, "onLayoutChildren: --------------s-t-a-r-t-------------------------");

        View labRatView = recycler.getViewForPosition(0);
        measureChildWithMargins(labRatView, 0, 0);

        decoratedChildWidth = getDecoratedMeasuredWidth(labRatView);
        decoratedChildHeight = getDecoratedMeasuredHeight(labRatView);
        itemsNumber = Math.min(getItemCount(), getHeight() / decoratedChildHeight);
        removeAndRecycleView(labRatView, recycler);

        drawChildren(BOTTOM, recycler);

        //Log.d(TAG, "onLayoutChildren: -------------------e-n-d----------------------");
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int itemAddPosition = dy > 0 ? BOTTOM : TOP;
        TAG = tag + (dy > 0 ? " v " : " ^ ");
        Log.w(TAG, "scrollVerticallyBy: " +
                "[dy: " + dy + "]" +
                "[lastTopY: " + lastTopY + "]" +
                "[scrollRemaining: " + state.getRemainingScrollVertical() + "]" +
                "[firstVisiblePosition: " + firstVisiblePosition + "]"
        );

        /*
            seems like for high dy, the algorithm does not work quite well..
            perhaps it would be a good idea to add some logic that
            predicts items positions and does not do stuff if it doesn't make sense
            (like removing 10 items at once if dy demands so)
         */
        int maxScroll = getItemCount() * (decoratedChildHeight - 2);

        View firstView = getChildAt(0);
        int firstChildPos = -1;
        if (firstView != null) {
            firstChildPos = getPosition(firstView);
        }
        if (firstChildPos == 0) {
            if (lastTopY + dy <= 0) {
                dy = -lastTopY;
            }
        }

        Log.w(TAG, "scrollVerticallyBy: [dy: " + dy + "]");

/*
//      virtual space at the beginning.

        if (lastTopY + yScrollOffset <= -getHeight() + 3 * itemHeight) { // perhaps needs improvement
//            //Log.i(TAG, "drawChildren: ltop " + lastTopY + "|" + getHeight() +
//                    "|" + 3 * itemHeight + "|" + yScrollOffset);
            yScrollOffset = -getHeight() + 3 * itemHeight - lastTopY;
        }
*/

//      virtual space at the end
        if (lastTopY + dy + decoratedChildHeight >= maxScroll) {
            dy = maxScroll - decoratedChildHeight - lastTopY;
        }

        lastTopY += dy;

        Log.i(TAG, "scrollVerticallyBy: " +
                " >>> firstVisiblePosition: " + firstVisiblePosition +
                " lastTopY: " + lastTopY
        );

        final int max = getChildCount();// TODO: 4/21/18 clean these fors up
        View[] toRecycle = new View[max];
        int index = 0;
        if (dy > 0) { // adding items at BOTTOM
            for (int i = 0; i < max; i++) {
                View v = getChildAt(i);
                float itemLowerBorder = v.getY() + decoratedChildHeight - dy;
                if (itemLowerBorder < -(decoratedChildHeight * extraChildren)) {
                    toRecycle[index++] = v;
                }
            }
        } else {     // adding items at TOP
            for (int i = max - 1; i >= 0; i--) {
                View v = getChildAt(i);
                float itemY = v.getY() - dy;
                if (itemY > getHeight() + (decoratedChildHeight * extraChildren)) {
                    toRecycle[index++] = v;
                }
            }
        }
        for (int i = 0; i < max; i++) {
            View v = toRecycle[i];
            if (v != null) {
                removeAndRecycleView(v, recycler);
                String text = getItemText(v);
                Log.d(TAG, "scrollVerticallyBy: " +
                        " XXX removing view: " + text +
                        " item top border: " + (v.getY() - dy) +
                        " item lower border: " + (v.getY() + decoratedChildHeight - dy) +
                        " children: " + getChildCount());
            } else {
                break;
            }
        }
        offsetChildrenVertical(-dy);
        drawChildren(itemAddPosition, recycler);
        return dy;
    }

    @Override
    public void collectAdjacentPrefetchPositions(
            int dx, int dy,
            RecyclerView.State state,
            LayoutPrefetchRegistry layoutPrefetchRegistry) {
        //Log.i(TAG, "collectAdjacentPrefetchPositions: ---------------------------------------------- ");

//        boolean listGoingDown = dy > 0;
//        View lastView = getChildAt(getChildCount() - 1);
//
//        int lastPos = lastView == null ? 0 : getPosition(lastView);
//        //Log.i(TAG, "collectAdjacentPrefetchPositions: " +firstVisiblePosition + " -> " + lastPos);
//        int from = getChildCount() == 0 ? 0 : (listGoingDown ?
//                Math.min(lastPos + 1, state.getItemCount()) :
//                Math.max(0, getPosition(getChildAt(0)) - 1));
//
//        int prefetchNumber = 1;
//        int itemsNumber = dy / decoratedChildHeight * prefetchNumber;
//
//        if (listGoingDown) {
//            //Log.i(TAG, "V : from " + from + " to " + Math.max(from+1, from + itemsNumber));
//            for (int i = from, addedItems = 0; i < Math.min(state.getItemCount(), Math.max(from+1, from + itemsNumber)); i++, addedItems++) {
//                //Log.v(TAG, "fetching position: " + i + " [already added items: " + addedItems + "]");
//                layoutPrefetchRegistry.addPosition(i, addedItems * decoratedChildHeight);
//                if (addedItems == prefetchNumber) {
//                    break;
//                }
//            }
//        } else {
//            //Log.i(TAG, "^ : from " + from + " to " + Math.max(from-1, from + itemsNumber));
//            for (int i = from, addedItems = 0; i > Math.max(0, Math.min(from-1, from + itemsNumber)); i--, addedItems++) {
//                //Log.v(TAG, "fetching position: " + i + " [already added items: " + addedItems + "]");
//                layoutPrefetchRegistry.addPosition(i, addedItems * decoratedChildHeight);
//                if (addedItems == prefetchNumber) {
//                    break;
//                }
//            }
//        }
//        Log.i(TAG, "collectAdjacentPrefetchPositions: ----------------------------------------------");
    }

    private void drawChildren(
            int newItemPosition,
            RecyclerView.Recycler recycler
    ) {

        // first of all, update first visible position.
        firstVisiblePosition = lastTopY / decoratedChildHeight;

        String logDirection = " = ";
        if (newItemPosition == BOTTOM) {
            logDirection = " v ";
        }
        if (newItemPosition == TOP) {
            logDirection = " ^ ";
        }
        TAG = tag + logDirection;

        int bottomMostPosition = getChildCount() == 0 ? 0 : getPosition(getChildAt(getChildCount() - 1));

        int from =
                newItemPosition == BOTTOM ?
                        Math.min(getItemCount(), bottomMostPosition == 0 ? 0 : bottomMostPosition + 1) :
                        Math.max(0, firstVisiblePosition - extraChildren);

        if (from == getItemCount()) {
            Log.v(TAG, "drawChildren: NOT DRAWING ANYTHING. [Adding items after last adapter position.]");
            return;
        }

        lastVisiblePosition = Math.min(firstVisiblePosition + itemsNumber + 2, getItemCount());

        View item = getChildAt(0);

        int to = newItemPosition == BOTTOM ? lastVisiblePosition + (decoratedChildHeight * extraChildren) : getPosition(item);

        if (from > to) {
            Log.v(TAG, "drawChildren: NOT DRAWING ANYTHING. [from > to]");
            return;
        }

        Log.e(TAG, "drawChildren: firstVisiblePosition: " + firstVisiblePosition);
        Log.e(TAG, "_drawChildren: lastVisiblePosition: " + lastVisiblePosition);
        Log.i(TAG, "drawChildren: from: " + from + " -> to: " + to + " > " + getItemText(item));
        Log.i(TAG, "_drawChildren: child count: " + getChildCount());

        int reversePosition = 0;
        for (int i = from; i < to; i++) {
            int inbetween = 0; // space between items | if item decorations wouldn't be enough
            int r, t, b, l;
            t = i * decoratedChildHeight + (inbetween * (i + 1)) - lastTopY;
            b = (i + 1) * (decoratedChildHeight + inbetween) - lastTopY;
            if (newItemPosition == BOTTOM) {
                if (t > getHeight() + (decoratedChildHeight * extraChildren)) {
                    Log.e(TAG, "v drawChildren: item is out of view bounds." +
                            " will not draw position #" + i + " t=" + t);
                    return;
                }
            } else {
                if (b < -decoratedChildHeight * extraChildren) {
                    Log.e(TAG, "drawChildren: ^ item is out of view bounds." +
                            " will not draw position #" + i + " b=" + b
                    );
                    return;
                }
            }
            item = recycler.getViewForPosition(i);
            String text = getItemText(item);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                //Log.i(TAG, "drawChildren: item.elevation: " + item.getElevation());
//            }

            l = (int) Math.pow(2, 6 - i) * 3;

            int lpWidth = item.getLayoutParams().width;
            r = lpWidth == -1 ? getWidth() : lpWidth;

            if (newItemPosition == BOTTOM) {
                addView(item);
            } else {
                addView(item, reversePosition++);
            }
            measureChildWithMargins(item, 0, 0);
            layoutDecoratedWithMargins(item, 0, t, r, b);

            Log.d(TAG, "drawChildren: newly added view: " + text +
                    "; position: " + (reversePosition - 1) +
                    "; children: " + getChildCount());
            Log.i(TAG, "drawChildren: " + getItemText(item) +
                    " adapter position: " + i +
                    " l: " + 0 + "; r: " + r + "; t: " + t + "; b: " + b +
                    "; lastTopY: " + lastTopY +
                    "; rv height: " + getHeight()
            );
        }
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

    private String getItemText(View v) {
        if (!(v instanceof LinearLayout)) {
            return "BAD VIEW!";
        }
        LinearLayout layout = (LinearLayout) v;
        TextView tv = (TextView) layout.getChildAt(0);
        return tv.getText().toString();
    }

}
