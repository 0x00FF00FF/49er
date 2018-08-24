package org.rares.miner49er.layoutmanager.postprocessing.rotation;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.rares.miner49er.util.TextUtils;

public abstract class AbstractItemRotator
        implements
        ItemRotator.PostProcessor,
        ItemRotator.PostProcessorValidator,
        ItemRotator {

    private static final String TAG = AbstractItemRotator.class.getSimpleName();

    PostProcessorConsumer postProcessorConsumer = null;

    @Override
    public void postProcess(RecyclerView rv) {
        RecyclerView.Adapter _tempAdapter = rv.getAdapter();
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
        Log.d(TAG, "validateItemPostProcess() called with: isViewGroupCollapsed = [" + isViewGroupCollapsed + "], isViewSelected = [" + isViewSelected + "]");
        validateViewRotation(view, isViewGroupCollapsed, isViewSelected);
    }

    @Override
    public void validateViewRotation(View view, boolean closedState, boolean isViewSelected) {
        ItemRotation rotationDirection = decideRotation(view, closedState, isViewSelected);
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

    private ItemRotation decideRotation(View itemView, boolean closedState, boolean isViewSelected) {
        // if item size is small, item should be rotated -90
        // if item is selected item, item should be at 0
        // if item size is large, item should be rotated at 0
        View viewToProcess = ((ViewGroup) itemView).getChildAt(0);

        if (closedState) {
            if (viewToProcess.getRotation() > -90) {
                if (!isViewSelected) {
                    return ItemRotation.ROTATE_COUNTER_CLOCKWISE;
                }
            }
            if (viewToProcess.getRotation() < 0) {
                if (isViewSelected) {
                    return ItemRotation.ROTATE_CLOCKWISE;
                }
            }
        } else {
            if (viewToProcess.getRotation() < 0) {
                return ItemRotation.ROTATE_CLOCKWISE;
            }
        }
        return ItemRotation.NO_ROTATION;
    }

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





