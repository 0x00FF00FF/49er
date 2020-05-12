package org.rares.miner49er.domain.entries.ui.control;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.cache.optimizer.DataUpdater;
import org.rares.miner49er.domain.agnostic.SelectedEntityProvider;
import org.rares.miner49er.domain.agnostic.TouchHelperCallback;
import org.rares.miner49er.domain.entries.adapter.TimeEntriesAdapter;
import org.rares.miner49er.domain.entries.adapter.TimeEntriesViewHolder;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.entries.persistence.TimeEntriesRepository;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.reactivestreams.Subscriber;

/**
 * @author rares
 * @since 14.03.2018
 */

public class TimeEntriesUiOps extends ResizeableItemsUiOps
        implements
        DomainLink {

    public static final String TAG = TimeEntriesUiOps.class.getSimpleName();

    private TimeEntriesRepository teRepository;

    private ToolbarActionManager toolbarManager = null;

    private TimeEntryMenuActionsProvider menuActionsProvider;

    private TouchHelperCallback<TimeEntriesViewHolder, TimeEntryData> touchHelperCallback = new TouchHelperCallback<>();
    private ItemTouchHelper itemTouchHelper;

    public TimeEntriesUiOps(RecyclerView rv, DataUpdater networkDataUpdater, Subscriber<String> networkProgressListener) {

        this.networkDataUpdater = networkDataUpdater;
        this.networkProgressListener = networkProgressListener;

        teRepository = new TimeEntriesRepository(networkDataUpdater, networkProgressListener);
        teRepository.setup();
        repository = teRepository;

        itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(getRv());
        touchHelperCallback.setDao(InMemoryCacheAdapterFactory.ofType(TimeEntryData.class));

        setRv(rv);
    }

    @Override
    public boolean onListItemClick(ResizeableItemViewHolder holder) {
        TimeEntriesAdapter adapter = (TimeEntriesAdapter) getRv().getAdapter();
        String text = adapter.getData(getRv().getChildAdapterPosition(holder.itemView));
        Log.d(TAG, "onListItemClick: [[ TIME ENTRY ]] :::: " + text);
        menuActionsProvider.edit(holder.getItemProperties().getId());
        return true;
    }

    @Override
    public void onParentChanged(ItemViewProperties itemViewProperties) {
        teRepository.setParentProperties(itemViewProperties);
        teRepository.refreshData();

//        Log.v(TAG, "onParentChanged: " + unbinderList.size());
//        for (Unbinder unbinder : unbinderList) {
//            RecyclerView.ViewHolder vh = (RecyclerView.ViewHolder) unbinder;
//            Log.v(TAG, "onParentChanged: " + unbinder + "" + TextUtils.getItemText(vh.itemView));
//        }
    }

    @Override
    protected void configureMenuActionsProvider(FragmentManager fm) {
        if (toolbarManager == null) {
            provideToolbarActionManager();
        }
        if (menuActionsProvider == null) {
            menuActionsProvider = new TimeEntryMenuActionsProvider(fragmentManager, toolbarManager);
        }
    }

    private void provideToolbarActionManager() {
        // TODO: 12/4/18 have the toolbar supplied, do not "grab"
        Toolbar t = ((AppCompatActivity) getRv().getContext()).findViewById(R.id.toolbar_c);

        if (t.getTag(R.integer.tag_toolbar_action_manager) == null) {
            toolbarManager = new ToolbarActionManager(t);
            t.setTag(R.integer.tag_toolbar_action_manager, toolbarManager);
        } else {
            toolbarManager = (ToolbarActionManager) t.getTag(R.integer.tag_toolbar_action_manager);
        }
    }

    @Override
    public void onParentSelected(ItemViewProperties viewProperties, boolean parentWasEnlarged) {

        AbstractAdapter adapter = (AbstractAdapter) getRv().getAdapter();

        if (parentWasEnlarged) {
//            if (unbinderList.size() > 40) {
                // + clear the viewHolders if
                // they reach a certain number;
                // so there are no leaks

//                repository.shutdown();
//                getRv().setAdapter(null);
//                touchHelperCallback.setAdapter(null);
//                itemTouchHelper.attachToRecyclerView(null);
//                resetRv();
//            } else if (adapter != null) {
//                adapter.clearData();
//            }
        } else {
//            if (adapter != null) {
//                onParentChanged(viewProperties);
//            } else {
              if (adapter == null) {
                getRv().setAdapter(createNewAdapter(viewProperties));
              } else {
                teRepository.setParentProperties(viewProperties);
                teRepository.refreshData();
              }
//                itemTouchHelper.attachToRecyclerView(getRv());
//                repository.refreshData(true);
//            }
        }
    }

    @Override
    public void onParentRemoved(ItemViewProperties viewProperties) {
        if (viewProperties != null) {
            getRv().setAdapter(createNewAdapter(viewProperties));
            itemTouchHelper.attachToRecyclerView(getRv());
        }
    }

    @Override
    protected AbstractAdapter createNewAdapter(ItemViewProperties viewProperties) {

        Log.i(TAG, "createNewAdapter: " + viewProperties.toString());

        TimeEntriesAdapter teAdapter = (TimeEntriesAdapter) getRv().getAdapter();
        if (teAdapter == null) {
            teAdapter = new TimeEntriesAdapter(this);
            teRepository.registerSubscriber(teAdapter, () -> repository.refreshData());
            touchHelperCallback.setAdapter(teAdapter);
        }

//        teAdapter.setUnbinderHost(this);

        teRepository.setup();
        teRepository.setParentProperties(viewProperties);
        teRepository.refreshData();


        return teAdapter;
    }

    @Override
    public int getEntityType() {
        return SelectedEntityProvider.ET_TIME_ENTRY;
    }

    @Override
    public void updateEntity() {
        AbstractViewModel vmData = getSelectedEntity();
        if (vmData != null) {
            networkDataUpdater.updateTimeEntry(vmData.objectId, vmData.parentId, networkProgressListener);
        }
    }
}
