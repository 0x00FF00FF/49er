package org.rares.miner49er.domain.projects.ui.actions.add;

import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.EmptyActionsProvider;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;

import static androidx.core.internal.view.SupportMenuItem.SHOW_AS_ACTION_ALWAYS;
import static androidx.core.internal.view.SupportMenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.FLAGS;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ICON_ID;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_ID;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_NAME;

public class ProjectAddActionListener
        implements
        ToolbarActionManager.MenuActionListener,
        ActionEnforcer.FragmentResultListener {

    private static final String TAG = ProjectAddActionListener.class.getSimpleName();

    private ActionFragment fragment;
    private ActionListenerManager actionManager;

    public ProjectAddActionListener(ActionFragment fragment, ActionListenerManager actionManager) {
        this.fragment = fragment;
        this.actionManager = actionManager;
        this.fragment.setResultListener(this);
    }

    @Override
    public boolean onToolbarBackPressed() {
        fragment.prepareExit();
        return false; // toolbar manager should not unregister this listener, as it unregisters itself
    }

    @Override
    public void configureCustomActionMenu(ToolbarActionManager.MenuConfig config) {
        config.titleRes = R.string.project_form_header_add;
        config.subtitle = "";
        config.createGenericMenu = false;
        config.menuId = R.menu.menu_action_done;
        config.overrideGenericMenuResources = null;

        config.menuResources = new int[1][4];
        config.menuResources[0][ITEM_ID] = R.id.action_add;
        config.menuResources[0][ICON_ID] = 0;
        config.menuResources[0][ITEM_NAME] = 0;
        config.menuResources[0][FLAGS] = SHOW_AS_ACTION_WITH_TEXT | SHOW_AS_ACTION_ALWAYS;

        config.additionalMenuId = 0;
        config.additionalResources = null;
        config.requireActionMode = true;
    }

    @Override
    public GenericMenuActions getMenuActionsProvider() {
        // need access to fragment
        return actionsProvider;
    }

    private GenericMenuActions actionsProvider = new EmptyActionsProvider() {
        @Override
        public boolean add(long id) {
            fragment.applyAction();
            return true;
        }
    };

    @Override
    public void onFragmentDismiss() {
        actionManager.unregisterActionListener(this);
    }
}