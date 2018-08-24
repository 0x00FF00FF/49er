package org.rares.miner49er.layoutmanager.postprocessing.rotation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.util.TextUtils;

public class AnimatedItemRotator extends AbstractItemRotator {

    private static final String TAG = AnimatedItemRotator.class.getSimpleName();

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
            TextView animatedView = (TextView) ((ViewGroup) itemView).getChildAt(0);

            String text = _tempAdapter.resolveData(vh.getAdapterPosition());
            if (text != null) {
                animatedView.setText(text);
            }

            if (prevPos == -1 && lastPos != -1) {   // from big to small, no prev item selected
                if (vhPos != lastPos) {
                    rotateItem(itemView, false);
                }
            }
            if (prevPos != -1 && lastPos != -1) {   // changing from one selected item to another
                if (vhPos == prevPos) {
                    rotateItem(itemView, false);
                }
                if (vhPos == lastPos) {
                    rotateItem(itemView, true);
                }
            }
            if (prevPos != -1 && lastPos == -1) {   // from small to big
                if (vhPos != prevPos) {
                    rotateItem(itemView, true);
                }
            }
        }
        if (postProcessorConsumer != null) {
            postProcessorConsumer.onPostProcessEnd();
        }
    }

    @Override
    public void rotateItem(View itemView, boolean clockwise) {
        int fromRotation, toRotation;

        View animatedView = ((ViewGroup) itemView).getChildAt(0);

        fromRotation = clockwise ? -90 : 0;
        toRotation = clockwise ? 0 : -90;

        animatedView.setPivotX(animatedView.getMeasuredHeight() / 2);
        animatedView.setPivotY(animatedView.getMeasuredHeight() / 2);

        PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofInt("rotation", fromRotation, toRotation);
        ValueAnimator anim = ValueAnimator.ofPropertyValuesHolder(pvhRotation);

        AnimationUpdateListener listener = new AnimationUpdateListener();
        listener.animatedView = animatedView;

        AnimationEventListener evListener = new AnimationEventListener();
        evListener.animatedView = animatedView;
        evListener.clockwise = clockwise;
        anim.addListener(evListener);
        anim.addUpdateListener(listener);
        anim.setDuration(200);
        anim.start();

    }

    @Override
    public void validateViewRotation(View itemView, boolean closedState, boolean isViewSelected) {
        super.validateViewRotation(itemView, closedState, isViewSelected);
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

        View animatedView;
        boolean clockwise = true;

        @Override
        public void onAnimationStart(Animator animation) {
            if (clockwise) {
                TextUtils.setCenterStartGravity((TextView) animatedView);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!clockwise) {
                TextUtils.setCenterGravity((TextView) animatedView);
            } else {
                TextUtils.setCenterStartGravity((TextView) animatedView);
            }
        }
    }
}
