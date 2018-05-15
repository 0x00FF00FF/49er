package org.rares.miner49er._abstract;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Build;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces;

/**
 * @author rares
 * @since 02.03.2018
 */

public abstract class ResizeableItemsUiOps
        implements
        BaseInterfaces.ListItemClickListener,
        BaseInterfaces.ResizeableItems {

    private static final String TAG = ResizeableItemsUiOps.class.getSimpleName();

    @Setter
    protected BaseInterfaces.DomainLink domainLink;

    @Setter
    private int rvCollapsedWidth;

    @Getter
    @Setter
    private RecyclerView rv;

    @Override
    public void resetLastSelectedId() {
        ((AbstractAdapter) getRv().getAdapter()).setLastSelectedPosition(-1);
    }

    public boolean selectItem(int selectedPosition) {
        // check if selected position is valid
        AbstractAdapter _tempAdapter = ((AbstractAdapter) getRv().getAdapter());
        final int prevSelected = _tempAdapter.getLastSelectedPosition();

//        RecyclerView.ViewHolder viewHolder = getRv().findViewHolderForAdapterPosition(prevSelected);
//        if (viewHolder != null) {
//            viewHolder.setIsRecyclable(true);
//        }

        if (prevSelected == selectedPosition) {
            resetLastSelectedId();
//            getRv().requestLayout();//////////////////////////
            return true;
        }
        _tempAdapter.setLastSelectedPosition(selectedPosition);
//        _tempAdapter.notifyItemChanged(prevSelected);
//        _tempAdapter.notifyItemChanged(selectedPosition);
        return false;
    }

/*
    @Override
    public boolean resizeItems(int selectedId) {
        boolean deselecting = false;
        for (int i = 0; i < viewHolderList.size(); i++) {
            ResizeableViewHolder vh = viewHolderList.get(i);
            int currentId = vh.getItemProperties().getItemContainerCustomId();
            boolean forceExpand;
            if (selectedId == lastSelectedId) {                 // expand
                deselecting = true;
                vh.getItemProperties().setSelected(false);
                forceExpand = true;
            } else {                                            // compact
                if (selectedId == currentId) {
                    vh.getItemProperties().setSelected(true);
                } else {
                    vh.getItemProperties().setSelected(false);
                }
                forceExpand = false;
            }
            vh.resizeItemView(forceExpand);
        }
        if (deselecting) {
            resetLastSelectedId();
        } else {
            lastSelectedId = selectedId;
        }
        Log.d(TAG, "resizeItems() returned: " + deselecting);
        return deselecting;
    }
*/

    /**
     * Resize the projects recycler view based on the enlarge param
     *
     * @param enlarge if true, makes the rv as big as its parent.
     *                also determines if the parent rv gets elevated over
     *                the issues rv (if the device supports the operation)
     */
    protected void resizeRv(boolean enlarge) {

        AbstractAdapter _tempAdapter = (AbstractAdapter) getRv().getAdapter();
        int width = enlarge ? ViewGroup.LayoutParams.MATCH_PARENT : rvCollapsedWidth;
        int elevation = enlarge ? 0 : _tempAdapter.getMaxElevation();

        resizeAnimated(getRv(), elevation, width);
    }

    public static void resizeAnimated(final View v, int desiredElevation, int desiredWidth) {

        final int fromWidth = v.getMeasuredWidth();
        float fromElevation = 0;

        final int parentWidth = ((View) v.getParent()).getMeasuredWidth();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fromElevation = v.getElevation();
        }

        if (desiredWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
            desiredWidth = parentWidth;
        }

        PropertyValuesHolder pvhElevation = PropertyValuesHolder.ofFloat("elevation", fromElevation, desiredElevation);
        PropertyValuesHolder pvhWidth = PropertyValuesHolder.ofInt("width", fromWidth, desiredWidth);


        ValueAnimator anim = ValueAnimator.ofPropertyValuesHolder(pvhElevation, pvhWidth);
        v.setTag(BaseInterfaces.TAG_ANIMATOR, anim);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int valW = (Integer) valueAnimator.getAnimatedValue("width");
                if (valW == parentWidth) {
                    valW = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                float valE = (float) valueAnimator.getAnimatedValue("elevation");
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.width = valW;
                v.setLayoutParams(layoutParams);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(valE);
                }
            }
        });
        anim.setInterpolator(new LinearOutSlowInInterpolator());
        anim.setDuration(300);
        anim.start();
    }
}
