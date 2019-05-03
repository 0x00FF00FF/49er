package org.rares.miner49er._abstract;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Unbinder;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.BaseInterfaces.ColorAnimation;
import org.rares.miner49er.BaseInterfaces.SelectableItemsManager;
import org.rares.miner49er.BaseInterfaces.SetValues;
import org.rares.miner49er.R;
import org.rares.miner49er.layoutmanager.ItemAnimationDto;
import org.rares.miner49er.layoutmanager.ResizeableLayoutManager;
import org.rares.miner49er.layoutmanager.postprocessing.ResizePostProcessor;
import org.rares.miner49er.util.ArgbEvaluator;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rares
 * @since 02.03.2018
 */

public abstract class ResizeableItemsUiOps
        implements
        BaseInterfaces.ListItemEventListener,
        BaseInterfaces.SelectableItemsManager,
        ResizePostProcessor.PostProcessorConsumer,
        BaseInterfaces.UnbinderHost {

    private static String TAG = ResizeableItemsUiOps.class.getSimpleName();

    private List<BaseInterfaces.RvResizeListener> resizeListeners = new ArrayList<>();

    @Setter
    protected BaseInterfaces.DomainLink domainLink;

    @Setter
    private int rvCollapsedWidth;

    protected List<Unbinder> unbinderList = new ArrayList<>();

    @Getter
    @Setter
    private RecyclerView rv;

    @Getter
    private ListState rvState = ListState.LARGE;

    private ResizePostProcessor.PostProcessor resizePostProcessor;

    protected Repository repository;

    protected FragmentManager fragmentManager;

    /**
     * Determines if item selection background animation should happen: <br />
     * {@link SetValues#NOT_SET} - use defaults <br />
     * {@link SetValues#ENABLED} - enable background animation <br />
     * {@link SetValues#DISABLED} - do not use background animation <br />
     */
    protected byte enableBackground = SetValues.NOT_SET;

    @DrawableRes
    protected Integer selectedDrawableRes = null;
    @DrawableRes
    protected Integer collapsedDrawableRes = null;
    @DrawableRes
    protected Integer expandedDrawableRes = null;

    protected int colorMarginNormalItem = ColorAnimation.NOT_SET;
    protected int colorMarginSelectedItem = ColorAnimation.NOT_SET;
    protected int colorBgLeft = ColorAnimation.NOT_SET;
    protected int colorBgRight = ColorAnimation.NOT_SET;
    protected int colorBgLeftSelected = ColorAnimation.NOT_SET;
    protected int colorBgRightSelected = ColorAnimation.NOT_SET;
    protected int colorBgSolid = ColorAnimation.DO_NOT_TOUCH;


    @Override
    public boolean onListItemClick(ResizeableItemViewHolder holder) {

        setupSelectedItemAnimation(holder);

        int adapterPosition = holder.getAdapterPosition();  /// if holder ==null ???
        boolean enlarge = selectItem(adapterPosition);

        domainLink.onParentSelected(holder.getItemProperties(), enlarge);

        RecyclerView.LayoutManager layoutManager = getRv().getLayoutManager();

        if (layoutManager instanceof ResizeableLayoutManager) {
            ResizeableLayoutManager mgr = (ResizeableLayoutManager) layoutManager;
            {
                mgr.setSelectedPosition(enlarge ? -1 : adapterPosition);
                List<ItemAnimationDto> animatedItemsList = mgr.resizeSelectedView(holder.itemView, enlarge);
                for (ItemAnimationDto ia : animatedItemsList) {
                    resizeItemAnimated(ia, ia.getAnimatedView() == holder.itemView, !enlarge);
                    // we do this here to ensure the visual effect of selecting the item
                }
            }
        }

        resizeRv(enlarge);

        return enlarge;
    }

    @Override
    public void onListItemChanged(ItemViewProperties ivp) {
        domainLink.onParentChanged(ivp);
    }

    public boolean selectItem(int selectedPosition) {
        // check if selected position is valid
        AbstractAdapter _tempAdapter = ((AbstractAdapter) getRv().getAdapter());
        final int prevSelected = _tempAdapter.getLastSelectedPosition();
        _tempAdapter.setPreviouslySelectedPosition(prevSelected);

        boolean enlarge = prevSelected == selectedPosition;

        rvState = enlarge ? ListState.LARGE : ListState.SMALL;

        dispatchResizeEvents(enlarge);

//        Log.d(TAG, "selectItem: rvState " + (rvState == ListState.LARGE ? "LARGE" : "SMALL"));
        if (enlarge) {
            resetLastSelectedId();
            return true;
        }

        _tempAdapter.setLastSelectedPosition(selectedPosition);
        return false;
    }

    @Override
    public void resetLastSelectedId() {
        AbstractAdapter _tempAdapter = (AbstractAdapter) getRv().getAdapter();
        _tempAdapter.setLastSelectedPosition(-1);
    }

    @Override
    public int getSelectedItemId() {
        AbstractAdapter adapter = (AbstractAdapter) getRv().getAdapter();
        return adapter == null ? -1 : adapter.getLastSelectedPosition();
    }

    @Override
    public ResizeableItemViewHolder getSelectedViewHolder() {
        return (ResizeableItemViewHolder) getRv().findViewHolderForAdapterPosition(getSelectedItemId());
    }

    public void expandList() {
        if (rvState == ListState.SMALL) {
            onListItemClick(getSelectedViewHolder());
        }
    }

    private void dispatchResizeEvents(boolean enlarge) {
        for (BaseInterfaces.RvResizeListener resizeListener : resizeListeners) {
            if (enlarge) {
                resizeListener.onRvGrow();
            } else {
                resizeListener.onRvShrink();
            }
        }
    }

    /**
     * Resize the projects recycler view based on the enlarge param
     *
     * @param enlarge if true, makes the rv as big as its parent.
     *                also determines if the parent rv gets elevated over
     *                the child domain rv (if the device supports the operation)
     */
    protected void resizeRv(boolean enlarge) {
        boolean animationEnabled = true;
        AbstractAdapter _tempAdapter = (AbstractAdapter) getRv().getAdapter();
        int width = enlarge ? ViewGroup.LayoutParams.MATCH_PARENT : rvCollapsedWidth;
        int elevation = enlarge ? 0 : (_tempAdapter == null ? 0 : _tempAdapter.getMaxElevation());

//        if (rv.getLayoutParams().width != width) {
//        reminder -> this^ if block introduces a bug:
//              repro:
//              select an item,
//              wait for the rv to be collapsed
//              tap 3 times on another item in the list
//              the new item is small (selected) and all of the others are big.
//              >(i.e. the rv did not collapse when it should have)
        if (animationEnabled) {
            resizeRvAnimated(elevation, width);
        } else {
            getRv().getLayoutParams().width = width;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getRv().setElevation(elevation);
            }
            getRv().requestLayout();
        }
//        }
        // post process views
        if (resizePostProcessor != null) {
            resizePostProcessor.postProcess(getRv());
        }
    }

    private void prepareWidthAnimation
            (@NonNull ItemAnimationDto animationData, @NonNull ArrayList<PropertyValuesHolder> valuesHolderList) {

        final View v = animationData.getAnimatedView();
        final float endElevation = animationData.getElevation();
        final int endWidth = animationData.getWidth();

        int startWidth = v.getWidth();
        float startElevation = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startElevation = v.getElevation();
        }

        valuesHolderList.add(PropertyValuesHolder.ofInt(SelectableItemsManager.ANIMATION_WIDTH, startWidth, endWidth));
        valuesHolderList.add(PropertyValuesHolder.ofFloat(SelectableItemsManager.ANIMATION_ELEVATION, startElevation, endElevation));
    }

    private void prepareBackgroundAnimation
            (ResizeableItemViewHolder holder,
             boolean selected,
             boolean collapsed,
             @NonNull ArrayList<PropertyValuesHolder> valuesHolderList) {

//        Log.i(TAG, "prepareBackgroundAnimation: -------------------------" +
//                "\ncolorMarginNormalItem 0x" + Integer.toHexString(colorMarginNormalItem) +
//                "\ncolorMarginSelectedItem 0x" + Integer.toHexString(colorMarginSelectedItem) +
//                "\ncolorBgLeft 0x" + Integer.toHexString(colorBgLeft) +
//                "\ncolorBgRight 0x" + Integer.toHexString(colorBgRight) +
//                "\ncolorBgLeftSelected 0x" + Integer.toHexString(colorBgLeftSelected) +
//                "\ncolorBgRightSelected 0x" + Integer.toHexString(colorBgRightSelected) +
//                "\ncolorBgSolid 0x" + Integer.toHexString(colorBgSolid)
//        );

        int startMarginColor;
        int endMarginColor;
        int startLeftBgColor;
        int endLeftBgColor;
        int startRightBgColor;
        int endRightBgColor;

        ArgbEvaluator argbEvaluator = ArgbEvaluator.getInstance();
        if (collapsed) {
            if (selected) { // the selected item going to collapsed state
//                Log.d(TAG, "prepareBackgroundAnimation: selected collapsed");
                startMarginColor = colorMarginNormalItem;
                endMarginColor = colorMarginSelectedItem;
                startLeftBgColor = colorBgLeft;
                endLeftBgColor = colorBgLeftSelected;
                startRightBgColor = colorBgRight;
                endRightBgColor = colorBgRightSelected;
            } else { // previously selected item, still collapsed
//                Log.d(TAG, "prepareBackgroundAnimation: collapsed !selected");
                startMarginColor = colorMarginSelectedItem;
                endMarginColor = colorMarginNormalItem;
                startLeftBgColor = colorBgLeftSelected;
                endLeftBgColor = colorBgLeft;
                startRightBgColor = colorBgRightSelected;
                endRightBgColor = colorBgRight;
            }
        } else {
            if (selected) { // selected item goes to enlarged form, no longer selected
//                Log.d(TAG, "prepareBackgroundAnimation: selected !collapsed");
                startMarginColor = colorMarginSelectedItem;
                endMarginColor = colorMarginNormalItem;
                startLeftBgColor = colorBgLeftSelected;
                endLeftBgColor = colorBgLeft;
                startRightBgColor = colorBgRightSelected;
                endRightBgColor = colorBgRight;
            } else {
                // should not even enter here...
//                Log.w(TAG, "prepareBackgroundAnimation: I WAS WRONG. (!selected !collapsed)");
                startMarginColor = Color.TRANSPARENT;
                endMarginColor = Color.TRANSPARENT;
                startLeftBgColor = Color.TRANSPARENT;
                endLeftBgColor = Color.TRANSPARENT;
                startRightBgColor = Color.TRANSPARENT;
                endRightBgColor = Color.TRANSPARENT;
            }
        }

        if (!(colorBgSolid == ColorAnimation.DO_NOT_TOUCH || colorBgSolid == ColorAnimation.NOT_SET)) {
            int solidColor = colorBgSolid;
            if (solidColor == ColorAnimation.ITEM_DATA) {
                solidColor = holder.getItemProperties().getItemBgColor();
                if (solidColor == 0) {
                    solidColor = Color.parseColor("#AA7986CB");
                    solidColor = UiUtil.getBrighterColor(solidColor, 0.1F);
                }
            }
            int transparentColor = UiUtil.getTransparentColor(solidColor, 0);
            int startSolidColor;
            int endSolidColor;

            if (collapsed) {
                startSolidColor = selected ? transparentColor : solidColor;
                endSolidColor = selected ? solidColor : transparentColor;
            } else {
                startSolidColor = selected ? solidColor : transparentColor;
                endSolidColor = selected ? transparentColor : solidColor;
            }

            valuesHolderList.add(
                    PropertyValuesHolder.ofObject(
                            BaseInterfaces.SelectableItemsManager.ANIMATION_SOLID_COLOR, argbEvaluator, startSolidColor, endSolidColor));
//            Log.i(TAG, "prepareBackgroundAnimation: ANIMATION_SOLID_COLOR: " + Integer.toHexString(startSolidColor) + " " + Integer.toHexString(endSolidColor));
        }

        valuesHolderList.add(PropertyValuesHolder.ofObject(
                SelectableItemsManager.ANIMATION_STROKE_COLOR, argbEvaluator, startMarginColor, endMarginColor));

//        Log.i(TAG, "prepareBackgroundAnimation: ANIMATION_STROKE_COLOR: " + Integer.toHexString(startMarginColor) + " " + Integer.toHexString(endMarginColor));

        valuesHolderList.add(PropertyValuesHolder.ofObject(
                SelectableItemsManager.ANIMATION_OVERLAY_LEFT_COLOR, argbEvaluator, startLeftBgColor, endLeftBgColor));
//        Log.i(TAG, "prepareBackgroundAnimation: ANIMATION_OVERLAY_LEFT_COLOR: " + Integer.toHexString(startLeftBgColor) + " " + Integer.toHexString(endLeftBgColor));
        valuesHolderList.add(PropertyValuesHolder.ofObject(
                SelectableItemsManager.ANIMATION_OVERLAY_RIGHT_COLOR, argbEvaluator, startRightBgColor, endRightBgColor));
//        Log.i(TAG, "prepareBackgroundAnimation: ANIMATION_OVERLAY_RIGHT_COLOR: " + Integer.toHexString(startRightBgColor) + " " + Integer.toHexString(endRightBgColor));
    }

    private class BackgroundManipulationAnimationUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        View animatedView;
        Resources res;

        public BackgroundManipulationAnimationUpdateListener(View animatedView) {
            this.animatedView = animatedView;
            res = animatedView.getResources();
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {

            final Drawable bg;

            if (animatedView.getBackground() == null) {
                if (selectedDrawableRes == null) {
                    bg = res.getDrawable(R.drawable.transient_semitransparent_rectangle_tl_br);
                } else {
                    bg = res.getDrawable(selectedDrawableRes);
                }
                animatedView.setBackgroundDrawable(bg);
            } else {
                bg = animatedView.getBackground();
            }

            if (bg instanceof LayerDrawable) {
                bg.mutate();
                Object animatedValue = animation.getAnimatedValue(SelectableItemsManager.ANIMATION_STROKE_COLOR);
                int animatedC = animatedValue == null ? ColorAnimation.NOT_SET : (int) animatedValue;
                animatedValue = animation.getAnimatedValue(BaseInterfaces.SelectableItemsManager.ANIMATION_OVERLAY_LEFT_COLOR);
                int animatedBgL = animatedValue == null ? ColorAnimation.NOT_SET : (int) animatedValue;
                animatedValue = animation.getAnimatedValue(SelectableItemsManager.ANIMATION_OVERLAY_RIGHT_COLOR);
                int animatedBgR = animatedValue == null ? ColorAnimation.NOT_SET : (int) animatedValue;
                animatedValue = animation.getAnimatedValue(SelectableItemsManager.ANIMATION_SOLID_COLOR);
                int animatedBgSolid = animatedValue == null ? ColorAnimation.NOT_SET : (int) animatedValue;

                LayerDrawable backgroundLayers = (LayerDrawable) bg;
                GradientDrawable strokeRectangle =
                        (GradientDrawable) backgroundLayers.findDrawableByLayerId(R.id.outside_stroke);
                if (strokeRectangle != null && animatedC != ColorAnimation.NOT_SET) {
                    strokeRectangle.setStroke(res.getDimensionPixelOffset(R.dimen.projects_list_item_background_stroke_width), animatedC);
                }

                GradientDrawable bgRectangle =
                        (GradientDrawable) backgroundLayers.findDrawableByLayerId(R.id.semitransparent_background);
                if (bgRectangle != null && animatedBgR != ColorAnimation.NOT_SET && animatedBgL != ColorAnimation.NOT_SET) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        bgRectangle.setColors(new int[]{animatedBgL, animatedBgR});
                    }
                }

                GradientDrawable opaqueBackground =
                        (GradientDrawable) backgroundLayers.findDrawableByLayerId(R.id.opaque_background);
                if (opaqueBackground != null && animatedBgSolid != ColorAnimation.NOT_SET) {
                    opaqueBackground.setColor(animatedBgSolid);
                }
                animatedView.requestLayout();
            }
        }
    }

    private class WidthAnimationUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        int endWidth/*, endAnimationWidth*/;
        View animatedView;

        public WidthAnimationUpdateListener(int endWidth, View animatedView) {
            this.endWidth = endWidth;
//            this.endAnimationWidth = endAnimationWidth;
            this.animatedView = animatedView;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int animatedW = (int) animation.getAnimatedValue(SelectableItemsManager.ANIMATION_WIDTH);
//            if (endWidth == ViewGroup.LayoutParams.MATCH_PARENT && animatedW == endAnimationWidth) {
            if (endWidth == ViewGroup.LayoutParams.MATCH_PARENT && animation.getAnimatedFraction() == 1) {
                animatedW = ViewGroup.LayoutParams.MATCH_PARENT;
            }

            animatedView.getLayoutParams().width = animatedW;

            float animatedE = (float) animation.getAnimatedValue(SelectableItemsManager.ANIMATION_ELEVATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animatedView.setElevation(animatedE);
            } /*else {
                    // FIXME
                }*/
            animatedView.requestLayout();
        }
    }

    private void resizeRvAnimated(final int elevation, final int width) {

        int endAnimationWidth = getEndAnimationWidth(width, rv);

        ArrayList<PropertyValuesHolder> pvhList = new ArrayList<>();
        prepareWidthAnimation(
                new ItemAnimationDto(rv, elevation, endAnimationWidth), pvhList);

        ValueAnimator anim = ValueAnimator.ofPropertyValuesHolder(
                pvhList.toArray(new PropertyValuesHolder[pvhList.size()]));

        anim.addUpdateListener(new WidthAnimationUpdateListener(width, rv));

        anim.setDuration(BaseInterfaces.ANIMATION_DURATION);
        anim.start();
    }

    /**
     * Computes the value of the final width, based on preference (MATCH_PARENT or desired width).
     *
     * @param width desired width. can be -1 for match_parent or an actual value.
     *              if match_parent is selected, it will compute the parent view's
     *              width and subtract the left and right margins
     * @param v     the target view
     * @return the final width for the animation (in pixels), based on the desired width. will not be -1
     */
    private int getEndAnimationWidth(int width, View v) {
        final int parentWidth = getParentWidth(v);

        // when horizontal margins are added, we need to adapt the width
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        return width == ViewGroup.LayoutParams.MATCH_PARENT ?
                parentWidth - mlp.leftMargin - mlp.rightMargin :
                width;
    }

    /**
     * Resize a recycler view item (the selected or previously selected item)
     *
     * @param animationDto - dto containing animation data (end width, end elevation)
     * @param selected     - if true, this item is selected
     * @param collapsed    - if true, the recycler view is in a collapsed state
     */
    private void resizeItemAnimated(ItemAnimationDto animationDto, boolean selected, boolean collapsed) {
        final View animatedView = animationDto.getAnimatedView();
        final int endAnimationWidth = getEndAnimationWidth(animationDto.getWidth(), animationDto.getAnimatedView());
        final int width = animationDto.getWidth();

        ArrayList<PropertyValuesHolder> pvhList = new ArrayList<>();

        prepareWidthAnimation(
                new ItemAnimationDto(animatedView, animationDto.getElevation(), endAnimationWidth), pvhList);
        prepareBackgroundAnimation(
                (ResizeableItemViewHolder) getRv().getChildViewHolder(animatedView),
                selected,
                collapsed,
                pvhList);

        ValueAnimator anim = ValueAnimator.ofPropertyValuesHolder(
                pvhList.toArray(new PropertyValuesHolder[pvhList.size()]));

        anim.addUpdateListener(new WidthAnimationUpdateListener(width, animatedView));
        anim.addUpdateListener(new BackgroundManipulationAnimationUpdateListener(animatedView));
        if (enableBackground != SetValues.ENABLED) {
            final AnimatorListenerAdapter ad = getWidthEndAnimationListener(animatedView);
            anim.addListener(ad);
        }
        anim.setDuration(BaseInterfaces.ANIMATION_DURATION);
        anim.start();
    }

    /**
     * Configures colors for background manipulation on item click.
     * Override this for custom colors.
     *
     * @param holder - the viewHolder for the selected item.
     */
    protected void setupSelectedItemAnimation(ResizeableItemViewHolder holder) {
        Resources res = getRv().getResources();

        if (enableBackground == SetValues.NOT_SET) {
            colorMarginNormalItem = res.getColor(R.color.transient_semitransparent_background_margin);
            colorMarginSelectedItem = res.getColor(R.color.pureWhite);
            colorBgLeftSelected = res.getColor(R.color.semitransparent_black_left_selected);
            colorBgRightSelected = res.getColor(R.color.semitransparent_black_right_selected);
            enableBackground = holder.itemView.getBackground() == null ? SetValues.DISABLED : SetValues.ENABLED;
        }
        if (enableBackground == SetValues.ENABLED && colorBgLeft == ColorAnimation.NOT_SET) {
            colorBgLeft = res.getColor(R.color.semitransparent_black_left);
            colorBgRight = res.getColor(R.color.semitransparent_black_right);
        }
        if (enableBackground == SetValues.DISABLED) {
            if (colorBgSolid == ColorAnimation.DO_NOT_TOUCH) {
                colorBgSolid = ColorAnimation.ITEM_DATA;
            }
            if (colorBgLeft == ColorAnimation.NOT_SET) {
                colorBgLeft = Color.TRANSPARENT;
                colorBgRight = Color.TRANSPARENT;
            }
        }
    }

    private int getParentWidth(View v) {
//        Log.e(TAG, "getParentWidth: rvState " + (rvState == ListState.LARGE ? "LARGE" : "SMALL"));
        int containerWidth = ((View) getRv().getParent()).getMeasuredWidth();
        if (v instanceof ViewGroup) {
            return rvState == ListState.SMALL ? rvCollapsedWidth : containerWidth;
        }
        return containerWidth;
    }

    /**
     * Configures an animator adapter to trigger callbacks when the item animation ends.
     *
     * @param animated the animated view.
     * @return a {@link AnimatorListenerAdapter} if item background manipulation
     * is disabled or not set ({@link #enableBackground}), that sets the item view
     * background accordingly.
     */
    protected AnimatorListenerAdapter getWidthEndAnimationListener(View animated) {

        final Drawable expandedDrawable;
        final Drawable collapsedDrawable;
        if (expandedDrawableRes != null) {
            expandedDrawable = animated.getContext().getResources().getDrawable(expandedDrawableRes);
        } else {
            expandedDrawable = null;
        }
        if (collapsedDrawableRes != null) {
            collapsedDrawable = animated.getContext().getResources().getDrawable(collapsedDrawableRes);
        } else {
            collapsedDrawable = null;
        }

        if (animated != getRv()) {
            if (enableBackground == SetValues.DISABLED) {
                return new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
//                        Log.i(TAG, "onAnimationEnd: DISABLED");
                        if (animated != null && animated.getLayoutParams().width == -1) {
                            animated.setBackgroundDrawable(null);
                            animated.requestLayout();
                        }
                    }
                };
            }
            if (enableBackground == SetValues.NOT_SET) {
                return new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
//                        Log.i(TAG, "onAnimationEnd: NOT SET");
                        if (animated != null && animated.getLayoutParams().width == -1) {
                            animated.setBackgroundDrawable(rvState == ListState.LARGE ? expandedDrawable : collapsedDrawable);
                            animated.requestLayout();
                        }
                    }
                };
            }
        }
        return null;
    }

    public final void addRvResizeListener(BaseInterfaces.RvResizeListener listener) {
        resizeListeners.add(listener);
    }

    public void setResizePostProcessor(ResizePostProcessor.PostProcessor postProcessor) {
        resizePostProcessor = postProcessor;
// TODO: 9/24/18  the following block might not be needed. investigate.
        {
            RecyclerView.LayoutManager lm = getRv().getLayoutManager();
            if (lm instanceof ResizePostProcessor.PostProcessorValidatorConsumer) {
                ((ResizePostProcessor.PostProcessorValidatorConsumer) lm)
                        .setPostProcessorValidator(resizePostProcessor.getPostProcessorValidator());
            }
        }
    }

    @Override
    public void onPostProcessEnd() {
        if (rvState == ListState.LARGE) {
            AbstractAdapter adapter = (AbstractAdapter) rv.getAdapter();
            adapter.setPreviouslySelectedPosition(-1);
        }
    }


    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        configureMenuActionsProvider(fragmentManager);
    }

    /**
     * The ui ops classes contain menu actions providers that
     * would need at some point to start different fragments
     *
     * @param fm the fragment manger to pass to the menu
     *           actions providers
     */
    protected abstract void configureMenuActionsProvider(FragmentManager fm);


    /**
     * Convenience method to ease up on demand data refresh.
     */
    public void refreshData(boolean onlyLocal) {
        if (repository != null) {
            repository.refreshData(onlyLocal);
        }
    }

    @Override
    public void registerUnbinder(Unbinder unbinder) {
        if (!unbinderList.contains(unbinder)) {
            unbinderList.add(unbinder);
        }
    }

    @Override
    public boolean deRegisterUnbinder(Unbinder unbinder) {
        return false;
    }

    @Override
    public void clearBindings() {
        if (unbinderList != null) {
            Log.i(TAG, "clearBindings: " + unbinderList.size() + " (" + this.getClass().getSimpleName() + ")");
            for (Unbinder u : unbinderList) {
                if (u instanceof ResizeableItemViewHolder) {
                    ResizeableItemViewHolder vh = (ResizeableItemViewHolder) u;
                    Log.v(TAG, "clearBindings: " + u + "" + TextUtils.getItemText(vh));
                    u.unbind();
                }
            }
            unbinderList.clear();
        }
    }

    /**
     * Resets item cache for the Recycler View.
     * Also clears the recycled view pool for
     * that adapter. Take care if you use shared
     * recycled view pools.
     */
    protected void resetRv() {
        Log.i(TAG, "resetRv:     <   <  < < <<<<<<<<<<<<<<<<<");
        getRv().setItemViewCacheSize(0);        // rv moves cached views to rvPool...
        getRv().getRecycledViewPool().clear();  // ...so we clean this too
        clearBindings();
        getRv().setItemViewCacheSize(14);
    }

    public void shutdown() {
        if (repository != null) {
            repository.shutdown();
        }
        clearBindings();
    }

    protected abstract AbstractAdapter createNewAdapter(ItemViewProperties itemViewProperties);

}
