package org.rares.miner49er.domain.projects.ui.actions.details;

import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.ui.actions.add.ProjectAddActionListener;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.EmptyActionsProvider;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuActionListener;

import java.lang.ref.WeakReference;

public class ProjectDetailsActionListener implements
        MenuActionListener,
        ActionEnforcer.FragmentResultListener {

    private WeakReference<ActionListenerManager> actionManager;

    @Setter
    private ProjectAddActionListener addActionListener;

    public ProjectDetailsActionListener(ActionFragment fragment, ActionListenerManager actionManager) {
        this.actionManager = new WeakReference<>(actionManager);
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

        config.menuId = 0;
        config.titleRes = R.string.project_form_header_details;
        config.menuResources = null;
    }

    @Override
    public GenericMenuActions getMenuActionsProvider() {
        return new EmptyActionsProvider(){};
    }

    @Override
    public void onFragmentDismiss() {
        actionManager.get().unregisterActionListener(this);

        actionManager.clear();
        actionManager = null;
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
