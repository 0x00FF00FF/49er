package org.rares.miner49er.ui.actionmode.transitions;

import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import androidx.annotation.NonNull;

public interface ActionFragmentTransition {

    //
    ViewPropertyAnimator getReplacedViewExitAnimator(@NonNull View replacedView);

    ViewPropertyAnimator getReplacedViewEnterAnimator(@NonNull View replacedView);

//    Animation getNewViewEnterAnimator();  // this is taken care of by the fragment manager. (for now)

    Animation getFragmentViewExitAnimator(@NonNull View fragmentView);

    default void prepareEntryAnimation(@NonNull View replacedView) {
        getReplacedViewExitAnimator(replacedView).start();
//        getNewViewEnterAnimator().start(); // this is taken care of by the fragment manager. (for now)
    }

    default void prepareExitAnimation(@NonNull View fragmentView, @NonNull View replacedView) {
        Animation exitAnimation = getFragmentViewExitAnimator(fragmentView);
//
//        exitAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                // get fragment manager and handle back pressed
//
//                AppCompatActivity context = (AppCompatActivity) fragmentView.getContext(); //
//                if (context != null) {
//                    context.getSupportFragmentManager().popBackStack();
//                }
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });

        getReplacedViewEnterAnimator(replacedView).start();
        exitAnimation.start();
    }

}
