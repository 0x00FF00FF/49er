package org.rares.miner49er.domain.issues.ui.control;

import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.domain.issues.adapter.IssuesAdapter;
import org.rares.miner49er.domain.issues.repository.IssuesRepository;
import org.rares.miner49er.layoutmanager.ResizeableLayoutManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;

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
        DomainLink {

    private static final String TAG = IssuesUiOps.class.getSimpleName();
    private IssuesRepository issuesRepository = new IssuesRepository();

    private ToolbarActionManager toolbarManager = null;

    // this component always requires action mode
    private final boolean requiresActionMode = true;

    public IssuesUiOps(RecyclerView rv) {
        setRv(rv);
        issuesRepository.setup();
        repository = issuesRepository;
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
        } else {
            toolbarManager.registerActionListener(this);
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
        // for now, re-use the generic one configured in the projects
        ResizeableItemViewHolder holder = getSelectedViewHolder();

        if (holder != null) {
//            config.title = holder.getLongTitle();
//            config.subtitle = holder.getInfoLabelString();
            config.subtitle = holder.getLongTitle();
            config.subtitleRes = 0;
        }

        config.requireActionMode = requiresActionMode;

        config.overrideGenericMenuResources = new int[1][4];
        config.overrideGenericMenuResources[0][ITEM_ID] = R.id.action_add;
        config.overrideGenericMenuResources[0][ICON_ID] = R.drawable.icon_path_add;
        config.overrideGenericMenuResources[0][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
        config.overrideGenericMenuResources[0][ITEM_NAME] = R.string.action_add_time_entry;

        config.createGenericMenu = true;

        config.additionalMenuId = R.menu.menu_additional_issues;
        config.additionalResources = new int[1][4];

        config.additionalResources[0][ITEM_ID] = R.id.action_set_auto_add_hours;
        config.additionalResources[0][ICON_ID] = R.drawable.icon_path_auto_add_time_2;
        config.additionalResources[0][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
        config.additionalResources[0][ITEM_NAME] = 0;
    }

    @Override
    public GenericMenuActions getMenuActionsProvider() {
        return null;
    }

    @Override
    protected void configureMenuActionsProvider(FragmentManager fm) {

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
            if (unbinderList.size() > 40) {
                repository.shutdown();
                getRv().setAdapter(null);
                resetRv();
            } else if (issuesAdapter != null) {
                issuesAdapter.clearData();
            }
        } else {
            if (issuesAdapter != null) {
                onParentChanged(projectProperties);
            } else {
                getRv().setAdapter(createNewIssuesAdapter(projectProperties));
            }
        }
        resizeRv(!parentWasEnlarged);
    }

    @Override
    public void onParentChanged(ItemViewProperties itemViewProperties) {
        RecyclerView.LayoutManager _tempLm = getRv().getLayoutManager();
//        AbstractAdapter adapter = (AbstractAdapter) getRv().getAdapter();
//        int lastSelectedPosition = adapter.getLastSelectedPosition();
//        boolean somethingSelected = lastSelectedPosition > -1;
//        getRv().scrollToPosition(somethingSelected ? lastSelectedPosition : 0);

        if (_tempLm instanceof ResizeableLayoutManager) {
            ((ResizeableLayoutManager) _tempLm).resetState(true);
        }
        issuesRepository.setParentProperties(itemViewProperties);
        issuesRepository.refreshData(true);
    }

    @Override
    public void onParentRemoved(ItemViewProperties projectProperties) {
        if (projectProperties != null) {
            getRv().setAdapter(createNewIssuesAdapter(projectProperties));
        }
        resizeRv(true);
        domainLink.onParentRemoved(projectProperties);
    }

    private IssuesAdapter createNewIssuesAdapter(ItemViewProperties projectViewProperties) {
        IssuesAdapter issuesAdapter = new IssuesAdapter(this);
        issuesAdapter.setUnbinderHost(this);
        issuesAdapter.setParentColor(projectViewProperties.getItemBgColor());
        issuesRepository.setup();
        issuesRepository.setParentProperties(projectViewProperties);
        issuesRepository.registerSubscriber(issuesAdapter);
        return issuesAdapter;
    }

    @Override
    protected AbstractAdapter createNewAdapter(ItemViewProperties itemViewProperties) {
        return createNewIssuesAdapter(itemViewProperties);
    }
}
