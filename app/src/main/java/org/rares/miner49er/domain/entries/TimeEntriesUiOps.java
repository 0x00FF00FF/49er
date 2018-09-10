package org.rares.miner49er.domain.entries;

import android.util.Log;
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
        implements
        DomainLink {

    public static final String TAG = TimeEntriesUiOps.class.getSimpleName();

    private TimeEntriesRepository teRepository = new TimeEntriesRepository();

    public TimeEntriesUiOps() {
        teRepository.setup();
        repository = teRepository;
    }

    @Override
    public boolean onListItemClick(ResizeableViewHolder holder) {
        TimeEntriesAdapter adapter = (TimeEntriesAdapter) getRv().getAdapter();
        String text = adapter.getData(getRv().getChildAdapterPosition(holder.itemView));
        Log.d(TAG, "onListItemClick: [[ TIME ENTRY ]] :::: " + text);
        return true;
    }

    @Override
    public void onParentChanged(ItemViewProperties itemViewProperties) {
        teRepository.setParentProperties(itemViewProperties);
        teRepository.refreshData(true);

//        Log.v(TAG, "onParentChanged: " + unbinderList.size());
//        for (Unbinder unbinder : unbinderList) {
//            RecyclerView.ViewHolder vh = (RecyclerView.ViewHolder) unbinder;
//            Log.v(TAG, "onParentChanged: " + unbinder + "" + TextUtils.getItemText(vh.itemView));
//        }
    }

    @Override
    public void onParentSelected(ItemViewProperties viewProperties, boolean parentWasEnlarged) {

        AbstractAdapter adapter = (AbstractAdapter) getRv().getAdapter();

        if (parentWasEnlarged) {
            if (unbinderList.size() > 40) {
                // + clear the viewHolders if
                // they reach a certain number;

                // LeakCanary shows views in
                // viewHolders as leaks so
                // they need to be cleared
                // out when possible

                // todo
                // investigate if it's worth
                // extending recyclerView to
                // get information about all
                // viewHolders

                // TODO, important
                // use some mechanism to
                // delay/block/eat excessive
                // user input so that RV will
                // not have to use resources
                // while it is being restarted
                repository.shutdown();
                getRv().setAdapter(null);
                resetRv();
            } else if (adapter != null) {
                adapter.clearData();
            }
        } else {
            if (adapter != null) {
                onParentChanged(viewProperties);
            } else {
                getRv().setAdapter(createNewAdapter(viewProperties));
            }
        }
    }

    @Override
    public void onParentRemoved(ItemViewProperties viewProperties) {
        if (viewProperties != null) {
            getRv().setAdapter(createNewAdapter(viewProperties));
        }
    }

    @Override
    protected AbstractAdapter createNewAdapter(ItemViewProperties viewProperties) {

        Log.i(TAG, "createNewAdapter: " + viewProperties.toString());

        TimeEntriesAdapter teAdapter = new TimeEntriesAdapter(this);
        teAdapter.setUnbinderHost(this);

        teRepository
                .setup()
                .setParentProperties(viewProperties)
                .registerSubscriber(teAdapter);
        return teAdapter;
    }
}
