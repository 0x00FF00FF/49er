package org.rares.miner49er.layoutmanager.postprocessing.rotation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.util.TextUtils;

/**
 * Customizes the item animation for view holders that do not support this themselves.
 */
@Deprecated
public class AnimatedItemRotator extends AbstractItemRotator {

    private static final String TAG = AnimatedItemRotator.class.getSimpleName();

    public AnimatedItemRotator(RecyclerView recyclerView) {
        super(recyclerView);
    }

    @Override
    public void rotateItems(ViewGroup viewGroup) {
        RecyclerView rv = (RecyclerView) viewGroup;
        AbstractAdapter _tempAdapter = (AbstractAdapter) rv.getAdapter();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View itemView = viewGroup.getChildAt(i);
            RecyclerView.ViewHolder vh = rv.getChildViewHolder(itemView);
            final int prevPos = _tempAdapter.getPreviouslySelectedPosition();
            final int lastPos = _tempAdapter.getLastSelectedPosition();
            final int vhPos = vh.getAdapterPosition();

            if (vhPos == -1) {
                Log.w(TAG, "rotateItems: processed holder that was no longer in the adapter. returning.");
                return;
            }

            if (vh instanceof ResizeableItemViewHolder) {
                ResizeableItemViewHolder holder = (ResizeableItemViewHolder) vh;
                boolean shortVersion = lastPos != -1;
//                int position = holder.getAdapterPosition();
//                boolean selected = position == lastPos;
//                holder.bindData(_tempAdapter.getDisplayData(vhPos), shortVersion, selected);
                holder.toggleItemText(shortVersion);
            }

            if (prevPos == -1 && lastPos != -1) {   // from big to small, no prev item selected
                if (vhPos != lastPos) {
                    rotateItem(vh, false);
                }
            }
            if (prevPos != -1 && lastPos != -1) {   // changing from one selected item to another
                if (vhPos == prevPos) {
                    rotateItem(vh, false);
                }
                if (vhPos == lastPos) {
                    rotateItem(vh, true);
                }
            }
            if (prevPos != -1 && lastPos == -1) {   // from small to big
                if (vhPos != prevPos) {
                    rotateItem(vh, true);
                }
            }
        }
        if (postProcessorConsumer != null) {
            postProcessorConsumer.onPostProcessEnd();
        }
    }

    @Override
    public void rotateItem(RecyclerView.ViewHolder holder, boolean clockwise) {

        View childView = ((ViewGroup) holder.itemView).getChildAt(0);

//        TextView animatedView = (TextView) childView;

        int fromRotation = clockwise ? -90 : 0;
        int toRotation = clockwise ? 0 : -90;

//        animatedView.setPivotX(animatedView.getMeasuredHeight() / 2);
//        animatedView.setPivotY(animatedView.getMeasuredHeight() / 2);

        PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofInt("rotation", fromRotation, toRotation);
        ValueAnimator anim = null;
        if (holder instanceof ResizeableItemViewHolder) {
            anim = ((ResizeableItemViewHolder) holder).getAnimator();
        }
        if (anim == null) {
            anim = ValueAnimator.ofPropertyValuesHolder(pvhRotation);
        }

        AnimationUpdateListener listener = new AnimationUpdateListener();
        listener.animatedView = childView;

        AnimationEventListener evListener = new AnimationEventListener();
        evListener.holder = holder;
        evListener.animatedView = childView;
        evListener.clockwise = clockwise;

        anim.removeAllListeners();
        anim.addListener(evListener);
        anim.addUpdateListener(listener);
        anim.setDuration(200);
        anim.start();

    }

    @Override
    public void setPostProcessConsumer(PostProcessorConsumer postProcessConsumer) {
        this.postProcessorConsumer = postProcessConsumer;
    }

    private class AnimationUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        View animatedView;

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int rotation = (int) animation.getAnimatedValue("rotation");
            animatedView.setRotation(rotation);
        }
    }

    private class AnimationEventListener extends AnimatorListenerAdapter {
        RecyclerView.ViewHolder holder;

        View animatedView;
        boolean clockwise = true;

        @Override
        public void onAnimationStart(Animator animation) {
            if (animatedView instanceof TextView/* && clockwise*/) {
                TextUtils.setCenterStartGravity((TextView) animatedView);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (animatedView instanceof TextView) {
                if (!clockwise) {
                    TextUtils.setCenterGravity((TextView) animatedView);
                } else {
                    TextUtils.setCenterStartGravity((TextView) animatedView);
                }
            }
        }
    }
}
