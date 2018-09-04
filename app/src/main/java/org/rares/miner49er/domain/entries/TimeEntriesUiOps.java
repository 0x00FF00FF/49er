package org.rares.miner49er.domain.entries;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er._abstract.ResizeableViewHolder;
import org.rares.miner49er.domain.entries.adapter.TimeEntriesAdapter;

/**
 * @author rares
 * @since 14.03.2018
 */

public class TimeEntriesUiOps extends ResizeableItemsUiOps
        implements DomainLink {

    public static final String TAG = TimeEntriesUiOps.class.getSimpleName();

    private TimeEntriesRepository teRepository = new TimeEntriesRepository();

    public TimeEntriesUiOps() {
        teRepository.setup();
        repository = teRepository;
    }

    @Override
    public boolean onListItemClick(ResizeableViewHolder holder) {
        Log.d(TAG, "onListItemClick: [[ TIME ENTRY ]] :::: " + (((TextView) ((ViewGroup) holder.itemView).getChildAt(0))).getText());
        return true;
    }

    @Override
    public void onParentChanged(ItemViewProperties itemViewProperties) {
        teRepository.setParentProperties(itemViewProperties);
        teRepository.refreshData(true);
    }

    @Override
    public void onParentSelected(ItemViewProperties viewProperties, boolean parentWasEnlarged) {

        AbstractAdapter adapter = (AbstractAdapter) getRv().getAdapter();

        if (parentWasEnlarged) {
            if (adapter != null) {
                adapter.clearData();
            }
        } else {
            if (adapter != null) {
                onParentChanged(viewProperties);
            } else {
                getRv().swapAdapter(createNewTimeEntriesAdapter(viewProperties), true);
            }
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
//        teAdapter.setParentColor(viewProperties.getItemBgColor());
        teRepository
                .setup()
                .setParentProperties(viewProperties)
                .registerSubscriber(teAdapter);
        return teAdapter;
    }
}
