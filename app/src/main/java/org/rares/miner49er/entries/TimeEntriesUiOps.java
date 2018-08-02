package org.rares.miner49er.entries;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er._abstract.ResizeableViewHolder;
import org.rares.miner49er.entries.adapter.TimeEntriesAdapter;

/**
 * @author rares
 * @since 14.03.2018
 */

public class TimeEntriesUiOps extends ResizeableItemsUiOps
        implements DomainLink {

    public static final String TAG = TimeEntriesUiOps.class.getSimpleName();

    @Override
    public boolean onListItemClick(ResizeableViewHolder holder) {
        Log.d(TAG, "onListItemClick: [[ TIME ENTRY ]] :::: " + (((TextView)((ViewGroup)holder.itemView).getChildAt(0))).getText());
        return true;
    }

    @Override
    public void onParentSelected(ItemViewProperties viewProperties, boolean enlarge) {
        getRv().setAdapter(createNewTimeEntriesAdapter(viewProperties));
//        resizeRv(!enlarge);
    }

    @Override
    public void onParentRemoved(ItemViewProperties viewProperties) {
        if (viewProperties != null) {
            getRv().setAdapter(createNewTimeEntriesAdapter(viewProperties));
        }
    }

    private TimeEntriesAdapter createNewTimeEntriesAdapter(ItemViewProperties viewProperties) {
        TimeEntriesAdapter teAdapter = new TimeEntriesAdapter(this);
        teAdapter.setParentColor(viewProperties.getItemBgColor());
        return teAdapter;
    }
}
