package org.rares.miner49er.domain.projects.viewholder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.util.Log;
import android.view.View;
import io.reactivex.processors.PublishProcessor;

public class ResizeableItemViewAnimationListener extends AnimatorListenerAdapter {
    private final View animatedView;
    private final boolean selected;
    private PublishProcessor<Boolean> publishProcessor;

    public static final String TAG = ResizeableItemViewAnimationListener.class.getSimpleName();

    ResizeableItemViewAnimationListener(View animatedView, boolean selected, PublishProcessor<Boolean> processor) {
        this.animatedView = animatedView;
        this.selected = selected;
        this.publishProcessor = processor;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (animatedView == null) {
            return;
        }
        if (animatedView.getLayoutParams().width != -1) {
            if (!selected) {
                animatedView.getLayoutParams().width = -1;
            }
        }
        if (publishProcessor != null) {
            publishProcessor.onNext(false);
        }
        animation.removeListener(this);
        Log.i(TAG, "onAnimationEnd: > selected: " + selected + " " + animatedView.getLayoutParams().width);
    }
}
