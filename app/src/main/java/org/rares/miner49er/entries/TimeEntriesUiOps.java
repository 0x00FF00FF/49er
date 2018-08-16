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

    private TimeEntriesRepository teRepository = new TimeEntriesRepository();

    @Override
    public boolean onListItemClick(ResizeableViewHolder holder) {
        Log.d(TAG, "onListItemClick: [[ TIME ENTRY ]] :::: " + (((TextView) ((ViewGroup) holder.itemView).getChildAt(0))).getText());
        return true;
    }

    @Override
    public void onParentSelected(ItemViewProperties viewProperties, boolean enlarge) {
        teRepository.shutdown();

        if (enlarge) {
            getRv().setAdapter(null);
        } else {
            getRv().swapAdapter(createNewTimeEntriesAdapter(viewProperties), true);
        }
    }

    @Override
    public void onParentRemoved(ItemViewProperties viewProperties) {
        if (viewProperties != null) {
            getRv().setAdapter(createNewTimeEntriesAdapter(viewProperties));
        }
    }

    private TimeEntriesAdapter createNewTimeEntriesAdapter(ItemViewProperties viewProperties) {

        Log.i(TAG, "createNewTimeEntriesAdapter: " + viewProperties.toString());

        TimeEntriesAdapter teAdapter = new TimeEntriesAdapter(this);
        teAdapter.setParentColor(viewProperties.getItemBgColor());
        teRepository.setup();
        teRepository.setParentId(viewProperties.getId());
        teRepository.registerSubscriber(teAdapter);
        return teAdapter;
    }
}
