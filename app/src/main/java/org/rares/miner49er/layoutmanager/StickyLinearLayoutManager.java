package org.rares.miner49er.layoutmanager;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import lombok.Setter;
import org.rares.miner49er.layoutmanager.postprocessing.ResizePostProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rares
 * @since 29.03.2018
 */

public class StickyLinearLayoutManager
        extends RecyclerView.LayoutManager
        implements ResizeableLayoutManager,
        ResizePostProcessor.PostProcessorValidatorConsumer {

    private final boolean DEBUG = false;

    private final static String tag = StickyLinearLayoutManager.class.getSimpleName() + ":";
    private String usedTag = tag;
    private String TAG = usedTag;

    private static final int BOTTOM = -1;
    private static final int NONE = 0;
    private static final int TOP = 1;

    private int selectedPosition = -1;    // currently adapter _selected_ position

    @Setter
    private int maxItemElevation = 0;

    @Setter
    private int itemCollapsedSelectedWidth = -1, itemCollapsedWidth = -1;

    @Setter
    private ResizePostProcessor.PostProcessorValidator postProcessorValidator = null;

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

    private boolean scrolling = false;
    private boolean selectedViewDetached = false;

    public StickyLinearLayoutManager() {
//        setItemPrefetchEnabled(true);
        usedTag = tag + "[ " + hashCode() + " ]:";
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
    public void setSelectedPosition(int selectedPosition) {
        if (DEBUG)
            Log.d(TAG, "setSelectedPosition() called with: " +
                    "selectedPosition = [" + selectedPosition + "]");
        this.selectedPosition = selectedPosition;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        if (DEBUG)
            Log.v(TAG, "onLayoutChildren: remaining scroll >>> " + state.getRemainingScrollVertical());

        if ((state.willRunSimpleAnimations() || scrolling) && selectedView != null) {
            Log.i(TAG, "onLayoutChildren: detach with for. " +
                    "[simple animations: " + state.willRunSimpleAnimations() + "][scrolling: " + scrolling + "]");
            final int childCount = getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View v = getChildAt(i);
                if (!v.equals(selectedView)) {
                    detachAndScrapView(v, recycler);
                } else {
                    if (DEBUG)
                        Log.w(TAG, "onLayoutChildren: SKIPPED SELECTED VIEW.");
                }
            }
        } else {
            if (selectedView != null) {
                if (DEBUG)
                    Log.e(TAG, "onLayoutChildren: >>> WILL DETACH SELECTED VIEW !!! <<<");
            }
            detachAndScrapAttachedViews(recycler);
            selectedViewDetached = true;
        }

        if (getItemCount() == 0) {
            return;
        }

        if (DEBUG)
            Log.d(TAG, "onLayoutChildren: --------------s-t-a-r-t-------------------------");

        if (decoratedChildWidth == 0) {
            View labRatView = recycler.getViewForPosition(0);
            measureChildWithMargins(labRatView, 0, 0);

            decoratedChildWidth = getDecoratedMeasuredWidth(labRatView);
            decoratedChildHeight = getDecoratedMeasuredHeight(labRatView);
            itemsNumber = Math.min(getItemCount(), getHeight() / decoratedChildHeight);
            removeAndRecycleView(labRatView, recycler);
        }

        drawChildren(NONE, recycler, state);

        if (DEBUG)
            Log.d(TAG, "onLayoutChildren: -------------------e-n-d----------------------");
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int originalDy = dy;
        int itemAddPosition = dy > 0 ? BOTTOM : TOP;
        TAG = usedTag + (dy > 0 ? " v " : " ^ ");
        if (DEBUG)
            Log.w(TAG, "scrollVerticallyBy: " +
                    "[dy: " + dy + "]" +
                    "[lastTopY: " + lastTopY + "]" +
                    "[scrollRemaining: " + state.getRemainingScrollVertical() + "]" +
                    "[firstVisiblePosition: " + firstVisiblePosition + "]"
            );

        int maxScroll = (int) ((getItemCount() - 1) * (decoratedChildHeight)/* - (0.5 * decoratedChildHeight)*/);

        if (DEBUG)
            Log.d(TAG, "scrollVerticallyBy: max scroll: " + maxScroll);

        // this fixes (over)scrolling to top
        if (lastTopY + dy <= 0) {
            dy = -lastTopY;
        }

        if (DEBUG)
            Log.w(TAG, "scrollVerticallyBy: [dy: " + dy + "]");


//      virtual space at the end
        if (lastTopY + dy + decoratedChildHeight >= maxScroll) {
            dy = maxScroll - decoratedChildHeight - lastTopY;
        }

        // we're not scrolling when remaining
        // vertical scroll reported by the rv
        // state is 0 or when we _actually_
        // scroll less than the original dy
        scrolling = state.getRemainingScrollVertical() != 0 && Math.abs(dy) >= Math.abs(originalDy);

        if (dy == 0) {
            if (DEBUG)
                Log.d(TAG, "scrollVerticallyBy: >>> returning dy = 0;");
            return 0;
        }

        lastTopY += dy;

//        if (DEBUG)
//            Log.i(TAG, "scrollVerticallyBy: " +
//                    " >>> firstVisiblePosition: " + firstVisiblePosition +
//                    " lastTopY: " + lastTopY
//            );

        final int max = getChildCount();// TODO: 4/21/18 clean these fors up + refactor|extract methods
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
                if (v.equals(selectedView)) {
//                    if (DEBUG)
//                        Log.v(TAG, "scrollVerticallyBy: # They tried to make me go to rehab, I said, no, no, no.. #");
                    continue;
                }
                removeAndRecycleView(v, recycler);
                String text = getItemText(v);
//                if (DEBUG)
//                    Log.d(TAG, "scrollVerticallyBy: " +
//                            " XXX removing view: " + text +
//                            " item top border: " + (v.getY() - dy) +
//                            " item lower border: " + (v.getY() + decoratedChildHeight - dy) +
//                            " children: " + getChildCount());
            } else {
                break;
            }
        }

        offsetChildrenVertical(-dy);
        drawChildren(itemAddPosition, recycler, state);
        if (DEBUG)
            Log.d(TAG, "scrollVerticallyBy() >>> returned dy: " + dy);
        return dy;
    }

    @Override
    public void collectAdjacentPrefetchPositions(
            int dx, int dy,
            RecyclerView.State state,
            LayoutPrefetchRegistry layoutPrefetchRegistry
    ) {
//        if(DEBUG)
//            Log.i(TAG, "collectAdjacentPrefetchPositions: ---------------------------------------------- ");
//
//        boolean listGoingDown = dy > 0;
//        View lastView = getChildAt(getChildCount() - 1);
//
//        int lastPos = lastView == null ? 0 : getPosition(lastView);
//        if (DEBUG)       //
//            Log.i(TAG, "collectAdjacentPrefetchPositions: " + firstVisiblePosition + " -> " + lastPos);
//        int from = getChildCount() == 0 ? 0 : (listGoingDown ?
//                Math.min(lastPos + 1, state.getItemCount()) :
//                Math.max(0, getPosition(getChildAt(0)) - 1));
//
//        int prefetchNumber = 1;
//        int itemsNumber = dy / decoratedChildHeight * prefetchNumber;
//
//        if (listGoingDown) {
//            if (DEBUG)           //
//                Log.i(TAG, "V : from " + from + " to " + Math.max(from + 1, from + itemsNumber));
//            for (int i = from, addedItems = 0; i < Math.min(state.getItemCount(), Math.max(from + 1, from + itemsNumber)); i++, addedItems++) {
//                if (DEBUG)               //
//                    Log.v(TAG, "fetching position: " + i + " [already added items: " + addedItems + "]");
//                layoutPrefetchRegistry.addPosition(i, addedItems * decoratedChildHeight);
//                if (addedItems == prefetchNumber) {
//                    break;
//                }
//            }
//        } else {
//            if (DEBUG)           //
//                Log.i(TAG, "^ : from " + from + " to " + Math.max(from - 1, from + itemsNumber));
//            for (int i = from, addedItems = 0; i > Math.max(0, Math.min(from - 1, from + itemsNumber)); i--, addedItems++) {
//                if (DEBUG)               //
//                    Log.v(TAG, "fetching position: " + i + " [already added items: " + addedItems + "]");
//                layoutPrefetchRegistry.addPosition(i, addedItems * decoratedChildHeight);
//                if (addedItems == prefetchNumber) {
//                    break;
//                }
//            }
//        }
//        if (DEBUG)
//            Log.i(TAG, "collectAdjacentPrefetchPositions: ----------------------------------------------");
    }

    private void keepSelectedViewDrawn(RecyclerView.Recycler recycler) {
        if (DEBUG)
            Log.e(TAG, "keepSelectedViewDrawn: >>>> " + hashCode());
        if (selectedView == null) {
            if (selectedPosition == -1) {
                if (DEBUG)
                    Log.w(TAG, "keepSelectedViewDrawn: no selected view.");
                return;
            } else {
                selectedView = recycler.getViewForPosition(selectedPosition);
            }
        }
        if (DEBUG)
            Log.i(TAG, "keepSelectedViewDrawn: " + selectedView.getY() + " selected position: " + selectedPosition);
        measureChildWithMargins(selectedView, 0, 0);
        int
                x = 0,
                y = (int) selectedView.getY(),
                w = selectedView.getLayoutParams().width,
                h = y + selectedView.getLayoutParams().height;
        layoutDecorated(selectedView, x, y, w, h);
    }

    private void drawChildren(
            int newItemPosition,
            RecyclerView.Recycler recycler,
            RecyclerView.State state
    ) {

        String logDirection = " = ";
        if (newItemPosition == BOTTOM) {
            logDirection = " v ";
        }
        if (newItemPosition == TOP) {
            logDirection = " ^ ";
        }
        TAG = usedTag + logDirection;

        if (DEBUG)
            Log.wtf(TAG, "-------------------------------------------------------------------start");

        if (DEBUG) {
            Log.v(TAG, "last top y: " + lastTopY);
            Log.d(TAG, "drawChildren: RV STATE > " + state);
            Log.v(TAG, "drawChildren: item count: " + getItemCount() + "; child count: " + getChildCount());
            for (int i = 0; i < getChildCount(); i++) {
                View iv = getChildAt(i);
                Log.v(TAG, "drawChildren: child at " + i + " [" + getItemText(iv) + "] selected view > " + iv.equals(selectedView));
            }
            Log.v(TAG, "drawChildren: spare items: " + recycler.getScrapList().size());
            for (int i = 0; i < recycler.getScrapList().size(); i++) {
                RecyclerView.ViewHolder viewHolder = recycler.getScrapList().get(i);
                ViewGroup group = (ViewGroup) viewHolder.itemView;
                Log.v(TAG, "drawChildren: view holder: #" + i + " [" + getItemText(group) + "] view holder > selected view " + (viewHolder.itemView.equals(selectedView)));
            }

            Log.v(TAG, "drawChildren: " +
                    "SELECTED POSITION: " + selectedPosition +
                    " SELECTED VIEW: " + selectedView +
                    " (" + getItemText(selectedView) + ")");
        }
        try {
            // first of all, update first visible position.
            firstVisiblePosition = lastTopY / decoratedChildHeight;

            View item = getChildAt(getChildCount() - 1);


            int bottomMostPosition = getChildCount() == 0 ? 0 : getPosition(item);
            if (DEBUG)
                Log.i(TAG, "drawChildren: bottomMostPosition> " + bottomMostPosition + "|" + getChildCount());
            if (item != null && item.equals(selectedView)) {
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
                if (DEBUG)
                    Log.v(TAG, "drawChildren: NOT DRAWING ANYTHING. [Adding items after last adapter position.]");
                return;
            }

            lastVisiblePosition = Math.min(firstVisiblePosition + itemsNumber + 2, getItemCount());

            item = getChildAt(0);
            if (item == null) {     // select first item, scroll upwards so that it remains on top, then refresh and scroll down -> npe
                if (DEBUG)
                    Log.w(TAG, "drawChildren: ITEM IS NULL!");
                for (int i = 0; i < getChildCount(); i++) {
                    if (DEBUG)
                        Log.i(TAG, "drawChildren: > " + i + " " + getChildAt(i));
                }
            }
            if (item != null && item.equals(selectedView)) {
                if (DEBUG)
                    Log.i(TAG, "drawChildren: item is selected view." + (selectedView == null ? " sv null " : " sv not null "));
                if (getChildCount() > 1) {
                    item = getChildAt(1);
                }
            }

            if (DEBUG)
                Log.e(TAG, "drawChildren: firstVisiblePosition: " + firstVisiblePosition);

            int to = newItemPosition == BOTTOM || newItemPosition == NONE ?
                    lastVisiblePosition + (decoratedChildHeight * extraChildren) : getPosition(item);   // FIXME: 8/28/18 <<<<

            if (DEBUG)
                Log.e(TAG, "_drawChildren: lastVisiblePosition: " + lastVisiblePosition);
            if (DEBUG)
                Log.i(TAG, "drawChildren: from: " + from + " -> to: " + to + " > " + getItemText(item));
            if (DEBUG)
                Log.i(TAG, "_drawChildren: child count: " + getChildCount());

            if (from > to) {
                if (DEBUG)
                    Log.v(TAG, "drawChildren: NOT DRAWING ANYTHING. [from > to]");
                return;
            }

            int reversePosition = 0;
            for (int i = from; i < to; i++) {
                if (i == selectedPosition) {
                    // reminder: if i take this out,
                    // the selected view will not be
                    // drawn correctly. i need to add
                    // it at its place, not at the
                    // beginning or end
                    if (newItemPosition != NONE) {
                        if (DEBUG)
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
                        if (DEBUG)
                            Log.e(TAG, "v drawChildren: item is out of view bounds." +
                                    " will not draw position #" + i + " t=" + t);
                        return;
                    }
                } else {
                    if (b < -decoratedChildHeight * extraChildren) {
                        if (DEBUG)
                            Log.e(TAG, "drawChildren: ^ item is out of view bounds." +
                                    " will not draw position #" + i + " b=" + b
                            );
                        return;
                    }
                }

                /*experimental*/
                if (/*state.willRunSimpleAnimations() && */i == selectedPosition) {
                    // TODO: 8/29/18 swap selected view with new view? [contents]
                    Log.e(TAG, "drawChildren: " + selectedPosition + " " + selectedView + " scrolling? " + scrolling);
                    item = selectedView;
//                    continue;
                } else {
                    item = recycler.getViewForPosition(i);
                }


                l = (int) Math.pow(2, 6 - i) * 3;

                int lpWidth = item.getLayoutParams().width;
                r = lpWidth == -1 ? getWidth() : lpWidth;

                if (newItemPosition == BOTTOM || newItemPosition == NONE) {
                    addView(item);
                } else {
                    // when first item in list is selected and sticky, adding items after it.
                    if (selectedPosition == 0) {
                        reversePosition++;
                    }
                    addView(item, reversePosition++);
                }

                measureChildWithMargins(item, 0, 0);

                layoutDecoratedWithMargins(item, 0, t, r, b);

                if (selectedPosition == i && item.equals(selectedView)) {
                    selectedViewDetached = false;
                }
//            if(selectedPosition!=-1 && selectedView.is not showing...)
//            keepSelectedViewDrawn(recycler);

                if (newItemPosition != NONE && postProcessorValidator != null) {
                    postProcessorValidator.validateItemPostProcess(
                            item,
                            item.getMeasuredWidth() <= itemCollapsedSelectedWidth,
                            item == selectedView);
                }

//            if (DEBUG)
//                Log.d(TAG, "drawChildren: newly added view: " + text +
//                        "; position: " + (reversePosition + newItemPosition == TOP ? -1 : 0) +
//                        "; children: " + getChildCount());
                if (DEBUG)
                    Log.i(TAG, "drawChildren: " + getItemText(item) +
                            " adapter position: " + i +
                            (i == selectedPosition ? " [selected] " : "") +
                            " l: " + 0 + "; r: " + r + "; t: " + t + "; b: " + b +
                            "; lastTopY: " + lastTopY +
                            "; rv height: " + getHeight()
                    );
            }
            if (DEBUG)
                Log.wtf(TAG, "---------------------------------------------------------------------end");
        } finally {
            if (selectedViewDetached && selectedView != null) {

                addView(selectedView); // ??
                measureChildWithMargins(selectedView, 0, 0);
                layoutDecoratedWithMargins(selectedView,
                        (int) selectedView.getX(),
                        (int) selectedView.getY(),
                        itemCollapsedSelectedWidth,
                        decoratedChildHeight);
                selectedViewDetached = false;
            }
        }
    }

    public List<ItemAnimationDto> resizeSelectedView(View itemView, boolean expandToMatchParent) {
        boolean animationEnabled = true;
        List<ItemAnimationDto> animatedItems = new ArrayList<>();

        int elevation;
        int width;

        if (expandToMatchParent) {
            if (!animationEnabled) {
                itemView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(0);
                }
            }
            elevation = 0;
            width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            setInitialPosition(itemView);
            if (!animationEnabled) {
                itemView.getLayoutParams().width = itemCollapsedSelectedWidth;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(maxItemElevation);
                }
            }
            elevation = maxItemElevation;
            width = itemCollapsedSelectedWidth;
        }

        if (animationEnabled) {
            animatedItems.add(new ItemAnimationDto(itemView, elevation, width));
        }

        if (selectedView == null) {
//            requestLayout();
            selectedView = itemView;
            return animatedItems;
        }

        if (itemView != selectedView) {
            if (!animationEnabled) {
                selectedView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    selectedView.setElevation(0);
                }
            }
            if (animationEnabled) {
                animatedItems.add(new ItemAnimationDto(selectedView, 0, ViewGroup.LayoutParams.MATCH_PARENT));
            }
//            requestLayout();
            selectedView = itemView;
        } else {
            selectedView = null;
        }
        return animatedItems;
    }

    private void setInitialPosition(View v) {
        originalPosition = getPosition(v) * decoratedChildHeight;
        virtualPosition = virtualTop + originalPosition;
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
    // TODO: 5/3/18 add support for different item heights.
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

        int childCount = getChildCount();
        boolean selectedChildProcessed = false;
        for (int i = 0; i <= childCount; i++) {
            View v;
            if (i == childCount) {
                if (selectedView != null && selectedPosition != -1 && !selectedChildProcessed) {
//                    if (DEBUG)
//                        Log.i(TAG, "offsetChildrenVertical: not forgetting about the selected view.");
                    v = selectedView;
                } else {
                    if (selectedChildProcessed) {
//                        if (DEBUG)
//                            Log.d(TAG, "offsetChildrenVertical: work done and selected view was processed.");
                    } else {
//                        if (DEBUG)
//                            Log.wtf(TAG, "offsetChildrenVertical: work done and the selected view didn't get moved.");
                    }
                    break;
                }
            } else {
                v = getChildAt(i);
            }
            if (v != selectedView) {
                v.offsetTopAndBottom(dy);
            } else {
                selectedChildProcessed = true;
                int offset = 0;
                if (virtualPosition + decoratedChildHeight < virtualBottom && virtualPosition > 0) {
//                    if (DEBUG)
//                        Log.d(TAG, "offsetChildrenVertical: v.getY() + dy: " + v.getY() + dy + " virtualPos: " + virtualPosition);
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
//                                if (DEBUG)
//                                    Log.w(TAG, "offsetChildrenVertical: " + dy + "/" + (virtualPosition - virtualTop) + "/" + originalPosition + "/" + v.getY());
                                // dy is already applied to virtualTop
                                offset = originalPosition - (virtualPosition - virtualTop);
                            }
                        }
                        if (v.getY() + dy <= 0) {
                            // if scrolling towards bottom, dy negative, adding items at top
                            // this keeps the view from going offscreen.
//                            if (DEBUG)
//                                Log.i(TAG, "offsetChildrenVertical: VIEW WILL GET OUT OF BOUNDS." + v.getY() + "/" + dy);
                            offset = (int) -v.getY();
                        }
                    } else {
                        // if scrolling towards top, dy is positive.
                        // new items are added to the top of the list.

                        // same as the previous case, if the following is true,
                        // the selected view is at its original position so
                        // we can scroll normally
                        if (virtualPosition - virtualTop == originalPosition) {
                            offset = dy;
                            // unless the selectedView is the first, in which
                            // case we don't continue the scroll.
                            if (selectedPosition == 0 && selectedView.getY() == 0) {
                                offset = 0;
                            }
                        }
                        // if the view is coming next to its original position
                        // while scrolling from outside the bounds, adapt the
                        // dy value so that there will be no empty space and
                        // overlaps
                        if (virtualPosition - virtualTop < originalPosition) {
                            offset = originalPosition - (virtualPosition - virtualTop);
                            // unless the selectedView is the first, in which
                            // case we don't continue the scroll.
                            if (selectedPosition == 0 && selectedView.getY() == 0) {
                                return;
                            }
                        }
                        // if the original position is outside of the bounds,
                        // and it will remain there after applying dy, do not
                        // move the sticky view from top
                        if (virtualPosition - virtualTop > originalPosition) {
                            offset = 0;
                        }
                        // this blocks the view at the bottom
                        if (v.getY() + decoratedChildHeight + dy > getHeight()) {
                            int tooMuch = (int) (v.getY() + decoratedChildHeight + dy - getHeight());
//                            if (DEBUG)
//                                Log.d(TAG, "offsetChildrenVertical: tooMuch: " + tooMuch);
                            offset = dy - tooMuch;
                        }
                    }
                }
                if (DEBUG)
                    Log.e(TAG, "offsetChildrenVertical: offset: " + offset);
                virtualPosition += offset;
                v.offsetTopAndBottom(offset);
            }
        }
        if (DEBUG)
            Log.v(TAG, "offsetChildrenVertical: dy: " + dy + "  scroll to end " + scrollToEnd);
//        int selPos = -44444444;
//        if (selectedView != null) {
//            selPos = (int) selectedView.getY();
//        }
//        if (DEBUG)
//            Log.i(TAG, "offsetChildrenVertical: " +
//                    "virtual top, bottom, position, original: "
//                    + virtualTop + ", "
//                    + virtualBottom + ", "
//                    + virtualPosition + ", "
//                    + originalPosition + ", "
//                    + selPos
//            );
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        // reset state on adapter change.
        resetState(true);
        if (DEBUG)
            Log.e(TAG, "onAdapterChanged: RESET STATE FROM ADAPTER CHANGE!!!");
    }

    public void resetState(boolean resetSelectedView) {
        if (resetSelectedView) {
            selectedView = null;
            selectedPosition = -1;
        }
        lastTopY = 0;
    }


    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
/*        if (DEBUG)
            Log.i(TAG, "smoothScrollToPosition: called with position " + position);
        Bundle bundle = determineEdges(position);
        position = bundle.getInt("position");

        final boolean firstGoingDown = bundle.getBoolean("firstGoingDown");
        final boolean lastGoingUp = bundle.getBoolean("lastGoingUp");

        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                final int dx = calculateDxToMakeVisible(targetView, getHorizontalSnapPreference());
                int dy = calculateDyToMakeVisible(targetView, getVerticalSnapPreference());

                if (DEBUG)
                    Log.i(TAG, "onTargetFound: " +
                            " dy: " + dy +
                            " decoratedChildHeight: " + decoratedChildHeight +
                            " lastTopY: " + lastTopY +
                            " getHeight(): " + getHeight());

                if (firstGoingDown) {
                    dy += decoratedChildHeight;
                }

                if (lastGoingUp) {
                    dy -= decoratedChildHeight; // TODO: 8/27/18 see why the last item is scrolled more (a few px) than it should
                }

                final int distance = (int) Math.sqrt(dx * dx + dy * dy);
                final int time = calculateTimeForDeceleration(distance);
                if (time > 0) {
                    action.update(-dx, -dy, time, mDecelerateInterpolator);
                }
            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                if (getChildCount() == 0) {
                    return null;
                }
                final int firstChildPos;
                if (getChildAt(0) == selectedView) {
                    firstChildPos = getPosition(getChildAt(1));
                } else {
                    firstChildPos = getPosition(getChildAt(0));
                }
                final int direction = targetPosition < firstChildPos ? -1 : 1;

                return new PointF(0, direction);
            }

        };


        if (DEBUG)
            Log.i(TAG, "smoothScrollToPosition: scrollto: " + position);
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);*/
    }

    /**
     * Because Recycler View does not scroll towards
     * a position if it can already find it on screen,
     * we need to scroll to a view which is not visible,
     * that being the first before or after the target
     * position, depending on the position of the selected
     * view (topmost or bottommost).
     *
     * @param position the target position we want to scroll towards
     * @return a bundle that contains the final position to scroll towards,
     * and also two booleans, that show if the first position (0) or last
     * position (getItemCount()) are the selected ones and we need to scroll
     * towards those positions
     * <ul>string keys for getting the aforementioned information
     * <li>position</li>
     * <li>firstGoingDown</li>
     * <li>lastGoingUp</li>
     * </ul>
     */
    private Bundle determineEdges(int position) {
        boolean firstGoingDown = false, lastGoingUp = false;
        if (position == selectedPosition) {
            if (DEBUG)
                Log.i(TAG, "determineEdges: position = selectedPosition = " + position);
            byte sd = determineScrollDirection(position);
            if (sd != 0) {
                if (sd < 0) { // new items appear at top
                    if (tryDecrementPosition(position)) {
                        position--;
                    } else {
                        position++;
                        firstGoingDown = true;
                    }
                } else { // new items appear at bottom
                    if (tryIncrementPosition(position)) {
                        position++;
                    } else {
                        position--;
                        lastGoingUp = true;
                    }
                }
            }
        }
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putBoolean("firstGoingDown", firstGoingDown);
        bundle.putBoolean("lastGoingUp", lastGoingUp);
        if (DEBUG)
            Log.d(TAG, "determineEdges() returned: " + bundle);
        return bundle;
    }

    private byte determineScrollDirection(int position) {
        byte toReturn = 0;
        int childCount = getChildCount();
        View firstView = getChildAt(0);
        if (firstView == selectedView) {
            if (childCount > 1) {
                firstView = getChildAt(1);
            }
        }
        View lastView = getChildAt(childCount - 1);
        if (lastView == selectedView) {
            if (lastView != firstView) {
                if (childCount > 2) {
                    lastView = getChildAt(childCount - 2);
                }
            }
        }
        int firstVisibleAdapterPosition = getPosition(firstView);
        int lastVisibleAdapterPosition = getPosition(lastView);
        if (DEBUG)
            Log.i(TAG, "determineScrollDirection: " + firstVisibleAdapterPosition + "|" + lastVisibleAdapterPosition);
        if (position < firstVisibleAdapterPosition) {
            // move list from up to down
            toReturn = -1;
        }
        if (position > lastVisibleAdapterPosition) {
            // move list from down to up
            toReturn = 1;
        }
        // do not care what happens if selected position is between these two
        if (DEBUG)
            Log.i(TAG, "determineScrollDirection: returning " + toReturn);
        return toReturn;
    }

    private boolean tryIncrementPosition(int position) {
        return getItemCount() > position + 1;

    }

    private boolean tryDecrementPosition(int position) {
        return position > 0;
    }
}
