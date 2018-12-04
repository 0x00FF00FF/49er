package org.rares.miner49er.domain.projects.ui.actions.edit;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.EmptyActionsProvider;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;

public class ProjectAddActionListener implements ToolbarActionManager.MenuActionListener {

    private static final String TAG = ProjectAddActionListener.class.getSimpleName();

    private ProjectEditFormFragment fragment;

    public ProjectAddActionListener(ProjectEditFormFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public boolean onToolbarBackPressed() {
        // get fragment manager and handle back pressed
        ((AppCompatActivity) fragment.getContext()).getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void configureCustomActionMenu(ToolbarActionManager.MenuConfig config) {
        // need context to get string resource
        config.titleRes = R.string.project_form_header;
        config.subtitle = "";
        config.menuId = R.menu.menu_action_done;
        config.createGenericMenu = false;
        config.menuResources = null;
        config.additionalResources = null;
        config.additionalMenuId = 0;
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
            if (fragment.validate()) {
                return fragment.addProject();
            }
            return false;
        }
    };
}
