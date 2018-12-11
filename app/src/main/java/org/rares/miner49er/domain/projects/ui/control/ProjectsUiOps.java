package org.rares.miner49er.domain.projects.ui.control;

import android.util.Log;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.functions.Consumer;
import lombok.Setter;
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
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_NAME;


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

    private boolean requireActionMode = false;

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
        boolean enlarge = super.onListItemClick(holder);

        if (toolbarManager == null) {
            provideToolbarActionManager();
        }

        if (enlarge) {
            requireActionMode = false;
            toolbarManager.unregisterActionListener(this);
        } else {
            requireActionMode = true;
            toolbarManager.registerActionListener(this);
        }

        return enlarge;
    }

    @Override
    public boolean onToolbarBackPressed() {
        ResizeableItemViewHolder vh = getSelectedViewHolder();
        if (vh != null) {
            onListItemClick(vh);
        }
        return vh == null; // toolbarManager should not unregister this component if there is something selected
    }

    @Override
    public void configureCustomActionMenu(MenuConfig config) {

        ResizeableItemViewHolder selectedHolder = getSelectedViewHolder();

        config.menuId = 0;      // set this to 0 to end action mode when add project menu has ended.
        config.requireActionMode = requireActionMode;

        if (selectedHolder != null) {
            config.menuId = R.menu.menu_generic_actions;
            config.additionalMenuId = R.menu.menu_additional_projects;
            config.additionalResources = new int[1][4];
            config.createGenericMenu = true;
            config.titleRes = 0;
            config.subtitleRes = 0;

            config.overrideGenericMenuResources = new int[1][4];
            config.overrideGenericMenuResources[0][ITEM_ID] = R.id.action_add;
            config.overrideGenericMenuResources[0][ICON_ID] = R.drawable.icon_path_add;
            config.overrideGenericMenuResources[0][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
            config.overrideGenericMenuResources[0][ITEM_NAME] = R.string.action_add_issue;

            config.additionalResources[0][ITEM_ID] = R.id.action_add_user;
            config.additionalResources[0][ICON_ID] = R.drawable.icon_path_add_user;
            config.additionalResources[0][FLAGS] = MenuItem.SHOW_AS_ACTION_NEVER;
            config.additionalResources[0][ITEM_NAME] = 0;
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
        toolbarManager.registerActionListener(this);
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

}