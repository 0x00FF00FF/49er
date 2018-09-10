package org.rares.miner49er._abstract;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.Unbinder;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.layoutmanager.ItemAnimationDto;
import org.rares.miner49er.layoutmanager.ResizeableLayoutManager;
import org.rares.miner49er.layoutmanager.postprocessing.ResizePostProcessor;
import org.rares.miner49er.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

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

    @Getter
    @Setter
    private RecyclerView rv;

    @Getter
    private ListState rvState = ListState.LARGE;

    private ResizePostProcessor.PostProcessor resizePostProcessor;

    protected Repository repository;

    @Override
    public void resetLastSelectedId() {
        AbstractAdapter _tempAdapter = (AbstractAdapter) getRv().getAdapter();
        _tempAdapter.setLastSelectedPosition(-1);
    }

    @Override
    public boolean onListItemClick(ResizeableViewHolder holder) {
        int adapterPosition = holder.getAdapterPosition();
        boolean enlarge = selectItem(adapterPosition);

        domainLink.onParentSelected(holder.getItemProperties(), enlarge);

        RecyclerView.LayoutManager layoutManager = getRv().getLayoutManager();

        if (layoutManager instanceof ResizeableLayoutManager) {
            ResizeableLayoutManager mgr = (ResizeableLayoutManager) layoutManager;
            {

                /*-
                FIXME
                this block smells.
                the layout manager needs to get the view itself, not to have it supplied;
                the way this is now, there can be differences between the selected position and the selected view
                 -*/

                mgr.setSelectedPosition(enlarge ? -1 : adapterPosition);
                List<ItemAnimationDto> animatedItemsList = mgr.resizeSelectedView(holder.itemView, enlarge);
                for (ItemAnimationDto ia : animatedItemsList) {
                    resizeAnimated(ia);
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

        Log.d(TAG, "selectItem: rvState " + (rvState == ListState.LARGE ? "LARGE" : "SMALL"));
        if (enlarge) {
            resetLastSelectedId();
            return true;
        }

        _tempAdapter.setLastSelectedPosition(selectedPosition);
//        _tempAdapter.notifyItemChanged(prevSelected);
//        _tempAdapter.notifyItemChanged(selectedPosition);
        return false;
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
//        RecyclerView.LayoutManager _tempLm = getRv().getLayoutManager();
        int width = enlarge ? ViewGroup.LayoutParams.MATCH_PARENT : rvCollapsedWidth;
        int elevation = enlarge ? 0 : (_tempAdapter == null ? 0 : _tempAdapter.getMaxElevation());

        ItemAnimationDto itemAnimationDto = new ItemAnimationDto(getRv(), elevation, width);
        if (animationEnabled) {
            resizeAnimated(itemAnimationDto);
        } else {
            getRv().getLayoutParams().width = width;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getRv().setElevation(elevation);
            }
            getRv().requestLayout();
        }
        // post process views
        if (resizePostProcessor != null) {
            resizePostProcessor.postProcess(getRv());
        }
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

        final PropertyValuesHolder pvhW = PropertyValuesHolder.ofInt("width", startWidth, startAnimationWidth);
        PropertyValuesHolder pvhE = PropertyValuesHolder.ofFloat("elevation", startElevation, endElevation);

        ValueAnimator anim;
        final ResizeableViewHolder holder = getHolder(v);
        if (holder != null && holder.getAnimator() != null) {
            anim = (ValueAnimator) holder.getAnimator();
        } else {
            if (v.getTag(BaseInterfaces.TAG_ANIMATOR) != null) {        // // FIXME: 8/17/18
                anim = (ValueAnimator) v.getTag(BaseInterfaces.TAG_ANIMATOR);
            } else {
                anim = ValueAnimator.ofPropertyValuesHolder(pvhW, pvhE);
            }
        }

        anim.removeAllUpdateListeners();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedW = (int) animation.getAnimatedValue("width");
                if (endWidth == ViewGroup.LayoutParams.MATCH_PARENT && animatedW == parentWidth) {
                    animatedW = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                float animatedE = (float) animation.getAnimatedValue("elevation");
                v.getLayoutParams().width = animatedW;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(animatedE);
                }
                v.requestLayout();
            }
        });
        anim.setDuration(200);
        anim.start();
    }

    private int getParentWidth(View v) {
        Log.e(TAG, "getParentWidth: rvState " + (rvState == ListState.LARGE ? "LARGE" : "SMALL"));
        int containerWidth = ((View) getRv().getParent()).getMeasuredWidth();
        if (v instanceof LinearLayout) {
            return rvState == ListState.SMALL ? rvCollapsedWidth : containerWidth;
        }
        return containerWidth;
    }

    private ResizeableViewHolder getHolder(View v) {
        ResizeableViewHolder holder = null;
        if (v instanceof LinearLayout) {
            holder = (ResizeableViewHolder) getRv().getChildViewHolder(v);
        }
        return holder;
    }

    public void setResizePostProcessor(ResizePostProcessor.PostProcessor postProcessor) {
        resizePostProcessor = postProcessor;
        RecyclerView.LayoutManager lm = getRv().getLayoutManager();
        if (lm instanceof ResizePostProcessor.PostProcessorValidatorConsumer) {
            ((ResizePostProcessor.PostProcessorValidatorConsumer) lm)
                    .setPostProcessorValidator(resizePostProcessor.getPostProcessorValidator());
        }
    }

    @Override
    public void onPostProcessEnd() {
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
                RecyclerView.ViewHolder vh = (RecyclerView.ViewHolder) u;
                Log.v(TAG, "clearBindings: " + u + "" + TextUtils.getItemText(vh.itemView));
                u.unbind();
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
