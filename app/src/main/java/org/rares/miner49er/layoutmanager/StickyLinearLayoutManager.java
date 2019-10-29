package org.rares.miner49er.layoutmanager;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.layoutmanager.postprocessing.ResizePostProcessor;
import org.rares.miner49er.util.TextUtils;
import org.rares.ratv.rotationaware.RotationAwareTextView;

import java.util.ArrayList;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING;

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


    /**
     * <strong>original position</strong> -> natural position = adapter position * item height <br />
     * <strong>virtual position</strong> = selectedView.getY() = virtualTop + originalPosition
     * at the beginning, but it changes with offset operations; it also changes
     * when the selected view is not fully visible, and the user selects it
     * (see {@link #assureVisibilityInViewport(int, int)}) <br />
     * <strong>virtual top</strong> = the position where the top of the list's scrollable viewport is
     * actually located. changes with scrolling operations. <br />
     * <strong>virtual bottom</strong> = IT IS NOT the point where the bottom of the list's scrollable
     * viewport is actually located; instead, it is the sum of virtualTop and the
     * viewport's height
     */
    private int virtualTop = 0, virtualBottom, virtualPosition, originalPosition;

    private View selectedView = null;

    private boolean scrolling = false;
    private boolean selectedViewDetached = false;

    private List<PreloadSizeConsumer> preloadSizeConsumers = new ArrayList<>();

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
//        if (DEBUG && METHOD_DEBUG)
//            Log.d(TAG, "setSelectedPosition() called with: " +
//                    "selectedPosition = [" + selectedPosition + "]");
        this.selectedPosition = selectedPosition;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        final boolean METHOD_DEBUG = false;

        if (DEBUG && METHOD_DEBUG) {
            Log.v(TAG, "onLayoutChildren: remaining scroll >>> " + state.getRemainingScrollVertical());
        }

        if ((state.willRunSimpleAnimations() || scrolling) && selectedView != null) {
            if (DEBUG && METHOD_DEBUG) {
                Log.i(TAG, "onLayoutChildren: detach with for. " +
                        "[simple animations: " + state.willRunSimpleAnimations() + "][scrolling: " + scrolling + "]");
            }
            final int childCount = getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View v = getChildAt(i);
                if (!v.equals(selectedView)) {
                    detachAndScrapView(v, recycler);
                } else {
                    if (DEBUG && METHOD_DEBUG) {
                        Log.w(TAG, "onLayoutChildren: SKIPPED SELECTED VIEW.");
                    }
                }
            }
        } else {
            if (selectedView != null) {
                if (DEBUG && METHOD_DEBUG) {
                    Log.e(TAG, "onLayoutChildren: >>> WILL DETACH SELECTED VIEW !!! <<<");
                }
            }
            detachAndScrapAttachedViews(recycler);
            selectedViewDetached = true;
        }

        if (getItemCount() == 0) {
            return;
        }

        if (DEBUG && METHOD_DEBUG) {
            Log.d(TAG, "onLayoutChildren: --------------s-t-a-r-t-------------------------");
        }

        if (decoratedChildWidth == 0) {
            View labRatView = recycler.getViewForPosition(0);
            measureChildWithMargins(labRatView, 0, 0);

            decoratedChildWidth = getDecoratedMeasuredWidth(labRatView);
            decoratedChildHeight = getDecoratedMeasuredHeight(labRatView);
//            removeAndRecycleView(labRatView, recycler);

            View pl = labRatView.findViewById(R.id.project_logo);
            View pi = labRatView.findViewById(R.id.project_image);

            if (pl != null) {
                int[] dimensions = {pl.getMeasuredWidth(), pl.getMeasuredHeight(), pi.getMeasuredWidth(), pi.getMeasuredHeight()};

                for (PreloadSizeConsumer psc : preloadSizeConsumers) {
                    psc.onMeasureComplete(dimensions);
                }
            }

            recycler.recycleView(labRatView);
        }

        itemsNumber = Math.min(getItemCount(), getHeight() / decoratedChildHeight);

        if (!state.isMeasuring()) {
            drawChildren(NONE, recycler, state);
            if (DEBUG && METHOD_DEBUG) {
                Log.d(TAG, "onLayoutChildren: " +
                        "height: " + getHeight() + "/" + decoratedChildHeight + "=" + getHeight() / decoratedChildHeight +
                        " cc(" + getChildCount() + ")*ch(" + decoratedChildHeight + "): " + getChildCount() * decoratedChildHeight +
                        " total items: " + itemsNumber + "/" + getItemCount()
                );
            }
        }

        if (DEBUG && METHOD_DEBUG) {
            Log.d(TAG, "onLayoutChildren: -------------------e-n-d----------------------");
        }
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        final boolean METHOD_DEBUG = false;

        int itemAddPosition = dy > 0 ? BOTTOM : TOP;
        TAG = usedTag + (dy > 0 ? " v " : " ^ ");

        // virtual space at the end
        int maxScroll = (int) ((getItemCount() - 1) * (decoratedChildHeight)/* - (0.5 * decoratedChildHeight)*/);

        // no virtual space at the end
        maxScroll = (getItemCount() + 1) * decoratedChildHeight - getHeight();

//        if (DEBUG && METHOD_DEBUG)
//            Log.d(TAG, "scrollVerticallyBy: max scroll: " + maxScroll);

        if (DEBUG && METHOD_DEBUG) {
            Log.w(TAG, "scrollVerticallyBy: " +
                    "[dy: " + dy + "]" +
                    "[height: " + getHeight() + "]" +
                    "[item count * decoratedItemHeight: " + getItemCount() * decoratedChildHeight + "]" +
                    "[maxScroll: " + maxScroll + "]" +
                    "[lastTopY: " + lastTopY + "]" +
                    "[scrollRemaining: " + state.getRemainingScrollVertical() + "]" +
                    "[firstVisiblePosition: " + firstVisiblePosition + "]"
            );
        }

        if (getHeight() > getItemCount() * decoratedChildHeight) {
            return 0;
        }

        // this fixes (over)scrolling to top
        if (lastTopY + dy <= 0) {
            dy = -lastTopY;
        }

//        if (DEBUG && METHOD_DEBUG)
//            Log.w(TAG, "scrollVerticallyBy: [dy: " + dy + "]");

//      virtual space at the end
        if (lastTopY + dy + decoratedChildHeight >= maxScroll) {
            dy = maxScroll - decoratedChildHeight - lastTopY;
        }


        if (dy == 0) {
            if (DEBUG && METHOD_DEBUG) {
                Log.d(TAG, "scrollVerticallyBy: >>> returned dy = 0");
            }
            return 0;
        }

        lastTopY += dy;

//        if (DEBUG && METHOD_DEBUG) {
//            Log.i(TAG, "scrollVerticallyBy: " +
//                    " >>> firstVisiblePosition: " + firstVisiblePosition +
//                    " lastTopY: " + lastTopY
//            );
//        }

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
//                    if (DEBUG && METHOD_DEBUG) {
//                        Log.v(TAG, "scrollVerticallyBy: # They tried to make me go to rehab, I said, no, no, no.. #");
//                    }
                    continue;
                }
                removeAndRecycleView(v, recycler);
//                if (DEBUG && METHOD_DEBUG) {
//                    Log.d(TAG, "scrollVerticallyBy: " +
//                            " XXX removing view: " + TextUtils.getItemText(v) +
//                            " item top border: " + (v.getY() - dy) +
//                            " item lower border: " + (v.getY() + decoratedChildHeight - dy) +
//                            " children: " + getChildCount());
//                }
            } else {
                break;
            }
        }

        offsetChildrenVertical(-dy);
        drawChildren(itemAddPosition, recycler, state);
        if (DEBUG && METHOD_DEBUG) {
            Log.d(TAG, "scrollVerticallyBy() >>> returned dy: " + dy);
        }
        return dy;
    }

    @Override
    public void collectAdjacentPrefetchPositions(
            int dx, int dy,
            RecyclerView.State state,
            LayoutPrefetchRegistry layoutPrefetchRegistry
    ) {/*
        boolean METHOD_DEBUG = false;
        if (DEBUG && METHOD_DEBUG) {
            Log.i(TAG, "collectAdjacentPrefetchPositions: ---------------------------------------------- ");
        }

        boolean listGoingDown = dy > 0;
        View lastView = getChildAt(getChildCount() - 1);

        int lastPos = lastView == null ? 0 : getPosition(lastView);
        if (DEBUG && METHOD_DEBUG) {      //
            Log.i(TAG, "collectAdjacentPrefetchPositions: " + firstVisiblePosition + " -> " + lastPos);
        }
        int from = getChildCount() == 0 ? 0 : (listGoingDown ?
                Math.min(lastPos + 1, state.getItemCount()) :
                Math.max(0, getPosition(getChildAt(0)) - 1));

        int prefetchNumber = 1;
        int itemsNumber = dy / decoratedChildHeight * prefetchNumber;

        if (listGoingDown) {
            if (DEBUG && METHOD_DEBUG) {      //
                Log.i(TAG, "V : from " + from + " to " + Math.max(from + 1, from + itemsNumber));
            }
            for (int i = from, addedItems = 0; i < Math.min(state.getItemCount(), Math.max(from + 1, from + itemsNumber)); i++, addedItems++) {
                if (DEBUG && METHOD_DEBUG) { //
                    Log.v(TAG, "fetching position: " + i + " [already added items: " + addedItems + "]");
                }
                layoutPrefetchRegistry.addPosition(i, addedItems * decoratedChildHeight);
                if (addedItems == prefetchNumber) {
                    break;
                }
            }
        } else {
            if (DEBUG && METHOD_DEBUG) {       //
                Log.i(TAG, "^ : from " + from + " to " + Math.max(from - 1, from + itemsNumber));
            }
            for (int i = from, addedItems = 0; i > Math.max(0, Math.min(from - 1, from + itemsNumber)); i--, addedItems++) {
                if (DEBUG && METHOD_DEBUG) {               //
                    Log.v(TAG, "fetching position: " + i + " [already added items: " + addedItems + "]");
                }
                layoutPrefetchRegistry.addPosition(i, addedItems * decoratedChildHeight);
                if (addedItems == prefetchNumber) {
                    break;
                }
            }
        }
        if (DEBUG && METHOD_DEBUG) {
            Log.i(TAG, "collectAdjacentPrefetchPositions: ----------------------------------------------");
        }
    */
    }

    private void drawChildren(
            int newItemPosition,
            RecyclerView.Recycler recycler,
            RecyclerView.State state
    ) {

        boolean METHOD_DEBUG = false;
        boolean selectedViewRefreshed = false;

        String logDirection = " = ";
        if (newItemPosition == BOTTOM) {
            logDirection = " v ";
        }
        if (newItemPosition == TOP) {
            logDirection = " ^ ";
        }
        TAG = usedTag + logDirection;

        if (DEBUG && METHOD_DEBUG) {
            Log.wtf(TAG, "-------------------------------------------------------------------start");
        }

        long s = System.currentTimeMillis();

        if (DEBUG && METHOD_DEBUG) {
//            Log.v(TAG, "last top y: " + lastTopY);
//            Log.d(TAG, "drawChildren: RV STATE > " + state);
//            Log.v(TAG, "drawChildren: item count: " + getItemCount() + "; child count: " + getChildCount());
//            for (int i = 0; i < getChildCount(); i++) {
//                View iv = getChildAt(i);
//                Log.v(TAG, "drawChildren: child at " + i + " [" + TextUtils.getItemText(iv) + "] selected view > " + iv.equals(selectedView));
//            }
            Log.v(TAG, "drawChildren: spare items: " + recycler.getScrapList().size());
            for (int i = 0; i < recycler.getScrapList().size(); i++) {
                RecyclerView.ViewHolder viewHolder = recycler.getScrapList().get(i);
                ViewGroup group = (ViewGroup) viewHolder.itemView;
//                Log.v(TAG, "drawChildren: viewHolder: #" + i + " [" + TextUtils.getItemText(group) + "] view holder > selected view " + (viewHolder.itemView.equals(selectedView)));
                Log.v(TAG, "drawChildren: viewHolder: #" + i + " [" + TextUtils.getItemText(group) + "] view holder " + viewHolder.hashCode());
            }

//            Log.v(TAG, "drawChildren: " +
//                    "SELECTED POSITION: " + selectedPosition +
//                    " SELECTED VIEW: " + selectedView +
//                    " (" + TextUtils.getItemText(selectedView) + ")");
        }

        // // TODO: 11/8/18: fix item bring to front
        // it works ok unless the selected item is the first
        // or last - repro:
        // disable elevation, uncomment these:
//        if (selectedView != null) {
//            selectedView.bringToFront();
//        }

        try {
            // first of all, update first visible position.
            firstVisiblePosition = lastTopY / decoratedChildHeight;

            View item = getChildAt(getChildCount() - 1);


            int bottomMostPosition = getChildCount() == 0 ? 0 : getPosition(item);
//            if (DEBUG && METHOD_DEBUG)
//                Log.i(TAG, "drawChildren: bottomMostPosition> " + bottomMostPosition + "|" + getChildCount());
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
                if (DEBUG && METHOD_DEBUG) {
                    Log.v(TAG, "drawChildren: NOT DRAWING ANYTHING. [Adding items after last adapter position.]");
                }
                return;
            }

            lastVisiblePosition = Math.min(firstVisiblePosition + itemsNumber + 2, getItemCount());

            // get first item to determine what to
            // add **if scrolling towards top** (i.e
            // adding new items in the list at the
            // top) [-> 1]
            item = getChildAt(0);
            if (DEBUG && METHOD_DEBUG) {
                if (item == null) {     // select first item, scroll upwards so that it remains on top, then refresh and scroll down -> npe
                    Log.w(TAG, "drawChildren: ITEM IS NULL!");  // what does this mean?
                    for (int i = 0; i < getChildCount(); i++) {
                        Log.i(TAG, "drawChildren: > " + i + " " + getChildAt(i));
                    }
                }
            }
            // [1 ->] if the first item is the selected item, try the second view
            if (item != null && item.equals(selectedView)) {
//                if (DEBUG && METHOD_DEBUG)
//                    Log.i(TAG, "drawChildren: item is selected view." + (selectedView == null ? " sv null " : " sv not null "));
                if (getChildCount() > 1) {
                    item = getChildAt(1);
                }
            }

            if (DEBUG && METHOD_DEBUG) {
                Log.e(TAG, "drawChildren: firstVisiblePosition: " + firstVisiblePosition);
            }

            int to = newItemPosition == BOTTOM || newItemPosition == NONE ?
                    lastVisiblePosition + (decoratedChildHeight * extraChildren) : getPosition(item);   // FIXME: 8/28/18 <<<< [possible npe on get position]

            if (DEBUG && METHOD_DEBUG) {
                Log.w(TAG, "drawChildren: lastVisiblePosition: " + lastVisiblePosition);
                Log.i(TAG, "drawChildren: from: " + from + " -> to: " + to + " > " + TextUtils.getItemText(item));
//                Log.i(TAG, "drawChildren: child count: " + getChildCount());
            }

            if (from > to) {
                if (DEBUG && METHOD_DEBUG)
                    Log.v(TAG, "drawChildren: NOT DRAWING ANYTHING. [from > to]");
                return;
            }

            int reversePosition = 0;
            for (int i = from; i < to; i++) {
                long cs = System.currentTimeMillis();
                // the algorithm may say that
                // the item at the selected
                // position item must be added.
                // do not be fooled by its trickery!
                // just skip over the selected
                // view if it is still showing.
                if (i == selectedPosition && !selectedViewDetached) {
                    // reminder: if i take this^ out,
                    // the selected view will not be
                    // drawn correctly. i need to add
                    // it at its place, not at the
                    // beginning or end
                    if (newItemPosition != NONE) {
                        if (DEBUG && METHOD_DEBUG)
                            Log.i(TAG, "drawChildren: skipping view at position: " + i);
                        continue;
                    }
                }

                int r, t, b, l;
                t = i * decoratedChildHeight - lastTopY;

                b = t + decoratedChildHeight;
                if (newItemPosition == BOTTOM || newItemPosition == NONE) {
                    if (t > getHeight() + (decoratedChildHeight * extraChildren)) {
                        if (DEBUG && METHOD_DEBUG) {
                            Log.e(TAG, "v drawChildren: item is out of view bounds." +
                                    " will not draw position #" + i + " t=" + t);
                        }
                        return;
                    }
                } else {
                    if (b < -decoratedChildHeight * extraChildren) {
                        if (DEBUG && METHOD_DEBUG) {
                            Log.e(TAG, "drawChildren: ^ item is out of view bounds." +
                                    " will not draw position #" + i + " b=" + b
                            );
                        }
                        return;
                    }
                }

                if (i == selectedPosition) {
                    if (DEBUG && METHOD_DEBUG) {
                        Log.e(TAG, "drawChildren: " + selectedPosition + " " + selectedView + " scrolling? " + scrolling);
                    }

                    refreshSelectedView(recycler);
                    selectedViewRefreshed = true;
                    item = selectedView;

                    selectedViewDetached = false;
                    // always draw selected item inside the rv viewport
                    // selected item virtual position needs to be updated
//                    Log.i(TAG, "drawChildren: setting virtualPosition t: " + t + " b: " + b);
                    int[] newTb = assureVisibilityInViewport(t, b);
                    t = newTb[0];
                    b = newTb[1];

                } else {
                    item = recycler.getViewForPosition(i);
                }


                l = 0/*(int) Math.pow(2, 6 - i) * 3*/;
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) item.getLayoutParams();
                int lpWidth = lp.width;
                r = lpWidth == -1 ?
                        // for normal items, draw them with
                        // the parent size, if they are to
                        // MATCH their PARENT
                        getWidth() :
                        // for selected items, add margins,
                        // because the margins are omitted
                        // when resizing the selected item
                        // ({@link #resizeSelectedItem()})
                        lpWidth + lp.leftMargin + lp.rightMargin;
                // ^ adding the margins, so that
                // measurement will be done correctly

                if (newItemPosition == BOTTOM || newItemPosition == NONE) {
                    addView(item);
                    // FIXME:
                    //  ^ IllegalStateException:
                    //      Added View has RecyclerView as parent
                    //      but view is not a real child.
                    //      Unfiltered index:8
                } else {
                    // when the first item in the list is
                    // selected and sticky, add items after it.
                    if (selectedPosition == 0) {
                        reversePosition++;
                    }
                    addView(item, reversePosition++);
                }

                if (newItemPosition != NONE) {
                    // only force item state if scrolling
                    // while also expanding/collapsing
                    if (postProcessorValidator != null) {
                        postProcessorValidator.validateItemPostProcess(
                                item,
                                selectedPosition != -1,
                                item == selectedView);
                    }
                }
                // measure and lay out children after the post process validation
                // so that we know we act on correct view positioning.
                if (i == selectedPosition) {
                    measureChildWithMargins(item, 0, 0);
                    // the lm uses layoutDecorated
                    // (and not layoutDecoratedWithMargins)
                    // and adds margins itself
                    layoutDecorated(item,
                            l + lp.leftMargin,
                            t + lp.topMargin,
                            // remember to add the left margin to
                            // the layout call!
                            // right margin is not taken into account here
                            // because of the nature of the selected item
                            // which can be larger than the RV
                            // there probably are situations where
                            // the lp.rightMargin should be taken
                            // into account (e.g. if the selected
                            // item would be smaller than the other ones)
                            lp.width + lp.leftMargin,
                            b - lp.bottomMargin);
                } else {
                    measureChildWithMargins(item, 0, 0);
                    // normal items follow normal rules
                    layoutDecorated(item,
                            l + lp.leftMargin,
                            t + lp.topMargin,
                            r - lp.rightMargin,
                            b - lp.bottomMargin);
                }

//                if (DEBUG && METHOD_DEBUG) {
//                    Log.d(TAG, "drawChildren: newly added view: " + TextUtils.getItemText(item) +
//                            "; position: " + (reversePosition + newItemPosition == TOP ? -1 : 0) +
//                            "; children: " + getChildCount());
//                Log.i(TAG, "drawChildren: " + TextUtils.getItemText(item) +
//                        " adapter position: " + i +
//                        (i == selectedPosition ? " [selected] " : "") +
//                        " l: " + 0 + "; r: " + r + "; t: " + t + "; b: " + b +
//                        "; lastTopY: " + lastTopY +
//                        "; rv height: " + getHeight() +
//                        "; sp: " + selectedPosition +
//                        "; sv null: " + (selectedView == null)
//                );
//                }
                if (DEBUG && METHOD_DEBUG) {
                    Log.e(TAG, "drawChildren: PER ITEM: " + (System.currentTimeMillis() - cs));
                }
            }
        } finally {
//            METHOD_DEBUG = true;
            if (selectedView != null) {
                int l = 0;
                int r = itemCollapsedSelectedWidth;
                int t = (int) selectedView.getY();
                int b = t + decoratedChildHeight;

                // always draw selected item inside the rv viewport
                // selected item virtual position needs to be updated
//                Log.d(TAG, "drawChildren: setting virtualPosition t: " + t + " b: " + b);
                int[] newTb = assureVisibilityInViewport(t, b);
                t = newTb[0];
                b = newTb[1];

                if (selectedViewDetached) {
                    if (DEBUG && METHOD_DEBUG) {
                        Log.d(TAG, "drawChildren: >>> adding selected view, because _someone_ \"forgot\" to add it /!\\");
                    }
                    addView(selectedView);
                    selectedViewDetached = false;
                }

                if (selectedPosition != -1) {
                    if (!selectedViewRefreshed) {
                        refreshSelectedView(recycler);
                    }
                }

                if (scrolling) {
                    RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) selectedView.getLayoutParams();
//                    measureChildWithMargins(selectedView, 0, 0);
                    measureChildWithMargins(selectedView, lp.leftMargin + lp.rightMargin, lp.topMargin + lp.bottomMargin);
                    layoutDecoratedWithMargins(selectedView, l, t, r, b);
//                    layoutDecorated(selectedView,
//                            l + lp.leftMargin,
//                            t + lp.topMargin,
//                            r - lp.rightMargin,
//                            b - lp.bottomMargin);
                }
            }

            if (DEBUG && METHOD_DEBUG) {
                int cc = getChildCount();
                for (int i = 0; i < cc; i++) {
                    View v = getChildAt(i);

//                  int[] location = new int[2];
//                  v.getLocationOnScreen(location);
//                  int x = location[0];
//                  int y = location[1];
                    Log.v(TAG, "drawChildren: [" + i + "][" +
                                    v.getY() + "-" + (v.getY() + decoratedChildHeight) + "][" +
                                    getPosition(v) + "][" +
//                                  x + "," + y + "][" +
                                    v.getWidth() + "][" +
                                    v.getHeight() + "][" +
                                    TextUtils.getItemText(v) + "]" +
                                    (v.equals(selectedView) ? "[" + originalPosition + "] [selected]" : "")
                    );
                }
            }
            if (DEBUG && METHOD_DEBUG) {
                Log.e(TAG, "drawChildren: TOTAL: " + (System.currentTimeMillis() - s));
                Log.wtf(TAG, "---------------------------------------------------------------------end");
            }
        }
    }

    /**
     * Refreshes the selected view with fresh contents
     * because at some points in the layout stage, the
     * selected view is skipped. It is necessary to skip
     * layout for the selected view because it can be
     * replaced by a view which is not laid out as
     * selected (we need larger width, elevation etc.)
     * Replacement can also cause unwanted visual effects.
     * Also, the selected view is kept inside the viewport
     * even when it would normally scroll out of visible
     * bounds. The refresh is made by requesting the new
     * view and just swapping some information to the
     * already existing view. The new view is then recycled.
     *
     * @param recycler the RecyclerView recycler that
     *                 takes care of providing and
     *                 recycling views
     */
    private void refreshSelectedView(RecyclerView.Recycler recycler) {
        boolean METHOD_DEBUG = false;
        // the following block is **very** itemView specific, this will
        // be extracted into some interface + method
//      --------------------------------------------------------------------
        View tempV = recycler.getViewForPosition(selectedPosition);
//      how good would it have been if we'd have access to a viewHolder here...
        if (DEBUG && METHOD_DEBUG) {
            Log.v(TAG, "refreshSelectedView: x_x " + selectedView);
            Log.d(TAG, "refreshSelectedView: x_x " + tempV);
        }
        Drawable background = tempV.getBackground();
        if (background instanceof ColorDrawable) {
            int color = ((ColorDrawable) background).getColor();
            selectedView.setBackgroundColor(color);
        }
        ViewGroup vg = (ViewGroup) selectedView;
        View childView = vg.getChildAt(0);
        if (childView instanceof TextView) {
            ((TextView) childView).setText(TextUtils.getItemText(tempV));
        }
        if (childView instanceof ViewGroup) {
            View v = ((ViewGroup) childView).getChildAt(0);
            if (v instanceof RotationAwareTextView) {
                ((RotationAwareTextView) v).setText(TextUtils.getItemText(tempV));
            }
        }

        if (!tempV.equals(selectedView)) {
            detachView(tempV);
            recycler.recycleView(tempV);
        }
//      --------------------------------------------------------------------
    }


    /**
     * Always draw selected item inside the rv viewport <br />
     * Selected item virtual position needs to be updated <br />
     * <table>
     * <tr>
     * <td>
     * <div style="color:red;">/_!_\</div>
     * </td>
     * <td>
     * <div><strong>VIRTUAL POSITION IS ALSO AFFECTED BY THIS CHANGE SO IT IS ALSO MODIFIED</strong></div>
     * </td>
     * <td>
     * <div style="color:red;">/_!_\</div>
     * </td>
     * </tr>
     * </table>
     *
     * @param top    initial top position
     * @param bottom initial bottom position
     * @return an integer array containing new top (position 0) and bottom (position 1) coordinates
     */
    private int[] assureVisibilityInViewport(int top, int bottom) {
        boolean METHOD_DEBUG = false;
        int t = top, b = bottom;
        int[] topAndBottom = new int[2];
        if (t < 0) {
            t = 0;
            b = t + decoratedChildHeight;
            virtualPosition = t;
        }
        if (b > getHeight()) {
            b = getHeight();
            t = b - decoratedChildHeight;
            virtualPosition = t;
        }

        if (DEBUG && METHOD_DEBUG) {
            Log.v(TAG, "assureVisibilityInViewport: setting virtualPosition to: " + virtualPosition);
        }

        topAndBottom[0] = t;
        topAndBottom[1] = b;
        return topAndBottom;
    }

    public List<ItemAnimationDto> resizeSelectedView(View itemView, boolean expandToMatchParent) {
        boolean animationEnabled = true;
        List<ItemAnimationDto> animatedItems = new ArrayList<>();

        int elevation;
        int width;
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) itemView.getLayoutParams();

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
            width = itemCollapsedSelectedWidth - lp.rightMargin - lp.leftMargin;
        }

        if (animationEnabled) {
            animatedItems.add(new ItemAnimationDto(itemView, elevation, width));
        }

        if (selectedView == null) {
            selectedView = itemView;
//            selectedView.bringToFront();
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
            selectedView = itemView;
//            selectedView.bringToFront();
        } else {
            selectedView = null;
        }
        return animatedItems;
    }

    private void setInitialPosition(View v) {
        final boolean METHOD_DEBUG = false;
        originalPosition = getPosition(v) * decoratedChildHeight;
        virtualPosition = virtualTop + originalPosition;
        if (DEBUG && METHOD_DEBUG) {
            Log.w(TAG, "setInitialPosition: >>> " + v.getY() + "|" + originalPosition + "|" + virtualTop + "|" + virtualPosition);
        }
    }

    // TODO: 4/26/18 bottom limit (getHeight()) should be dynamic.
    // TODO: 5/3/18 add support for different item heights.
    @Override
    public void offsetChildrenVertical(int dy) {
        final boolean METHOD_DEBUG = false;

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
         *  virtual top and bottom, but only if the
         *  original position is equal with its virtual
         *  position)
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
                    if (DEBUG && METHOD_DEBUG) {
                        Log.i(TAG, "offsetChildrenVertical: not forgetting about the selected view.");
                    }
                    v = selectedView;
                } else {
                    if (selectedChildProcessed) {
                        if (DEBUG && METHOD_DEBUG) {
                            Log.d(TAG, "offsetChildrenVertical: work done and selected view was processed.");
                        }
                    } else {
                        if (DEBUG && METHOD_DEBUG) {
                            Log.wtf(TAG, "offsetChildrenVertical: work done and the selected view didn't get moved.");
                        }
                    }
                    break;
                }
            } else {
                v = getChildAt(i);
            }
            if (DEBUG && METHOD_DEBUG) {
                Log.i(TAG, "offsetChildrenVertical: " +
                        "[ y: " + v.getY() + "-" + (v.getY() + decoratedChildHeight) +
                        "][" + i +
                        "][" + v.getWidth() +
                        "][" + TextUtils.getItemText(v) +
                        "]" + (v.equals(selectedView) ? " [selected]" : ""));
            }
            // TODO: 10/11/18 extract this as an interface
            // TODO: 10/11/18 make this more consistent with scroll/position
            View vv = v.findViewById(R.id.project_image);
            if (vv != null) {
                vv.setRotation(vv.getRotation() + 3 * Math.signum(dy));
//                vv.setRotation(vv.getRotation() + virtualBottom*.005F * Math.signum(dy));
//                vv.setPivotY(v.getY() * .45F);
            }

            if (v != selectedView) {
                v.offsetTopAndBottom(dy);
            } else {
                if (DEBUG && METHOD_DEBUG) {
                    Log.i(TAG, "offsetChildrenVertical: originalPosition " + originalPosition);
                }
                selectedChildProcessed = true;
                int offset = 0;

                String offsetStatus = "[svos] "; //selected view offset status

                if (DEBUG && METHOD_DEBUG) {
                    Log.d(TAG, offsetStatus + "offsetChildrenVertical:" +
                            " v.getY() + dy: " + v.getY() + "+" + dy + "=" + (v.getY() + dy) +
                            " virtual position + dy: " + virtualPosition + "+" + dy + "=" + (virtualPosition + dy) +
                            " offset: " + offset +
                            " virtualTop: " + virtualTop +
                            " originalPosition: " + originalPosition +
                            " virtualPos: " + virtualPosition);
                }
                if (dy + virtualPosition + decoratedChildHeight < getHeight()
                        && dy + virtualPosition > 0
                        && virtualPosition == originalPosition
                        && selectedPosition != 0) { // first position is tricky so better skip it here
                    if (DEBUG && METHOD_DEBUG) {
                        Log.i(TAG, offsetStatus + "offsetChildrenVertical: VIEW IN NORMAL POSITION, WITHIN BOUNDS");
                    }
                    offset = dy;
                } else {
                    offset = dy;
                    if (scrollToEnd) {
                        if (virtualPosition - virtualTop < originalPosition) {
                            // this happens when the view is at the bottom
                            // and we want to keep it there when the original
                            // position would not enter the visible bounds
                            if (DEBUG && METHOD_DEBUG) {
                                Log.i(TAG, offsetStatus + "offsetChildrenVertical: BLOCK VIEW AT BOTTOM");
                            }
                            offset = 0;
                        } else {
                            if (virtualPosition - virtualTop == originalPosition) {
                                // this happens when the view is inside the visible bounds
                                if (DEBUG && METHOD_DEBUG) {
                                    Log.i(TAG, offsetStatus + "offsetChildrenVertical: VIEW IN PLACE, NORMAL SCROLL");
                                }
                                offset = dy;
                                // special case for the last item
                                // when in the case that it is selected,
                                // and normal scroll is used (no extra virtual space)
                                // it should be blocked in its place and not scroll
                                if (selectedPosition == getItemCount() - 1) {
                                    offset = 0;
                                }
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
//                                if (DEBUG && METHOD_DEBUG)
//                                    Log.w(TAG, "offsetChildrenVertical: " + dy + "/" + (virtualPosition - virtualTop) + "/" + originalPosition + "/" + v.getY());
                                // dy is already applied to virtualTop
                                if (DEBUG && METHOD_DEBUG) {
                                    Log.i(TAG, offsetStatus + "offsetChildrenVertical: ADJUST DY");
                                }
                                offset = originalPosition - (virtualPosition - virtualTop);
                            }
                        }
                        if (v.getY() + dy <= 0) {
                            if (DEBUG && METHOD_DEBUG) {
                                Log.i(TAG, offsetStatus + "offsetChildrenVertical: BLOCK VIEW AT TOP");
                            }
                            // if scrolling towards bottom, dy negative, adding items at top
                            // this keeps the view from going offscreen.
//                            if (DEBUG && METHOD_DEBUG)
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
                            if (DEBUG && METHOD_DEBUG) {
                                Log.i(TAG, offsetStatus + "offsetChildrenVertical: VIEW IN PLACE, NORMAL SCROLL");
                            }
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
                            if (DEBUG && METHOD_DEBUG) {
                                Log.i(TAG, offsetStatus + "offsetChildrenVertical: ADJUST DY");
                            }
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
                            if (DEBUG && METHOD_DEBUG) {
                                Log.i(TAG, offsetStatus + "offsetChildrenVertical: BLOCK VIEW AT TOP");
                            }
                            offset = 0;
                        }
                        // this blocks the view at the bottom
                        if (v.getY() + decoratedChildHeight + dy > getHeight()) {
                            if (DEBUG && METHOD_DEBUG) {
                                Log.i(TAG, offsetStatus + "offsetChildrenVertical: BLOCK VIEW AT BOTTOM");
                            }
                            int tooMuch = (int) (v.getY() + decoratedChildHeight + dy - getHeight());
//                            if (DEBUG && METHOD_DEBUG)
//                                Log.d(TAG, "offsetChildrenVertical: tooMuch: " + tooMuch);
                            offset = dy - tooMuch;
                        }
                    }
                }
                if (DEBUG && METHOD_DEBUG) {
                    Log.e(TAG, "offsetChildrenVertical: offset: " + offset + " (" + dy + ")");
                }
                virtualPosition += offset;
                v.offsetTopAndBottom(offset);
            }
            if (DEBUG && METHOD_DEBUG) {
                Log.d(TAG, "offsetChildrenVertical: " +
                        "[ y: " + v.getY() + "-" + (v.getY() + decoratedChildHeight) +
                        "][" + i +
                        "][" + v.getWidth() +
                        "][" + TextUtils.getItemText(v) +
                        "]" + (v.equals(selectedView) ? " [selected]" : ""));
            }
        }
        if (DEBUG && METHOD_DEBUG) {
            Log.v(TAG, "offsetChildrenVertical: dy: " + dy + "  scroll to end " + scrollToEnd);
        }
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        // reset state on adapter change.
        final boolean METHOD_DEBUG = false;

        resetState(true);
        if (DEBUG && METHOD_DEBUG) {
            Log.e(TAG, "onAdapterChanged: RESET STATE FROM ADAPTER CHANGE!!!");
        }
    }

    public void resetState(boolean resetSelectedView) {
        if (resetSelectedView) {
            selectedView = null;
            selectedPosition = -1;
        }
        virtualTop = 0;
        virtualPosition = 0;
        lastTopY = 0;
    }


    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
/*
        final boolean METHOD_DEBUG = false;

        if (DEBUG && METHOD_DEBUG) {
            Log.i(TAG, "smoothScrollToPosition: called with position " + position);
        }
        Bundle bundle = determineEdges(position);
        position = bundle.getInt("position");

        final boolean firstGoingDown = bundle.getBoolean("firstGoingDown");
        final boolean lastGoingUp = bundle.getBoolean("lastGoingUp");

        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                final int dx = calculateDxToMakeVisible(targetView, getHorizontalSnapPreference());
                int dy = calculateDyToMakeVisible(targetView, getVerticalSnapPreference());

                if (DEBUG && METHOD_DEBUG) {
                    Log.i(TAG, "onTargetFound: " +
                            " dy: " + dy +
                            " decoratedChildHeight: " + decoratedChildHeight +
                            " lastTopY: " + lastTopY +
                            " getHeight(): " + getHeight());
                }

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


        if (DEBUG && METHOD_DEBUG) {
            Log.i(TAG, "smoothScrollToPosition: scrollto: " + position);
        }
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    */
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
        final boolean METHOD_DEBUG = false;
        boolean firstGoingDown = false, lastGoingUp = false;

        if (position == selectedPosition) {
            if (DEBUG && METHOD_DEBUG) {
                Log.i(TAG, "determineEdges: position = selectedPosition = " + position);
            }
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
        if (DEBUG && METHOD_DEBUG) {
            Log.d(TAG, "determineEdges() returned: " + bundle);
        }
        return bundle;
    }

    private byte determineScrollDirection(int position) {

        final boolean METHOD_DEBUG = false;

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
        if (DEBUG && METHOD_DEBUG) {
            Log.i(TAG, "determineScrollDirection: " + firstVisibleAdapterPosition + "|" + lastVisibleAdapterPosition);
        }
        if (position < firstVisibleAdapterPosition) {
            // move list from up to down
            toReturn = -1;
        }
        if (position > lastVisibleAdapterPosition) {
            // move list from down to up
            toReturn = 1;
        }
        // do not care what happens if selected position is between these two
        if (DEBUG && METHOD_DEBUG) {
            Log.i(TAG, "determineScrollDirection: returning " + toReturn);
        }
        return toReturn;
    }

    private boolean tryIncrementPosition(int position) {
        return getItemCount() > position + 1;

    }

    private boolean tryDecrementPosition(int position) {
        return position > 0;
    }

    @Override
    public void onScrollStateChanged(int state) {
        boolean METHOD_DEBUG = false;
        if (SCROLL_STATE_IDLE == state) {
            if (DEBUG && METHOD_DEBUG) {
                Log.i(TAG, "onScrollStateChanged: IDLE");
            }
            scrolling = false;
        }
        if (SCROLL_STATE_DRAGGING == state) {
            if (DEBUG && METHOD_DEBUG) {
                Log.i(TAG, "onScrollStateChanged: DRAG");
            }
            scrolling = true;
        }
        if (SCROLL_STATE_SETTLING == state) {
            if (DEBUG && METHOD_DEBUG) {
                Log.i(TAG, "onScrollStateChanged: SETTLING");
            }
            scrolling = true;
        }
    }

    public int firstVisiblePosition() {
        return firstVisiblePosition;
    }

    public int lastVisiblePosition() {
        return lastVisiblePosition;
    }

    @Override
    public void addMeasureCompleteListener(PreloadSizeConsumer listener) {
        preloadSizeConsumers.add(listener);
    }

    @Override
    public void removeMeasureCompleteListener(PreloadSizeConsumer listener) {
        preloadSizeConsumers.remove(listener);
    }
}
