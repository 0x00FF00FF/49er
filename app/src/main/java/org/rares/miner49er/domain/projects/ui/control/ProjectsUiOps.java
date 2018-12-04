package org.rares.miner49er.domain.projects.ui.control;

import android.util.Log;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.functions.Consumer;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.domain.projects.ProjectsInterfaces;
import org.rares.miner49er.domain.projects.repository.ProjectsRepository;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig;

import java.util.List;

import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.FLAGS;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ICON_ID;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_ID;


/**
 * @author rares
 * @since 01.03.2018
 */

public class ProjectsUiOps
        extends ResizeableItemsUiOps
        implements ToolbarActionManager.MenuActionListener {

    @Setter
    private ProjectsInterfaces.ProjectsResizeListener projectsListResizeListener;

    private static final String TAG = ProjectsUiOps.class.getSimpleName();

    private ProjectsRepository projectsRepository;

    private ToolbarActionManager toolbarManager = null;

    private ProjectMenuActionsProvider menuActionsProvider;

    public ProjectsUiOps(RecyclerView rv) {
//        Miner49erApplication.getRefWatcher(activity).watch(this);
        setRv(rv);
        projectsRepository = new ProjectsRepository();
        repository = projectsRepository;

        selectedDrawableRes = R.drawable.transient_semitransparent_rectangle_tr_bl;
    }

    /**
     * Should be called on activity start.
     */
    public void setupRepository() {
        Log.e(TAG, "setupRepository() called");
        projectsRepository
                .setup()
                .registerSubscriber((Consumer<List>) getRv().getAdapter());
    }

    @Override
    public boolean onListItemClick(ResizeableItemViewHolder holder) {
        final ListState state = getRvState();
        boolean enlarge = super.onListItemClick(holder);

        if (toolbarManager == null) {
            provideToolbarActionManager();
        }

        if (enlarge) {
            toolbarManager.endActionMode();
        } else {
            if (state == ListState.LARGE) {
//                toolbarManager.addActionListener(this);
                toolbarManager.startActionMode();
            } else {
                toolbarManager.refreshActionMode();
            }
        }

        return enlarge;
    }

    @Override
    public boolean onToolbarBackPressed() {
        ResizeableItemViewHolder vh = getSelectedViewHolder();
        final ListState beforeState = getRvState();
        if (vh != null) {
            onListItemClick(vh);
        }
        return !beforeState.equals(getRvState());
    }

    @Override
    public void configureCustomActionMenu(MenuConfig config) {

        ResizeableItemViewHolder selectedHolder = getSelectedViewHolder();

        config.menuId = 0;      // set this to 0 to end action mode when add project menu has ended.

        if (selectedHolder != null) {
            config.menuId = R.menu.menu_generic_actions;
            config.additionalMenuId = R.menu.menu_additional_projects;
            config.additionalResources = new int[1][3];
            config.createGenericMenu = true;
            config.titleRes = 0;
            config.subtitleRes = 0;

            config.additionalResources[0][ITEM_ID] = R.id.action_add_user;
            config.additionalResources[0][ICON_ID] = R.drawable.icon_path_add_user;
            config.additionalResources[0][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
            Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_add_user: " + R.drawable.icon_path_add_user);
            config.title = selectedHolder.getLongTitle();
            // refresh infoLabel
            config.subtitle = selectedHolder.getInfoLabelString();
        }
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
            menuActionsProvider = new ProjectMenuActionsProvider(fragmentManager, toolbarManager);
        }
        toolbarManager.addActionListener(this);
    }

    private void provideToolbarActionManager() {
        // TODO: 12/4/18 provide the toolbar
        Toolbar t = ((AppCompatActivity) getRv().getContext()).findViewById(R.id.toolbar_c);
        if (t.getTag(R.integer.tag_toolbar_action_manager) == null) {
            toolbarManager = new ToolbarActionManager(t);
            t.setTag(R.integer.tag_toolbar_action_manager, toolbarManager);
        } else {
            toolbarManager = (ToolbarActionManager) t.getTag(R.integer.tag_toolbar_action_manager);
        }
    }

    /**
     * Should be called on activity stop.
     */
    public void shutdown() {
        projectsRepository.shutdown();
    }

    @Override
    protected AbstractAdapter createNewAdapter(ItemViewProperties itemViewProperties) {
        return null;
    }

    /**
     * Removes a project from the list.
     * Calls {@link DomainLink} to act on the event.
     */
    // TODO: 02.03.2018 refactor
    public void removeItem() {
//        RecyclerView.LayoutManager llm = getRv().getLayoutManager();
//        int itemCount = getRv().getAdapter().getItemCount();
//        if (itemCount > 0) {
//            View child = llm.getChildAt(0);
//
//            ItemViewProperties projectViewProperties =
//                    ((ResizeableViewHolder) getRv().getChildViewHolder(child)).getItemProperties();
//            // ^
//            if (projectViewProperties.getItemContainerCustomId() == getLastSelectedId()) {
//                if (itemCount > 1) {
//                    child = llm.getChildAt(1);
//
//                    projectViewProperties =
//                            ((ResizeableViewHolder) getRv().getChildViewHolder(child)).getItemProperties();
//                    // ^
//                    resizeItems(projectViewProperties.getItemContainerCustomId());
//
//                    domainLink.onParentRemoved(projectViewProperties);
//                    // ^
//                } else {
//                    domainLink.onParentRemoved(null);
//                    resizeAnimated(resizeItems(getLastSelectedId()));
//                }
//            }
//        }
//        ((ProjectsAdapter) getRv().getAdapter()).removeItem();
//        // ^
    }

}
