package org.rares.miner49er.layoutmanager.postprocessing.rotation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import io.reactivex.processors.PublishProcessor;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewAnimator;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;

public class SelfAnimatedItemRotator extends AbstractItemRotator {

    private static final String TAG = SelfAnimatedItemRotator.class.getSimpleName();

    public SelfAnimatedItemRotator(RecyclerView rv) {
        super(rv);
    }

    @Override
    public void rotateItems(ViewGroup viewGroup, PublishProcessor<Boolean> processor) {
        int defaultAnimationTime = 200;
        RecyclerView rv = (RecyclerView) viewGroup;
        AbstractAdapter _tempAdapter = (AbstractAdapter) rv.getAdapter();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View itemView = viewGroup.getChildAt(i);

            RecyclerView.ViewHolder vh = rv.getChildViewHolder(itemView);
            final int prevPos = _tempAdapter.getPreviouslySelectedPosition();
            final int lastPos = _tempAdapter.getLastSelectedPosition();
            final int vhPos = vh.getAdapterPosition();
            boolean reverse = false;

            // we only work with ItemViewAnimator+ viewHolder
            ItemViewAnimator h;

            if (vh instanceof ItemViewAnimator) {
                h = ((ItemViewAnimator) vh);
            } else {
                continue;
            }

            ValueAnimator valueAnimator = null;

            if (prevPos == -1 && lastPos != -1) {   // from big to small, no prev item selected
                reverse = false;
                valueAnimator = h.animateItem(false, vhPos == lastPos, defaultAnimationTime, processor);
            }
            if (prevPos != -1 && lastPos != -1) {   // changing from one selected item to another
                if (vhPos == prevPos) {
                    reverse = false;
                    valueAnimator = h.animateItem(false, false, defaultAnimationTime, processor);
                }
                if (vhPos == lastPos) {
                    reverse = false;
                    valueAnimator = h.animateItem(false, true, defaultAnimationTime, processor);
                }
            }
            if (prevPos != -1 && lastPos == -1) {   // from small to big

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
                reverse = true;
                valueAnimator = h.animateItem(true, vhPos == prevPos, defaultAnimationTime, processor);
            }

//            if (i == rv.getChildCount() - 1 && valueAnimator != null) {
//                addedPostProcessCall = true;
//                if (!valueAnimator.getListeners().contains(ppt)) {
//                    valueAnimator.addListener(ppt);
//                }
//            }
            if (valueAnimator != null) {
                valueAnimator.setStartDelay(/*(reverse ? 100 : 0) +*/ i * 50);
//                if (vhPos != lastPos) {
                    valueAnimator.start();
//                }
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

    private class PostProcessTrigger extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            if (postProcessorConsumer != null) {
                Log.i(TAG, "onAnimationEnd: adding post process call in holder.");
                postProcessorConsumer.onPostProcessEnd();
            }
            animation.removeListener(this);
        }
    }

    private PostProcessTrigger ppt = new PostProcessTrigger();

}
