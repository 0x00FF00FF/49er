package org.rares.miner49er.layoutmanager.postprocessing.rotation;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.util.TextUtils;

/**
 * Default implementation for a rotation post processor.
 * This class should be extended to further customize
 * rotation post processing.
 */
public abstract class AbstractItemRotator
        implements
        ItemRotator.PostProcessor,
        ItemRotator.PostProcessorValidator,
        ItemRotator {

    private static final String TAG = AbstractItemRotator.class.getSimpleName();

    protected RecyclerView rv;

    PostProcessorConsumer postProcessorConsumer = null;

    AbstractItemRotator(RecyclerView rv) {
        this.rv = rv;
    }

    @Override
    public void postProcess(RecyclerView rv) {
        RecyclerView.Adapter _tempAdapter = rv.getAdapter();
        if (_tempAdapter == null) {
            Log.w(TAG, "postProcess: SKIP ITEM ROTATION, adapter is null.");
            return;
        }
        int ic = _tempAdapter.getItemCount();
        int cc = rv.getChildCount();
        if (ic < cc) {
            Log.w(TAG, "rotateItems: skipping rotation. " +
                    "adapter item count does not match rv child count! " +
                    "(" + ic + " vs " + cc + ")");
        } else {
            rotateItems(rv);
        }
    }

    @Override
    public void validateItemPostProcess(View view, boolean isViewGroupCollapsed, boolean isViewSelected) {
        if (rv != null) {
            RecyclerView.ViewHolder holder = rv.findContainingViewHolder(view);
            if (holder != null) {
                if (holder instanceof ResizeableItemViewHolder) {
                    ResizeableItemViewHolder h = (ResizeableItemViewHolder) holder;
                    h.toggleItemText(isViewGroupCollapsed);
                }
            }
        }
        validateViewRotation(view, isViewGroupCollapsed, isViewSelected);
    }

    /**
     * Default implementation for validating rotation: <ul>
     * <li>decide if view should be rotated clockwise or counterclockwise</li>
     * <li>rotate the view.</li>
     * </ul>
     *
     * @param view           the view to act onto.
     * @param closedState    boolean showing if parent is enlarged or not.
     * @param isViewSelected boolean showing whether the current view is selected
     */
    @Override
    public void validateViewRotation(View view, boolean closedState, boolean isViewSelected) {
        int rotationDirection = decideRotation(view, closedState, isViewSelected);
        switch (rotationDirection) {
            case ROTATE_CLOCKWISE:
                rotateView(view, true);
                break;
            case ROTATE_COUNTER_CLOCKWISE:
                rotateView(view, false);
                break;
            case NO_ROTATION:
            default:
                // do nothing :)
        }
    }

    private int decideRotation(View itemView, boolean closedState, boolean isViewSelected) {
        // if item size is small, item should be rotated -90
        // if item is selected item, item should be at 0
        // if item size is large, item should be rotated at 0
        View viewToProcess = ((ViewGroup) itemView).getChildAt(0);

        if (closedState) {
            if (viewToProcess.getRotation() > -90) {
                if (!isViewSelected) {
                    return ItemRotator.ROTATE_COUNTER_CLOCKWISE;
                }
            }
            if (viewToProcess.getRotation() < 0) {
                if (isViewSelected) {
                    return ItemRotator.ROTATE_CLOCKWISE;
                }
            }
        } else {
            if (viewToProcess.getRotation() < 0) {
                return ItemRotator.ROTATE_CLOCKWISE;
            }
        }
        return ItemRotator.NO_ROTATION;
    }

    /**
     * Default implementation for validating view rotation.
     * Rotates a view by +/- 90 degrees.
     *
     * @param itemView  the item view to be acted on.
     * @param clockwise the direction of rotation.
     */
    private void rotateView(View itemView, boolean clockwise) {
        View rotatedView = ((ViewGroup) itemView).getChildAt(0);
        rotatedView.setRotation(clockwise ? 0 : -90);

        if (rotatedView instanceof TextView) {
            TextView tv = (TextView) rotatedView;
            if (clockwise) {
                TextUtils.setCenterStartGravity(tv);
            } else {
                TextUtils.setCenterGravity(tv);
            }
        }
    }

    @Override
    public PostProcessorValidator getPostProcessorValidator() {
        return this;
    }
}





