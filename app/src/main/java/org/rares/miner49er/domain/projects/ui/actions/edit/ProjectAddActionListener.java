package org.rares.miner49er.domain.projects.ui.actions.edit;

import android.util.Log;
import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.ActionFragment;
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
        ActionFragment.FragmentDismissListener {

    private static final String TAG = ProjectAddActionListener.class.getSimpleName();

    private ActionFragment fragment;
    private ToolbarActionManager toolbarActionManager;

    public ProjectAddActionListener(ProjectEditFormFragment fragment, ToolbarActionManager toolbarActionManager) {
        this.fragment = fragment;
        this.toolbarActionManager = toolbarActionManager;
        this.fragment.setDismissListener(this);
    }

    @Override
    public boolean onToolbarBackPressed() {
        fragment.prepareExit();
        return true; // toolbar manager should unregister this listener
    }

    @Override
    public void configureCustomActionMenu(ToolbarActionManager.MenuConfig config) {
        config.titleRes = R.string.project_form_header;
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
        public boolean add(int id) {
            Log.i(TAG, "add: validate + add [ " + id + "]");
            if (fragment.validateForm()) {
                if (fragment.applyAction()) {
                    fragment.prepareExit();
                    fragment.getFragmentManager().popBackStack();
                    onFragmentDismiss();
                }
            }
            return true;
        }
    };

    @Override
    public void onFragmentDismiss() {
        toolbarActionManager.unregisterActionListener(this);
    }
}
