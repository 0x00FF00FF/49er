package org.rares.miner49er._abstract;

import android.graphics.Color;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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

public abstract class AbstractAdapter<ExtendedViewHolder extends ResizeableItemViewHolder>
        extends RecyclerView.Adapter<ExtendedViewHolder>
        implements Consumer<List> {

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
        // for when the view is animating and is scrolled out of the visible area
        if (holder.getAnimator() != null && holder.getAnimator().isRunning()) {
            holder.getAnimator().end();
        }
    }

    @Override
    public void onViewRecycled(@NonNull ExtendedViewHolder holder) {
//        if (holder instanceof ProjectsViewHolder) {
//            ProjectsViewHolder projectsViewHolder = (ProjectsViewHolder) holder;
//            projectsViewHolder.clearImages();
//        }
//        Log.i(TAG, "onViewRecycled() called with: holder = [" + holder.hashCode() + "]");
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ExtendedViewHolder holder) {
        if (holder instanceof ItemViewAnimator) {

//            Log.i(TAG, "onViewAttachedToWindow: " + holder.getItemText());

            ((ItemViewAnimator) holder).validateItem(
                    getLastSelectedPosition() != -1,
                    holder.getAdapterPosition() == getLastSelectedPosition());
        }
//        Log.i(TAG, "onViewAttachedToWindow() called with: holder = [" + holder.hashCode() + "]");
    }

    public abstract void clearData();

    public abstract String resolveData(int position);

    public abstract Object getDisplayData(int adapterPosition);
}
