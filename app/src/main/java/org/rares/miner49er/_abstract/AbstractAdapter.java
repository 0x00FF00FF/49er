package org.rares.miner49er._abstract;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.ListItemClickListener;

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
    private int lastSelectedPosition = -1, previouslySelectedPosition = -1;

    protected ListItemClickListener clickListener;


    /**
     * disable (custom=non {@link RecyclerView.ItemAnimator}) animation
     * and return true;
     */
    @Override
    public boolean onFailedToRecycleView(@NonNull ExtendedViewHolder holder) {
        // todo: disable animation
        return true;
    }

    @Override
    @CallSuper
    public void onBindViewHolder(@NonNull ExtendedViewHolder holder, int position) {
//        Log.i(TAG, "onBindViewHolder: ");
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
    public void onViewDetachedFromWindow(@NonNull ExtendedViewHolder holder) {
//        Log.i(TAG, "onViewDetachedFromWindow: " + holder.getItemProperties().getData());
    }

    @Override
    public void onViewRecycled(@NonNull ExtendedViewHolder holder) {
//        Log.i(TAG, "onViewRecycled: " + holder.getItemProperties().getData());
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ExtendedViewHolder holder) {
//        Log.i(TAG, "onViewAttachedToWindow: " + holder.getItemProperties().getData());
        // the following may not be needed if
        // the first itemView after the visible
        // ones in the recycler view list is not
        // drawn; but that solution could have
        // other (unwanted) implications in
        // scrolling behaviour.
        {
            TextView tv = (TextView) ((ViewGroup) holder.itemView).getChildAt(0);
            String newData = resolveData(holder.getAdapterPosition());
            if (tv.getText().length() != newData.length()) {
                tv.setText(newData);
            }
//            Log.w(TAG, "onViewAttachedToWindow: " + tv.getText());
        }

    }

    public abstract String resolveData(int position);

}
