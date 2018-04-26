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
    private static final int NONE = 0;
    private static final int TOP = 1;

    public static final int TAG_INITIAL_POSITION = 298734927;

    @Setter
    private int selectedPosition = -1;    // currently adapter _selected_ position

    @Setter
    private int maxItemElevation = 0;

    @Setter
    private int itemCollapsedSelectedWidth = -1;


    // extraChildren: number of children drawn outside of bounds, both at top and bottom.
    // e.g. extraChildren = 1 will result in one child at top, and one at bottom.
    // this number will affect child recycling as well as drawing
    // only works with positive values.
    // --
    // virtualPosition: made-up position to help calculate the selected view position
    // after not scrolling out of bounds when it should.
    private int
            extraChildren = 0,
            decoratedChildWidth = 0,
            decoratedChildHeight = 0,
            firstVisiblePosition = 0,
            itemsNumber = 0,
            lastVisiblePosition = 0,
            lastTopY = 0;

    private int virtualTop = 0, virtualBottom, virtualPosition, originalPosition;

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

        drawChildren(NONE, recycler);

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
        int maxScroll = (getItemCount() - 1) * (decoratedChildHeight);

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
        if (dy > 0) { // adding items at BOTTOM -> removing at top
            for (int i = 0; i < max; i++) {
                View v = getChildAt(i);
                float itemLowerBorder = v.getY() + decoratedChildHeight - dy;
                if (itemLowerBorder < -(decoratedChildHeight * extraChildren)) {
                    toRecycle[index++] = v;
                }
            }
        } else {     // adding items at TOP -> removing at bottom
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
                if (v == selectedView) {
                    Log.v(TAG, "scrollVerticallyBy: # They tried to make me go to rehab, I said, no, no, no.. #");
                    continue;
                }
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
        View item = getChildAt(getChildCount() - 1);

        int bottomMostPosition = getChildCount() == 0 ? 0 : getPosition(item);
        Log.i(TAG, "drawChildren: bottomMostPosition> " + bottomMostPosition + "|" + getChildCount());
        if (item == selectedView) {
            if (getChildCount() > 1) {
                bottomMostPosition = getPosition(getChildAt(getChildCount() - 2));
            }
        }

        int from = 0;
        if (newItemPosition == BOTTOM) {
            from = Math.min(getItemCount(), bottomMostPosition == 0 ? 0 : bottomMostPosition + 1);
        }
        if (newItemPosition == NONE) {
            from = firstVisiblePosition;
        }
        if (newItemPosition == TOP) {
            from = Math.max(0, firstVisiblePosition - extraChildren);
        }


        if (from == getItemCount()) {
            Log.v(TAG, "drawChildren: NOT DRAWING ANYTHING. [Adding items after last adapter position.]");
            return;
        }

        lastVisiblePosition = Math.min(firstVisiblePosition + itemsNumber + 2, getItemCount());

        item = getChildAt(0);
        if (item == selectedView) {
            if (getChildCount() > 1) {
                item = getChildAt(1);
            }
        }

        int to = newItemPosition == BOTTOM || newItemPosition == NONE ?
                lastVisiblePosition + (decoratedChildHeight * extraChildren) : getPosition(item);

        Log.e(TAG, "drawChildren: firstVisiblePosition: " + firstVisiblePosition);
        Log.e(TAG, "_drawChildren: lastVisiblePosition: " + lastVisiblePosition);
        Log.i(TAG, "drawChildren: from: " + from + " -> to: " + to + " > " + getItemText(item));
        Log.i(TAG, "_drawChildren: child count: " + getChildCount());

        if (from > to) {
            Log.v(TAG, "drawChildren: NOT DRAWING ANYTHING. [from > to]");
            return;
        }

        int reversePosition = 0;
        for (int i = from; i < to; i++) {
            if (i == selectedPosition) {
                if (newItemPosition != NONE) {
                    Log.i(TAG, "drawChildren: skipping view at position: " + i);
                    continue;
                }
            }
            int inbetween = 0; // space between items | if item decorations wouldn't be enough
            int r, t, b, l;
            t = i * decoratedChildHeight + (inbetween * (i + 1)) - lastTopY;
            b = (i + 1) * (decoratedChildHeight + inbetween) - lastTopY;
            if (newItemPosition == BOTTOM || newItemPosition == NONE) {
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

            Log.i(TAG, "drawChildren: item: " + item + "/" + selectedView);


            l = (int) Math.pow(2, 6 - i) * 3;

            int lpWidth = item.getLayoutParams().width;
            r = lpWidth == -1 ? getWidth() : lpWidth;

            if (newItemPosition == BOTTOM || newItemPosition == NONE) {
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
            selectedView.setTag(TAG_INITIAL_POSITION, null);
        } else {
            itemView.getLayoutParams().width = itemCollapsedSelectedWidth;
            setInitialPosition(itemView);
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
        } else {
            selectedView = null;
        }
    }

    private void setInitialPosition(View v) {
        int pos = getPosition(v) * decoratedChildHeight;
        v.setTag(TAG_INITIAL_POSITION, pos);
        originalPosition = pos;
        virtualPosition = virtualTop + originalPosition;
        Log.v(TAG, "setInitialPosition: " + pos + ", " + v.getY() + ", " + (virtualPosition - virtualTop));
    }

    private String getItemText(View v) {
        if (!(v instanceof LinearLayout)) {
            return "BAD VIEW!";
        }
        LinearLayout layout = (LinearLayout) v;
        TextView tv = (TextView) layout.getChildAt(0);
        return tv.getText().toString();
    }

    // TODO: 4/26/18 bottom limit (getHeight()) should be dynamic.
    @Override
    public void offsetChildrenVertical(int dy) {
        boolean scrollToEnd = dy < 0, addItemsAtEnd = scrollToEnd;
        if (dy == 0) {
            return;
        }

        virtualTop += dy;
        virtualBottom = virtualTop + getHeight();

        /*
         *          # offset children logic #
         *
         *  parse the child views. > if iterated child
         *  is not the selected one, use the default
         *  behaviour (scroll as if nothing happened)
         *  >> if child is the selected one, let it
         *  scroll normally inside the visible region.
         *  (if its virtual position is between the
         *  virtual top and bottom)
         *  > if the child goes outside the bounds,
         *  don't let it (adapt dy for that view).
         *  > if dy is too big for the child to fully
         *  disappear, adapt it to the right value so
         *  that the view will stick to the top or
         *  bottom and won't slide outside of bounds.
         *  > when scrolling from outside the bounds,
         *  (the original position would be outside)
         *  don't scroll the sticky view unless it's
         *  at its original position inside the list.
         *  for this to happen, dy is forced to be 0
         *  if the original position is outside the
         *  visible bounds. dy will be adapted in this
         *  case as well, so that there will be no
         *  differences between the current position
         *  and its original position (these can be
         *  perceived as blank spaces between the
         *  selected view and the next/previous one,
         *  and/or the view will overlap the previous
         *  or next view, depending on scroll direction)
         *
         *  >> the virtual points are dynamically
         *  calculated and change as the viewport is
         *  scrolled. virtualTop changes by adding the
         *  original dy (and not the computed offset for
         *  the selected view!) virtualBottom depends on
         *  virtualTop. virtualPosition is derived
         *  from virtualTop at the beginning of the
         *  view selection process, but as the views
         *  are scrolled, the calculated offset for
         *  the selected view is added to its value
         *  so that we can compute the difference
         *  between the total amount of scroll and
         *  the adapted one, applied only to the
         *  selected view. based on this difference,
         *  we can decide when to apply limits to dy
         *  so that the view will move in a controlled
         *  fashion, its movement would be smooth and
         *  there will bee no blank spaces or overlaps.
         */


        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v != selectedView) {
                v.offsetTopAndBottom(dy);
            } else {
                int offset = 0;
                if (virtualPosition + decoratedChildHeight < virtualBottom && virtualPosition > 0) {
                    Log.d(TAG, "offsetChildrenVertical: v.getY() + dy: " + v.getY() + dy + " virtualPos: " + virtualPosition);
                    offset = dy;
                } else {
                    offset = dy;
                    if (scrollToEnd) {
                        if (virtualPosition - virtualTop < originalPosition) {
                            // this happens when the view is at the bottom
                            // and we want to keep it there when the original
                            // position would not enter the visible bounds
                            offset = 0;
                        } else {
                            if (virtualPosition - virtualTop == originalPosition) {
                                // this happens when the view is inside the visible bounds
                                offset = dy;
                            } else { // virtualPosition - virtualTop > originalPosition
                                // this happens when original position is about to get inside
                                // of visible bounds. applying the full value of dy would shift
                                // the selected view from the bottom even if it's not at the exact
                                // original location, resulting in the appearance of blank space
                                // and views overlapping. based on dy, the difference can vary
                                // between 1 and a lot of pixels. we need to limit dy so that
                                // when the original position comes into view, it will be fully
                                // covered by the sticky selected view.
                                //
                                // quite an edge case :)
                                Log.w(TAG, "offsetChildrenVertical: " + dy + "/" + (virtualPosition - virtualTop) + "/" + originalPosition + "/" + v.getY());
                                // dy is already applied to virtualTop
                                offset = originalPosition - (virtualPosition - virtualTop);
                            }
                        }
                        if (v.getY() + dy <= 0) {
                            // if scrolling towards bottom, dy negative, adding items at top
                            // this keeps the view from going offscreen.
                            Log.i(TAG, "offsetChildrenVertical: VIEW WILL GET OUT OF BOUNDS." + v.getY() + "/" + dy);
                            offset = (int) -v.getY();
                        }
                    } else {
                        // same as the previous case, if the following is true,
                        // the selected view is at its original position so
                        // we can scroll normally
                        if (virtualPosition - virtualTop == originalPosition) {
                            offset = dy;
                        }
                        // if the view is coming next to its original position
                        // while scrolling from outside the bounds, adapt the
                        // dy value so that there will be no empty space and
                        // overlaps
                        if (virtualPosition - virtualTop < originalPosition) {
                            offset = originalPosition - (virtualPosition - virtualTop);
                        }
                        // if the original position is outside of the bounds,
                        // and it will remain there after applying dy, do not
                        // move the sticky view from top
                        if (virtualPosition - virtualTop > originalPosition) {
                            offset = 0;
                        }
                        // if scrolling towards top, dy is positive.
                        // new items are added to the top of the list.
                        if (v.getY() + decoratedChildHeight + dy > getHeight()) {
                            // this blocks the view at the bottom
                            int tooMuch = (int) (v.getY() + decoratedChildHeight + dy - getHeight());
                            Log.d(TAG, "offsetChildrenVertical: tooMuch: " + tooMuch);
                            offset = dy - tooMuch;
                        }
                    }
                }
                Log.e(TAG, "offsetChildrenVertical: offset: " + offset);
                virtualPosition += offset;
                v.offsetTopAndBottom(offset);
            }
        }
        Log.v(TAG, "offsetChildrenVertical: dy: " + dy + "  scroll to end " + scrollToEnd);
        Log.i(TAG, "offsetChildrenVertical: " +
                "virtual top, bottom, position, original: "
                + virtualTop + ", "
                + virtualBottom + ", "
                + virtualPosition + ", "
                + originalPosition
        );
    }
}
