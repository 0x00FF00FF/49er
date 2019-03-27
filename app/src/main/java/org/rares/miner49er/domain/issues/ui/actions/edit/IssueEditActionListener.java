package org.rares.miner49er.domain.issues.ui.actions.edit;

import org.rares.miner49er.R;
import org.rares.miner49er.domain.issues.ui.actions.add.IssueAddActionListener;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuActionListener;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig;

public class IssueEditActionListener implements
        ToolbarActionManager.MenuActionListener,
        ActionEnforcer.FragmentResultListener {

    private ActionListenerManager actionManager;
    private IssueAddActionListener addActionListener;

    public IssueEditActionListener(ActionFragment fragment, ActionListenerManager actionManager) {
        this.actionManager = actionManager;
        addActionListener = new IssueAddActionListener(fragment, actionManager);
        fragment.setResultListener(this);
    }

    @Override   // why unregister here and register somewhere else?
    public void onFragmentDismiss() {
        actionManager.unregisterActionListener(this);
    }

    @Override
    public boolean onToolbarBackPressed() {
        return addActionListener.onToolbarBackPressed();
    }

    @Override
    public void configureCustomActionMenu(MenuConfig config) {
        MenuActionListener.super.configureCustomActionMenu(config);
        config.titleRes = R.string.issue_form_header_edit;
    }

    @Override
    public GenericMenuActions getMenuActionsProvider() {
        return addActionListener.getMenuActionsProvider();
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
