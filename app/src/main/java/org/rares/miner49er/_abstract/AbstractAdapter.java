package org.rares.miner49er._abstract;

import android.graphics.Color;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;
import io.reactivex.functions.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.ListItemEventListener;

import java.util.List;

/**
 * @author rares
 * @since 07.03.2018
 */

public abstract class AbstractAdapter<ExtendedViewHolder extends ResizeableViewHolder>
        extends RecyclerView.Adapter<ExtendedViewHolder>
        implements Consumer<List>
{

    public static final String TAG = AbstractAdapter.class.getSimpleName();

    @Getter
    @Setter
    private int maxElevation = 0;

    @Getter
    @Setter
    private int parentColor = Color.parseColor("#cbbeb5");

    @Getter
    @Setter
    private int lastSelectedPosition = -1, previouslySelectedPosition = -1;

    protected ListItemEventListener eventListener;


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
//        Log.d(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");
//        int bgColor = parentColor + ((position % 2 == 0 ? 1 : -1) * 15);
//        holder.itemView.setBackgroundColor(bgColor);
//        holder.getItemProperties().setItemBgColor(bgColor);
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

    public abstract void clearData();

    public abstract String resolveData(int position);
}
