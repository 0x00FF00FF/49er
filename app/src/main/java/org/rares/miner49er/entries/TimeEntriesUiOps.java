package org.rares.miner49er.entries;

import android.app.Activity;
import android.util.Log;

import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
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

    public TimeEntriesUiOps(Activity activity) {
        super(activity);
        setMaxElevation(BaseInterfaces.MAX_ELEVATION_TIME_ENTRIES);
    }

    @Override
    public void onListItemClick(ItemViewProperties itemViewProperties) {
        TimeEntryViewProperties tvp = (TimeEntryViewProperties) itemViewProperties;
        Log.d(TAG, "onListItemClick: [[ TIME ENTRY ]] :::: " + tvp.getText());
    }

    @Override
    public void onParentSelected(ItemViewProperties viewProperties, boolean enlarge) {
        getRv().setAdapter(createNewTimeEntriesAdapter(viewProperties));
        resizeItems(getLastSelectedId());
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
