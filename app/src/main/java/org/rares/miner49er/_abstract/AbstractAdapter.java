package org.rares.miner49er._abstract;

import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.rares.miner49er.BaseInterfaces.ListItemClickListener;

import lombok.Getter;
import lombok.Setter;

/**
 * @author rares
 * @since 07.03.2018
 */

public abstract class AbstractAdapter<ExtendedViewHolder extends ResizeableViewHolder>
        extends RecyclerView.Adapter<ExtendedViewHolder> {

    public static final String TAG = AbstractAdapter.class.getSimpleName();

    @Getter
    @Setter
    private int maxElevation = 0;

    @Getter
    @Setter
    private int parentColor;

    @Getter
    @Setter
    private int lastSelectedPosition = -1;

    protected ListItemClickListener clickListener;


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
        Log.i(TAG, "onBindViewHolder: ");
//        if (!viewHolders.contains(holder)) {
//            viewHolders.add(holder);
//        }
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
        Log.i(TAG, "onViewDetachedFromWindow: " + holder.getItemProperties().getData());
    }

    @Override
    public void onViewRecycled(ExtendedViewHolder holder) {
        Log.i(TAG, "onViewRecycled: " + holder.getItemProperties().getData());
//        if (holder.getItemProperties().isSelected()) {
//            setLastSelectedPosition(holder.getItemProperties().getItemContainerCustomId());
//        }
//        holder.resizeItemView(true);
    }

    @Override
    public void onViewAttachedToWindow(ExtendedViewHolder holder) {
        Log.i(TAG, "onViewAttachedToWindow: " + holder.getItemProperties().getData());
//        if (getLastSelectedPosition() == holder.getItemProperties().getItemContainerCustomId()) {
//            holder.getItemProperties().setSelected(true);
//            holder.resizeItemView(false);
//        }
    }

}
