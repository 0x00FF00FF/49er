package org.rares.miner49er.ui.actionmode.transitions;

import android.content.Context;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.constraintlayout.widget.ConstraintLayout;
import org.rares.miner49er.R;

public class TranslationTransition implements ActionFragmentTransition {

    @Override
    public ViewPropertyAnimator getReplacedViewExitAnimator(View replacedView) {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) replacedView.getLayoutParams(); //
        return replacedView.animate().translationXBy(replacedView.getWidth() + lp.leftMargin + lp.rightMargin);
    }

    @Override
    public ViewPropertyAnimator getReplacedViewEnterAnimator(View replacedView) {
        return replacedView.animate().translationX(0);
    }

    @Override
    public Animation getFragmentViewExitAnimator(View fragmentView) {
        Context ctx = fragmentView.getContext();
        return AnimationUtils.loadAnimation(ctx, R.anim.item_animation_to_left);
    }
}
