package org.rares.miner49er._abstract;

import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author rares
 * @since 07.03.2018
 */

public abstract class AbstractAdapter<ExtendedViewHolder extends ResizeableViewHolder>
        extends RecyclerView.Adapter<ExtendedViewHolder> {

    @Getter @Setter
    private int parentColor;

    protected ResizeableItemsUiOps ops;
    protected List<ResizeableViewHolder> viewHolders = new ArrayList<>();  // -- this can leak :(
    // TODO: this badly needs refactoring.


    /**
     * disable (custom=non {@link RecyclerView.ItemAnimator}) animation
     * and return true;
     */
    @Override
    public boolean onFailedToRecycleView(ExtendedViewHolder holder) {
        // todo: disable animation
        return true;
    }

    @Override
    @CallSuper
    public void onBindViewHolder(ExtendedViewHolder holder, int position) {
        if (!viewHolders.contains(holder)) {
            viewHolders.add(holder);
        }
        int bgColor = parentColor + ((position % 2 == 0 ? 1 : -1) * 15);
        holder.itemView.setBackgroundColor(bgColor);
        holder.getItemProperties().setItemBgColor(bgColor);
        // TODO: 07.03.2018 use a more intelligent compare mechanism + check if adapter has fixed ids
//        if (holder.getItemProperties().getData().equals(getData(position))) {
//            holder.setToBeRebound(false);
//        } else {
//            holder.setToBeRebound(true);
//        }
    }

    @Override
    public void onViewDetachedFromWindow(ExtendedViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(ExtendedViewHolder holder) {
        if (holder.getItemProperties().isSelected()) {
            ops.setLastSelectedId(holder.getItemProperties().getItemContainerCustomId());
        }
        holder.resizeItemView(true);
        super.onViewRecycled(holder);
    }

    @Override
    public void onViewAttachedToWindow(ExtendedViewHolder holder) {
        if (ops.getLastSelectedId() == holder.getItemProperties().getItemContainerCustomId()) {
            holder.getItemProperties().setSelected(true);
            holder.resizeItemView(false);
        }
        super.onViewAttachedToWindow(holder);
    }
}
