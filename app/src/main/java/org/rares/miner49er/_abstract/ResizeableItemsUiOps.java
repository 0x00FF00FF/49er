package org.rares.miner49er._abstract;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.R;
import org.rares.miner49er.layoutmanager.ItemAnimationDto;
import org.rares.miner49er.layoutmanager.ResizeableLayoutManager;
import org.rares.miner49er.layoutmanager.postprocessing.ResizePostProcessor;
import org.rares.miner49er.util.ArgbEvaluator;
import org.rares.miner49er.util.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author rares
 * @since 02.03.2018
 */

public abstract class ResizeableItemsUiOps
        implements
        BaseInterfaces.ListItemEventListener,
        BaseInterfaces.ResizeableItems,     // is this really necessary?
        ResizePostProcessor.PostProcessorConsumer,
        BaseInterfaces.UnbinderHost {

    private static String TAG = ResizeableItemsUiOps.class.getSimpleName();

    private List<BaseInterfaces.RvResizeListener> resizeListeners = new ArrayList<>();

    @Setter
    protected BaseInterfaces.DomainLink domainLink;

    @Setter
    private int rvCollapsedWidth;

    protected List<Unbinder> unbinderList = new ArrayList<>();

    protected RecyclerView rv;

    @Getter
    private ListState rvState = ListState.LARGE;

    private ResizePostProcessor.PostProcessor resizePostProcessor;

    protected Repository repository;

    protected Guideline guideline = null;

    protected int indigo = 0;
    protected int white = 0;
    protected int bgLeft = 0;
    protected int bgRight = 0;
    protected int bgLeftSelected = 0;
    protected int bgRightSelected = 0;

    private CompositeDisposable disposables = new CompositeDisposable();
    private Disposable rvWidthDisposable = null;

    private long lastUserAction = -1;

    @Override
    public void resetLastSelectedId() {
        AbstractAdapter _tempAdapter = (AbstractAdapter) rv.getAdapter();
        _tempAdapter.setLastSelectedPosition(-1);
    }

    @Override
    public boolean onListItemClick(ResizeableItemViewHolder holder) {
        Log.e(TAG, "onListItemClick: _________________________________________");
        long now = System.currentTimeMillis();
        if (lastUserAction != -1) {
            if (now - lastUserAction < 300) {
                Log.w(TAG, "resizeRv: EATEN EVENT");
                lastUserAction = now;
                return false;
            }else{
                Log.w(TAG, "onListItemClick: " + now + "-" + lastUserAction + "=" + (now - lastUserAction) );
            }
        }
        lastUserAction = now;

        int adapterPosition = holder.getAdapterPosition();
        boolean enlarge = selectItem(adapterPosition);

//        domainLink.onParentSelected(holder.getItemProperties(), enlarge);

        RecyclerView.LayoutManager layoutManager = rv.getLayoutManager();

        if (layoutManager instanceof ResizeableLayoutManager) {
            ResizeableLayoutManager mgr = (ResizeableLayoutManager) layoutManager;
            {
                if (enlarge) {
                    mgr.setSelected(-1, null);
                } else {
                    mgr.setSelected(adapterPosition, holder.itemView);
                }
//                List<ItemAnimationDto> animatedItemsList = mgr.resizeSelectedView(holder.itemView, enlarge);
//                for (ItemAnimationDto ia : animatedItemsList) {
//                    resizeAnimated(ia);
//                }
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
        AbstractAdapter _tempAdapter = ((AbstractAdapter) rv.getAdapter());
        final int prevSelected = _tempAdapter.getLastSelectedPosition();
        _tempAdapter.setPreviouslySelectedPosition(prevSelected);

        boolean enlarge = prevSelected == selectedPosition;

        rvState = enlarge ? ListState.LARGE : ListState.SMALL;

        dispatchStartResizeEvents(enlarge);

        Log.d(TAG, "selectItem: rvState " + (rvState == ListState.LARGE ? "LARGE" : "SMALL"));
        if (enlarge) {
            resetLastSelectedId();
            return true;
        }

        _tempAdapter.setLastSelectedPosition(selectedPosition);
        return false;
    }

    private void dispatchStartResizeEvents(boolean enlarge) {
        for (BaseInterfaces.RvResizeListener resizeListener : resizeListeners) {
            if (enlarge) {
                resizeListener.onRvExpanding();
            } else {
                resizeListener.onRvCollapsing();
            }
        }
    }

    private void dispatchEndResizeEvents(boolean enlarge) {
        for (BaseInterfaces.RvResizeListener resizeListener : resizeListeners) {
            if (enlarge) {
                resizeListener.onRvExpanded();
            } else {
                resizeListener.onRvCollapsed();
            }
        }
    }

    private void dispatchPostProcess(boolean enlarge, int selectedPosition, PublishProcessor processor) {
        Log.d(TAG, "dispatchPostProcess() called");
        // post process views
        if (resizePostProcessor != null) {
            resizePostProcessor.postProcess(rv, processor);
        }
    }

    protected void resizeRv(boolean enlarge) {
        Log.i(TAG, "resizeRv: START");
        boolean animationEnabled = true;
        AbstractAdapter _tempAdapter = (AbstractAdapter) rv.getAdapter();
        if (_tempAdapter == null) {
            return;
        }
        final int selectedPos = _tempAdapter.getLastSelectedPosition();
        int rvParentWidth = ((View) rv.getParent()).getWidth();
//        int width = enlarge ? 0 : rvParentWidth-rvCollapsedWidth; // guideline width   // -> enlarge/collapse strategy
        int width = enlarge ? 0 : -rvParentWidth + rvCollapsedWidth;// x translation    //
        int elevation = enlarge ? 0 : _tempAdapter.getMaxElevation();

        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();

        ItemAnimationDto itemAnimationDto = new ItemAnimationDto.Builder(guideline).elevation(elevation).width(width).build();

        ViewPropertyAnimator rvViewAnimator = rv.animate();

        if (animationEnabled) {
            // if the user double clicks the selected item or clicks while translation is happening
            // cancel the EAR runnable that puts the guideline at collapsed position
            rvViewAnimator.setListener(null).cancel();
            if (rvWidthDisposable != null && !rvWidthDisposable.isDisposed()) {
                rvWidthDisposable.dispose();
            }
            if (enlarge) {                    // do the SELECTED->NORMAL collapse
                PublishProcessor<Boolean> animationEndedProcessor = PublishProcessor.create();
                rvWidthDisposable = animationEndedProcessor
                        .debounce(80, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((event) -> {
                            if (lp.guideEnd != width) { // no use to rerun code if the guideline is already at its place
                                // before moving on with putting the guideline back
                                // and translating
                                lp.guideEnd = width; // (0)
                                guideline.setLayoutParams(lp);
                                rv.setTranslationX(-rvParentWidth + rvCollapsedWidth);
                            }
                            if (rv.getTranslationX() != 0) {
                                rvViewAnimator
                                        .setStartDelay(200)
                                        .translationX(0);
                            }
                        });
                disposables.add(rvWidthDisposable);
                dispatchPostProcess(true, selectedPos, animationEndedProcessor);
            } else {
                rvViewAnimator
                        .setStartDelay(rv.getTranslationX() == width ? 0 : 200)
                        .translationX(width);
/*                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                dispatchPostProcess(false, selectedPos);

                                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) g.getLayoutParams();
                                if (lp.guideEnd != -width) {
                                    for (int i = 0; i < rv.getChildCount(); i++) {
                                        RecyclerView.ViewHolder vh = rv.getChildViewHolder(rv.getChildAt(i));
                                        if (vh instanceof ItemViewAnimator) {
                                            ((ItemViewAnimator) vh).validateItem(true,
                                                    vh.getAdapterPosition() == selectedPos);
                                        }
                                    }

                                    lp.guideEnd = -width;
                                    g.setLayoutParams(lp);
                                    rv.setTranslationX(0);
                                }

                            }
                        });*/
                rvViewAnimator
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // get Guideline per extended class
                                if (ear == null) {
                                    ear = new EndAnimationRunnable(width, guideline);
                                } else {
                                    ear.g = guideline;
                                    ear.guidelinePosition = width;
                                }
                                ear.selectedPosition = selectedPos;
                                ear.enlarge = false;
                                rv.post(ear);
                            }
                        });
            }
            rvViewAnimator.start();
//            resizeRvAnimated(itemAnimationDto);
        } else {
            rv.getLayoutParams().width = width;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rv.setElevation(elevation);
            }
            rv.requestLayout();
        }
        dispatchEndResizeEvents(enlarge);
        Log.i(TAG, "resizeRv: END");
    }

    private class EndAnimationRunnable implements Runnable {
        public boolean ok = true;
        int selectedPosition;
        boolean enlarge;

        int guidelinePosition;
        Guideline g;

        EndAnimationRunnable(int guidelinePosition, Guideline guideline) {
            this.g = guideline;
            this.guidelinePosition = guidelinePosition;
        }

        @Override
        public void run() {
            if (ok) {
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) g.getLayoutParams();
                Log.w(TAG, "run:" + lp.guideEnd + "!=" + -guidelinePosition);
                if (lp.guideEnd != -guidelinePosition) {
                    lp.guideEnd = -guidelinePosition;
                    g.setLayoutParams(lp);
                    rv.setTranslationX(0);
                }
                dispatchPostProcess(enlarge, selectedPosition, null);
            }
        }
    }

    protected EndAnimationRunnable ear = null;

    protected ValueAnimator.AnimatorUpdateListener rvResizeListener = animation -> {
        int guidelinePosition = (int) animation.getAnimatedValue("guidelinePosition");
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        Log.i(TAG, "guideline position: " + guidelinePosition + " " + lp.guideEnd);

        guideline.setGuidelineEnd(guidelinePosition);
    };

    private void resizeRvAnimated(final ItemAnimationDto animatedItem) {
        final View v = animatedItem.getAnimatedView();
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) v.getLayoutParams();
        int startWidth = lp.guideEnd;
        int endWidth = animatedItem.getWidth();
        PropertyValuesHolder widthHolder = PropertyValuesHolder.ofInt("guidelinePosition", startWidth, endWidth);
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(widthHolder);
        animator.addUpdateListener(rvResizeListener);
        animator.start();
    }

    /**
     * Resize a view by using animation.
     */
    protected void resizeAnimated(final ItemAnimationDto animatedItem) {
        final View v = animatedItem.getAnimatedView();
        float endElevation = animatedItem.getElevation();
        final int endWidth = animatedItem.getWidth();
        final int parentWidth = getParentWidth(v);
        int startAnimationWidth = endWidth == ViewGroup.LayoutParams.MATCH_PARENT ? parentWidth : endWidth;
        int startWidth = v.getWidth();
        float startElevation = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startElevation = v.getElevation();
        }

//        final PropertyValuesHolder pvhW = PropertyValuesHolder.ofInt("width", startWidth, startAnimationWidth);
        PropertyValuesHolder pvhE = PropertyValuesHolder.ofFloat("elevation", startElevation, endElevation);
        // background manipulation

        boolean selected = endWidth != ViewGroup.LayoutParams.MATCH_PARENT;
        int startColor = selected ? indigo : white;
        int endColor = selected ? white : indigo;
        int startLeftBgColor = selected ? bgLeft : bgLeftSelected;
        int endLeftBgColor = selected ? bgLeftSelected : bgLeft;
        int startRightBgColor = selected ? bgRight : bgRightSelected;
        int endRightBgColor = selected ? bgRightSelected : bgRight;
        PropertyValuesHolder pvhC = PropertyValuesHolder.ofObject("strokeColor", ArgbEvaluator.getInstance(), startColor, endColor);
        PropertyValuesHolder pvhBgCL = PropertyValuesHolder.ofObject("bgColorL", ArgbEvaluator.getInstance(), startLeftBgColor, endLeftBgColor);
        PropertyValuesHolder pvhBgCR = PropertyValuesHolder.ofObject("bgColorR", ArgbEvaluator.getInstance(), startRightBgColor, endRightBgColor);

//        Animation animation = new AnimationSet(true);
//
//        LayoutAnimationController animationController = new LayoutAnimationController(animation);

        ValueAnimator anim = ValueAnimator.ofPropertyValuesHolder(/*pvhW,*/ pvhE, pvhC, pvhBgCL, pvhBgCR);

        anim.removeAllUpdateListeners();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {


                Drawable bg = v.getBackground();
                if (bg instanceof LayerDrawable) {
                    bg.mutate();
                    int animatedC = (int) animation.getAnimatedValue("strokeColor");
                    int animatedBgL = (int) animation.getAnimatedValue("bgColorL");
                    int animatedBgR = (int) animation.getAnimatedValue("bgColorR");
                    LayerDrawable backgroundLayers = (LayerDrawable) bg;
                    GradientDrawable strokeRectangle = (GradientDrawable) backgroundLayers.findDrawableByLayerId(R.id.outside_stroke);
                    if (strokeRectangle != null) {
                        strokeRectangle.setStroke(v.getResources().getDimensionPixelOffset(R.dimen.projects_list_item_background_stroke_width), animatedC);
                    }
                    GradientDrawable bgRectangle = (GradientDrawable) backgroundLayers.findDrawableByLayerId(R.id.semitransparent_background);
                    if (bgRectangle != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            bgRectangle.setColors(new int[]{animatedBgL, animatedBgR});
                        }
                    }
                }

//                int animatedW = (int) animation.getAnimatedValue("width");
//                if (endWidth == ViewGroup.LayoutParams.MATCH_PARENT && animatedW == parentWidth) {
//                    animatedW = ViewGroup.LayoutParams.MATCH_PARENT;
//                }
                float animatedE = (float) animation.getAnimatedValue("elevation");
//                v.getLayoutParams().width = animatedW;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(animatedE);
                } else {
                    v.bringToFront();
                }
                v.requestLayout();
            }
        });
        anim.setDuration(200);
        anim.start();
    }

    private int getParentWidth(View v) {
        Log.e(TAG, "getParentWidth: rvState " + (rvState == ListState.LARGE ? "LARGE" : "SMALL"));
        int containerWidth = ((View) rv.getParent()).getMeasuredWidth();
        if (v instanceof ViewGroup) {
            return rvState == ListState.SMALL ? rvCollapsedWidth : containerWidth;
        }
        return containerWidth;
    }

    public void setResizePostProcessor(ResizePostProcessor.PostProcessor postProcessor) {
        resizePostProcessor = postProcessor;
// TODO: 9/24/18  the following block might not be needed. investigate.
        {
            RecyclerView.LayoutManager lm = rv.getLayoutManager();
            if (lm instanceof ResizePostProcessor.PostProcessorValidatorConsumer) {
                ((ResizePostProcessor.PostProcessorValidatorConsumer) lm)
                        .setPostProcessorValidator(resizePostProcessor.getPostProcessorValidator());
            }
        }
    }


    @Override
    public void onPostProcessEnd() {
        Log.d(TAG, "onPostProcessEnd() called");
        if (rvState == ListState.LARGE) {
            AbstractAdapter adapter = (AbstractAdapter) rv.getAdapter();
            adapter.setPreviouslySelectedPosition(-1);
        }
    }

    public final void addRvResizeListener(BaseInterfaces.RvResizeListener listener) {
        resizeListeners.add(listener);
    }

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
        rv.setItemViewCacheSize(0);        // rv moves cached views to rvPool...
        rv.getRecycledViewPool().clear();  // ...so we clean this too
        clearBindings();
        rv.setItemViewCacheSize(14);
    }

    public void shutdown() {
        if (repository != null) {
            repository.shutdown();
        }
        if (rvWidthDisposable != null) {
            rvWidthDisposable.dispose();
        }
        disposables.dispose();
        clearBindings();
    }

    protected abstract AbstractAdapter createNewAdapter(ItemViewProperties itemViewProperties);

}
