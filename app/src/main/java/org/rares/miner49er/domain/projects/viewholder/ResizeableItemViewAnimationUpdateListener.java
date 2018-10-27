package org.rares.miner49er.domain.projects.viewholder;

import android.animation.ValueAnimator;
import android.os.Build;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

public class ResizeableItemViewAnimationUpdateListener implements ValueAnimator.AnimatorUpdateListener {

    private WeakReference<View> reference;

    private static final String TAG = ResizeableItemViewAnimationUpdateListener.class.getSimpleName();

    ResizeableItemViewAnimationUpdateListener(View view) {
        reference = new WeakReference<>(view);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        View animatedView = reference.get();


/*        Drawable bg = animatedView.getBackground();
        if (bg instanceof LayerDrawable) {
            bg.mutate();
            int animatedC = (int) animation.getAnimatedValue("strokeColor");
            int animatedBgL = (int) animation.getAnimatedValue("bgColorL");
            int animatedBgR = (int) animation.getAnimatedValue("bgColorR");
            LayerDrawable backgroundLayers = (LayerDrawable) bg;
            GradientDrawable strokeRectangle = (GradientDrawable) backgroundLayers.findDrawableByLayerId(R.id.outside_stroke);
            if (strokeRectangle != null) {
                strokeRectangle.setStroke(animatedView.getResources().getDimensionPixelOffset(R.dimen.projects_list_item_background_stroke_width), animatedC);
            }
            GradientDrawable bgRectangle = (GradientDrawable) backgroundLayers.findDrawableByLayerId(R.id.semitransparent_background);
            if (bgRectangle != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    bgRectangle.setColors(new int[]{animatedBgL, animatedBgR});
                }
            }
        }*/

        int animatedW = (int) animation.getAnimatedValue("parentWidth");
//        if (endWidth == ViewGroup.LayoutParams.MATCH_PARENT && animatedW == parentWidth) {
//            animatedW = ViewGroup.LayoutParams.MATCH_PARENT;
//        }
        float animatedE = (float) animation.getAnimatedValue("elevation");
        Log.v(TAG, "onAnimationUpdate: ____ request layout gw: " + animatedView.getWidth() + " lpw: " + animatedView.getLayoutParams().width + " aw: " + animatedW);
        animatedView.getLayoutParams().width = animatedW;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatedView.setElevation(animatedE);
        } else {
            animatedView.bringToFront();
        }
    }

    public void clear() {
        reference.clear();
        reference = null;
    }
}
