package org.rares.miner49er.layoutmanager.postprocessing.rotation;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;

public class SimpleItemRotator extends AbstractItemRotator {

    public SimpleItemRotator(RecyclerView recyclerView) {
        super(recyclerView);
    }

    @Override
    public void rotateItems(ViewGroup viewGroup) {
        RecyclerView rv = (RecyclerView) viewGroup;
        AbstractAdapter _tempAdapter = (AbstractAdapter) rv.getAdapter();

        float closedDimension = viewGroup.getContext().getResources().getDimension(R.dimen.projects_rv_collapsed_selected_item_width);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View itemView = viewGroup.getChildAt(i);
            RecyclerView.ViewHolder vh = rv.getChildViewHolder(itemView);
            ViewGroup vg = (ViewGroup) itemView;

            boolean closedState = _tempAdapter.getLastSelectedPosition() != -1;

            View childView = vg.getChildAt(0);

            int itemPosition = vh.getAdapterPosition();

            String text = _tempAdapter.resolveData(itemPosition);

            if (childView instanceof TextView) {
                TextView tv = (TextView) childView;
                if (text != null) {
                    tv.setText(text);
                }
            }

            validateViewRotation(itemView,
                    closedState,
                    _tempAdapter.getLastSelectedPosition() == itemPosition
            );
            itemView.invalidate();
            if (postProcessorConsumer != null) {
                postProcessorConsumer.onPostProcessEnd();
            }
        }
    }

    @Override
    public void rotateItem(RecyclerView.ViewHolder viewHolder, boolean clockwise) {
//        doing nothing here
    }

    @Override
    public void setPostProcessConsumer(PostProcessorConsumer postProcessConsumer) {
        this.postProcessorConsumer = postProcessConsumer;
    }

}
