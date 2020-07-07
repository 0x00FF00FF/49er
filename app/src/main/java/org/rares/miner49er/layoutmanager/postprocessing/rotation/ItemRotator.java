package org.rares.miner49er.layoutmanager.postprocessing.rotation;

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import org.rares.miner49er.layoutmanager.postprocessing.ResizePostProcessor;

public interface ItemRotator extends ResizePostProcessor {

    int ROTATE_COUNTER_CLOCKWISE = -90;
    int ROTATE_CLOCKWISE = 90;
    int NO_ROTATION = 0;

    void rotateItems(ViewGroup viewGroup);

    void rotateItem(RecyclerView.ViewHolder viewHolder, boolean clockwise);

    void validateViewRotation(View view, boolean closedState, boolean isViewSelected);
}
