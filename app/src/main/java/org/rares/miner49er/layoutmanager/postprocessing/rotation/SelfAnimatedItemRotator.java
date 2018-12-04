package org.rares.miner49er.layoutmanager.postprocessing.rotation;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewAnimator;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;

public class SelfAnimatedItemRotator extends AbstractItemRotator {

    private static final String TAG = SelfAnimatedItemRotator.class.getSimpleName();

    public SelfAnimatedItemRotator(RecyclerView rv) {
        super(rv);
    }


    @Override
    public void rotateItems(ViewGroup viewGroup) {
        int defaultAnimationTime = 200;

        final int SELECTED = 1;
        final int SELECTED_AFTER_SELECTED = 1 << 1;
        final int DESELECTED_AFTER_SELECTED = 1 << 2;
        final int EXPANDED = 1 << 3;
        final int COLLAPSED = 1 << 4;

        RecyclerView rv = (RecyclerView) viewGroup;
        AbstractAdapter _tempAdapter = (AbstractAdapter) rv.getAdapter();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View itemView = viewGroup.getChildAt(i);

            RecyclerView.ViewHolder vh = rv.getChildViewHolder(itemView);
            final int prevPos = _tempAdapter.getPreviouslySelectedPosition();
            final int lastPos = _tempAdapter.getLastSelectedPosition();
            final int vhPos = vh.getAdapterPosition();

            // we only work with ItemViewAnimator+ viewHolder
            ItemViewAnimator h;
            ValueAnimator itemAnimator = null;

            if (vh instanceof ItemViewAnimator) {
                h = ((ItemViewAnimator) vh);
            } else {
                continue;
            }

            int state = 0;

            if (prevPos == -1 && lastPos != -1) {   // from big to small, no prev item selected
                itemAnimator = h.animateItem(false, vhPos == lastPos, defaultAnimationTime);
                state = COLLAPSED | (vhPos == lastPos ? SELECTED : 0);
            }
            if (prevPos != -1 && lastPos != -1) {   // changing from one selected item to another
                state = COLLAPSED;
                if (vhPos == prevPos) {
                    itemAnimator = h.animateItem(false, false, defaultAnimationTime);
                    state |= DESELECTED_AFTER_SELECTED;
                }
                if (vhPos == lastPos) {
                    itemAnimator = h.animateItem(false, true, defaultAnimationTime);
                    state |= SELECTED_AFTER_SELECTED;
                    state |= SELECTED;
                }
            }
            if (prevPos != -1 && lastPos == -1) {   // from small to big
                state = EXPANDED;
                /*
                 * A little inoffensive hack :)
                 * Because the StickyLayoutManager keeps
                 * the selected view and updates it artificially,
                 * we need to re-bind the view holder when animating,
                 * because the layout manager can only update the view
                 * and not all the items in the view holder.
                 * */
                if (h instanceof ResizeableItemViewHolder && vhPos == prevPos) {
                    ((ResizeableItemViewHolder) h)
                            .bindData(_tempAdapter.getDisplayData(vhPos), false, false);
                }
                itemAnimator = h.animateItem(true, vhPos == prevPos, defaultAnimationTime);
                state |= vhPos == prevPos ? DESELECTED_AFTER_SELECTED : 0;
            }
            if (itemAnimator != null) {
                if (!((state & DESELECTED_AFTER_SELECTED) == DESELECTED_AFTER_SELECTED ||
                        (state & SELECTED_AFTER_SELECTED) == SELECTED_AFTER_SELECTED) ||
                        (state & EXPANDED) == EXPANDED) {
                    itemAnimator.setStartDelay(100 + (i + 1) * 100);
                }
                itemAnimator.start();
            }
        }
        if (postProcessorConsumer != null) {
            postProcessorConsumer.onPostProcessEnd();
        }
    }

    @Override
    public void rotateItem(RecyclerView.ViewHolder holder, boolean clockwise) {
        // nothing to do here, item is rotated by the holder
    }

    @Override
    public void validateViewRotation(View itemView, boolean closedState, boolean isViewSelected) {
        RecyclerView.ViewHolder holder = rv.getChildViewHolder(itemView);
        if (holder == null) {
            Log.w(TAG, "validateViewRotation: RETURNING, HOLDER NOT IN RV");
            return;
        }
        ItemViewAnimator rvh = null;
        if (holder instanceof ItemViewAnimator) {
            rvh = (ItemViewAnimator) holder;
        }
        if (rvh == null) {
            Log.w(TAG, "validateViewRotation: RETURNING, HOLDER NOT RVH");
            return;
        }
        rvh.validateItem(closedState, isViewSelected);
    }

    @Override
    public void setPostProcessConsumer(PostProcessorConsumer postProcessConsumer) {
        this.postProcessorConsumer = postProcessConsumer;
    }

}
