package org.rares.miner49er.domain.issues.ui.control;

import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.agnostic.SelectedEntityProvider;
import org.rares.miner49er.domain.agnostic.TouchHelperCallback;
import org.rares.miner49er.domain.agnostic.TouchHelperCallback.SwipeDeletedListener;
import org.rares.miner49er.domain.issues.adapter.IssuesAdapter;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.persistence.IssuesRepository;
import org.rares.miner49er.domain.issues.ui.actions.remove.IssueRemoveAction;
import org.rares.miner49er.domain.issues.ui.viewholder.IssuesViewHolder;
import org.rares.miner49er.network.DataUpdater;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.util.PermissionsUtil;
import org.rares.miner49er.viewmodel.HierarchyViewModel;

import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ENABLED;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.FLAGS;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ICON_ID;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_ID;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_NAME;

/**
 * @author rares
 * @since 01.03.2018
 */

public class IssuesUiOps extends ResizeableItemsUiOps
        implements
        ToolbarActionManager.MenuActionListener,
        DomainLink,
        SwipeDeletedListener {

    private static final String TAG = IssuesUiOps.class.getSimpleName();
    private IssuesRepository issuesRepository;

    private ToolbarActionManager toolbarManager = null;

    private IssueMenuActionsProvider menuActionsProvider;
    // this component always requires action mode
    private final boolean requiresActionMode = true;

    @Getter
    @Setter
    private long menuActionEntityId;

    private TouchHelperCallback<IssuesViewHolder, IssueData> touchHelperCallback = new TouchHelperCallback<>();
    private ItemTouchHelper itemTouchHelper;

    private HierarchyViewModel vm;

    public IssuesUiOps(RecyclerView rv, DataUpdater networkDataUpdater) {
        this.networkDataUpdater = networkDataUpdater;

         issuesRepository = new IssuesRepository(networkDataUpdater);

        setRv(rv);
        issuesRepository.setup();
        repository = issuesRepository;

        itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(getRv());
        touchHelperCallback.setDao(InMemoryCacheAdapterFactory.ofType(IssueData.class));
        touchHelperCallback.setDeletedListener(this);

        vm = new ViewModelProvider((ViewModelStoreOwner) rv.getContext()).get(HierarchyViewModel.class);
    }

    @Override
    public boolean onListItemClick(ResizeableItemViewHolder holder) {
        boolean enlarge = super.onListItemClick(holder);

        if (toolbarManager == null) {
            Toolbar t = ((AppCompatActivity) getRv().getContext()).findViewById(R.id.toolbar_c);
            toolbarManager = (ToolbarActionManager) t.getTag(R.integer.tag_toolbar_action_manager);
        }

        if (enlarge) {
            toolbarManager.unregisterActionListener(this);
//            selectedEntityManager.deregisterProvider(this);
            vm.selectedIssueId=-1L;
            vm.selectedTimeEntryId=-1L;
            vm.scrollPositionIssues = 0;
            vm.scrollPositionTimeEntries = 0;
            itemTouchHelper.attachToRecyclerView(getRv());
        } else {
            menuActionEntityId = holder.getItemProperties().getId();
            toolbarManager.registerActionListener(this);
//            selectedEntityManager.registerProvider(this);
            itemTouchHelper.attachToRecyclerView(null);
            vm.selectedIssueId = holder.getItemProperties().getId();
            vm.scrollPositionIssues = ((AbstractAdapter)getRv().getAdapter()).findPositionById(vm.selectedIssueId); // expose this by a function that can be called from the activity
        }

        return enlarge;
    }

    @Override
    public boolean onToolbarBackPressed() {
        IssuesAdapter adapter = (IssuesAdapter) getRv().getAdapter();
        ResizeableItemViewHolder holder =
                (ResizeableItemViewHolder) getRv().findViewHolderForAdapterPosition(adapter.getLastSelectedPosition());
        onListItemClick(holder);
        return false; // toolbarManager should not unregister this component
    }

    @Override
    public void configureCustomActionMenu(ToolbarActionManager.MenuConfig config) {

        config.requireActionMode = requiresActionMode;

        IssuesAdapter adapter = (IssuesAdapter) getRv().getAdapter();

        if (adapter == null || adapter.getLastSelectedPosition() == -1) {
            return;
        }

        IssueData issueData = adapter.getData().get(adapter.getLastSelectedPosition());

        config.overrideGenericMenuResources = new int[3][5];
        config.overrideGenericMenuResources[0][ITEM_ID] = R.id.action_add;
        config.overrideGenericMenuResources[0][ICON_ID] = R.drawable.icon_path_add;
        config.overrideGenericMenuResources[0][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
        config.overrideGenericMenuResources[0][ITEM_NAME] = R.string.action_add_time_entry;
        config.overrideGenericMenuResources[0][ENABLED] = PermissionsUtil.canAddIssue(issueData.parentId) ? 1 : 0;

        config.overrideGenericMenuResources[1][ITEM_ID] = R.id.action_edit;
        config.overrideGenericMenuResources[1][ICON_ID] = R.drawable.icon_path_edit;
        config.overrideGenericMenuResources[1][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
        config.overrideGenericMenuResources[1][ITEM_NAME] = 0;
        config.overrideGenericMenuResources[1][ENABLED] = PermissionsUtil.canEditIssue(issueData) ? 1 : 0;

        config.overrideGenericMenuResources[2][ITEM_ID] = R.id.action_remove;
        config.overrideGenericMenuResources[2][ICON_ID] = R.drawable.icon_path_remove;
        config.overrideGenericMenuResources[2][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
        config.overrideGenericMenuResources[2][ITEM_NAME] = 0;
        config.overrideGenericMenuResources[2][ENABLED] = PermissionsUtil.canRemoveIssue(issueData) ? 1 : 0;

        config.createGenericMenu = true;
        config.additionalMenuId = R.menu.menu_additional_issues;
        config.additionalResources = new int[1][5];

        config.additionalResources[0][ITEM_ID] = R.id.action_set_auto_add_hours;
        config.additionalResources[0][ICON_ID] = R.drawable.icon_path_auto_add_time_2;
        config.additionalResources[0][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
        config.additionalResources[0][ITEM_NAME] = 0;
        config.additionalResources[0][ENABLED] = PermissionsUtil.canAddTimeEntry(issueData) ? 1 : 0;

        config.subtitleRes = 0;
        config.titleRes = 0;

        config.subtitle = issueData.getName();
    }

    @Override
    public GenericMenuActions getMenuActionsProvider() {
        return menuActionsProvider;
    }

    @Override
    protected void configureMenuActionsProvider(FragmentManager fm) {
        if (toolbarManager == null) {
            provideToolbarActionManager();
        }
        if (menuActionsProvider == null) {
            menuActionsProvider = new IssueMenuActionsProvider(fragmentManager, toolbarManager, new IssueRemoveAction(this));
        }
    }

    @Override
    public void onParentSelected(ItemViewProperties projectProperties, boolean parentWasEnlarged) {

        IssuesAdapter issuesAdapter = (IssuesAdapter) getRv().getAdapter();

        // the following (if) block is here to resize issue items
        // after selecting another project while one of the
        // issues was selected
        if (issuesAdapter != null) {
            int selected = issuesAdapter.getLastSelectedPosition();
            if (selected != -1) {
                ResizeableItemViewHolder vh = (ResizeableItemViewHolder)
                        getRv().findViewHolderForAdapterPosition(selected);
                if (vh != null) {
                    onListItemClick(vh);
                }
                resetLastSelectedId();
                issuesAdapter.setPreviouslySelectedPosition(-1);
            }
        }

        if (parentWasEnlarged) {
            // clear the viewHolders if
            // they reach a certain number
            // so that the chance of leaked
            // context or views will be
            // minimal.
//            if (unbinderList.size() > 40) {
//            repository.shutdown();
            if (issuesAdapter != null) {
                issuesAdapter.setLastSelectedPosition(-1);
                issuesAdapter.setPreviouslySelectedPosition(-1);
            }
//            getRv().setAdapter(null);
//            touchHelperCallback.setAdapter(null);
//            itemTouchHelper.attachToRecyclerView(null);
//            resetRv();
//            } else if (issuesAdapter != null) {
//                issuesAdapter.clearData();
//            }
        } else {
            if (issuesAdapter == null) {
                getRv().setAdapter(createNewIssuesAdapter(projectProperties));
            } else {
                issuesRepository.setParentProperties(projectProperties);
                issuesRepository.refreshData();
            }
        }
        getRv().scrollToPosition(0);
        resizeRv(!parentWasEnlarged);
    }

    @Override
    public void onParentChanged(ItemViewProperties itemViewProperties) {
//        RecyclerView.LayoutManager _tempLm = getRv().getLayoutManager();
//        AbstractAdapter adapter = (AbstractAdapter) getRv().getAdapter();
//        int lastSelectedPosition = adapter.getLastSelectedPosition();
//        boolean somethingSelected = lastSelectedPosition > -1;
//        getRv().scrollToPosition(somethingSelected ? lastSelectedPosition : 0);

//        if (_tempLm instanceof ResizeableLayoutManager) {
//            ((ResizeableLayoutManager) _tempLm).resetState(true);
//        }
        // ^this is responsible for
        // * edit issue, apply, go to issues list, repeat a few times until
        //			item decoration is not shown anymore under the previously
        //			selected issue, click that issue -> crash.
        //			- selected view is kept/laid out in stickyLM after expansion
        // /!\ in fact, the selected view is reset and then added again when it shouldn't
        // this is a fix for network data refresh resetting the selected view
        // // FIXME: 25.04.2019 ^^^ for network data refresh + collision detection and fixing
        issuesRepository.setParentProperties(itemViewProperties);
        issuesRepository.refreshData();
    }

    @Override
    public void onParentRemoved(ItemViewProperties projectProperties) {
        if (projectProperties != null) {
            getRv().setAdapter(createNewIssuesAdapter(projectProperties));
        }
        resizeRv(true);
        domainLink.onParentRemoved(projectProperties);
    }

    @Override
    public void onListItemChanged(ItemViewProperties ivp) {
        super.onListItemChanged(ivp);
        toolbarManager.refreshActionMode();
    }

    private IssuesAdapter createNewIssuesAdapter(ItemViewProperties projectViewProperties) {
        IssuesAdapter issuesAdapter = (IssuesAdapter) getRv().getAdapter();

        if (issuesAdapter == null) {
            issuesAdapter = new IssuesAdapter(this);
            issuesRepository.registerSubscriber(issuesAdapter, () -> repository.refreshData());
            touchHelperCallback.setAdapter(issuesAdapter);
        }

        issuesRepository.setup();
        issuesRepository.setParentProperties(projectViewProperties);
        issuesRepository.refreshData();

//        issuesAdapter.setUnbinderHost(this);
        issuesAdapter.setParentColor(projectViewProperties.getItemBgColor());
        return issuesAdapter;
    }

    @Override
    protected AbstractAdapter createNewAdapter(ItemViewProperties itemViewProperties) {
        return createNewIssuesAdapter(itemViewProperties);
    }

    // todo ... i don't like this!
    private void provideToolbarActionManager() {
        // TODO: 12/4/18 have the toolbar supplied, do not "grab"
        Toolbar t = ((AppCompatActivity) getRv().getContext()).findViewById(R.id.toolbar_c);

        if (t.getTag(R.integer.tag_toolbar_action_manager) == null) {
            toolbarManager = new ToolbarActionManager(t);   // tool bar manager in the view model -> set/unset toolbar inside it -> the tbm will only do actions when it has a toolbar associated
            t.setTag(R.integer.tag_toolbar_action_manager, toolbarManager);
        } else {
            toolbarManager = (ToolbarActionManager) t.getTag(R.integer.tag_toolbar_action_manager);
        }
    }

    @Override
    public void onItemDeleted(ViewHolder vh) {
        toolbarManager.refreshActionMode();
    }

    @Override
    public void onItemPseudoDeleted(ViewHolder vh) {
        toolbarManager.refreshActionMode();
    }

    @Override
    public int getEntityType() {
        return SelectedEntityProvider.ET_ISSUE;
    }

    @Override
    public void updateEntity() {
        AbstractViewModel vmData = getSelectedEntity();
        if (vmData != null) {
            networkDataUpdater.fullyUpdateIssue(vmData.objectId, vmData.parentId);
        }
    }
}
