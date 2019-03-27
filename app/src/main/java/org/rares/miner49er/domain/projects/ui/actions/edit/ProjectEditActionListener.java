package org.rares.miner49er.domain.projects.ui.actions.edit;

import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.ui.actions.add.ProjectAddActionListener;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuActionListener;


// favor composition over inheritance ?

public class ProjectEditActionListener implements
        ToolbarActionManager.MenuActionListener,
        ActionEnforcer.FragmentResultListener {

    private ActionListenerManager actionManager;

    @Setter
    private ProjectAddActionListener addActionListener;

    public ProjectEditActionListener(ActionFragment fragment, ActionListenerManager actionManager) {
        this.actionManager = actionManager;
        addActionListener = new ProjectAddActionListener(fragment, actionManager);
        fragment.setResultListener(this);
    }

    @Override
    public boolean onToolbarBackPressed() {
        return addActionListener.onToolbarBackPressed();
    }

    @Override
    public void configureCustomActionMenu(ToolbarActionManager.MenuConfig config) {
        MenuActionListener.super.configureCustomActionMenu(config);
        config.titleRes = R.string.project_form_header_edit;
    }

    @Override
    public GenericMenuActions getMenuActionsProvider() {
        return addActionListener.getMenuActionsProvider();
    }

    @Override
    public void onFragmentDismiss() {
        actionManager.unregisterActionListener(this);
    }

    @Override
    public long getMenuActionEntityId() {
        return addActionListener.getMenuActionEntityId();
    }

    @Override
    public void setMenuActionEntityId(long entityId) {
        addActionListener.setMenuActionEntityId(entityId);
    }
}
