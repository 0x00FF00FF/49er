package org.rares.miner49er._abstract;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import io.reactivex.functions.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces;
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

    @Setter
    protected BaseInterfaces.UnbinderHost unbinderHost = null;

    /**
     * disable (custom=non {@link RecyclerView.ItemAnimator}) animation
     * and return true;
     */
    @Override
    public boolean onFailedToRecycleView(@NonNull ExtendedViewHolder holder) {
        // todo: disable animation
        Log.e(TAG, "onFailedToRecycleView: WELL... " + holder.hashCode());
        return true;
    }

    @Override
    public void onBindViewHolder(@NonNull ExtendedViewHolder holder, int position) {
//        Log.d(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ExtendedViewHolder holder) {
//        Log.e(TAG, "onViewDetachedFromWindow() called with: holder = [" + holder.hashCode() + "]");
    }

    @Override
    public void onViewRecycled(@NonNull ExtendedViewHolder holder) {
//        Log.i(TAG, "onViewRecycled() called with: holder = [" + holder.hashCode() + "]");
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ExtendedViewHolder holder) {
//        Log.d(TAG, "onViewAttachedToWindow() called with: holder = [" + holder.hashCode() + "]");
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
