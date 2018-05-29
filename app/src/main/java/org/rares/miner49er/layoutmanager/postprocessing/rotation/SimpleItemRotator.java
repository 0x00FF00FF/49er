package org.rares.miner49er.layoutmanager.postprocessing.rotation;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;

import static org.rares.miner49er.util.TextUtils.TAG;

public class SimpleItemRotator extends AbstractItemRotator {

    @Override
    public void rotateItems(ViewGroup viewGroup) {
        RecyclerView rv = (RecyclerView) viewGroup;
        AbstractAdapter _tempAdapter = (AbstractAdapter) rv.getAdapter();

        float closedDimension = viewGroup.getContext().getResources().getDimension(R.dimen.projects_rv_collapsed_selected_item_width);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View v = viewGroup.getChildAt(i);
            RecyclerView.ViewHolder vh = rv.getChildViewHolder(v);
            ViewGroup vg = (ViewGroup) v;
            TextView tv = (TextView) vg.getChildAt(0);

            boolean closedState = _tempAdapter.getLastSelectedPosition() != -1;

            int itemPosition = vh.getAdapterPosition();

            String text = _tempAdapter.resolveData(vh.getAdapterPosition());
            tv.setText(text);

            validateViewRotation(v,
                    closedState,
                    _tempAdapter.getLastSelectedPosition() == itemPosition
            );
            v.invalidate();
            if (postProcessorConsumer != null) {
                postProcessorConsumer.onPostProcessEnd();
            }
        }
    }

    @Override
    public void rotateItem(View view, boolean clockwise) {
//        view.animate().rotationBy(clockwise ? 90 : -90);
    }

    @Override
    public void validateViewRotation(View view, boolean closedState, boolean isViewSelected) {
        Log.i(TAG, "validateViewRotation: cALLED");
        super.validateViewRotation(view, closedState, isViewSelected);
    }

    @Override
    public void setPostProcessConsumer(PostProcessorConsumer postProcessConsumer) {
        this.postProcessorConsumer = postProcessConsumer;
    }

}
