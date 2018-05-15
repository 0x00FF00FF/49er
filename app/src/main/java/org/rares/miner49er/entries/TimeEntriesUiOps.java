package org.rares.miner49er.entries;

import android.util.Log;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er._abstract.ResizeableViewHolder;
import org.rares.miner49er.entries.adapter.TimeEntriesAdapter;
import org.rares.miner49er.entries.adapter.TimeEntryViewProperties;
import org.rares.miner49er.util.NumberUtils;

/**
 * @author rares
 * @since 14.03.2018
 */

public class TimeEntriesUiOps extends ResizeableItemsUiOps
        implements DomainLink {

    public static final String TAG = TimeEntriesUiOps.class.getSimpleName();

    @Override
    public void onListItemClick(ResizeableViewHolder holder) {
        TimeEntryViewProperties tvp = (TimeEntryViewProperties) holder.getItemProperties();
        Log.d(TAG, "onListItemClick: [[ TIME ENTRY ]] :::: " + tvp.getText());
    }

    @Override
    public void onParentSelected(ItemViewProperties viewProperties, boolean enlarge) {
        getRv().setAdapter(createNewTimeEntriesAdapter(viewProperties));
//        resizeItems(getLastSelectedId()); //todo:test this. is this still needed?
        resizeRv(!enlarge);
    }

    @Override
    public void onParentRemoved(ItemViewProperties viewProperties) {
        if (viewProperties != null) {
            getRv().setAdapter(createNewTimeEntriesAdapter(viewProperties));
        }
    }

    private TimeEntriesAdapter createNewTimeEntriesAdapter(ItemViewProperties viewProperties) {
        TimeEntriesAdapter teAdapter = new TimeEntriesAdapter(this, NumberUtils.getRandomInt(5, 40));
        teAdapter.setParentColor(viewProperties.getItemBgColor());
        return teAdapter;
    }
}
