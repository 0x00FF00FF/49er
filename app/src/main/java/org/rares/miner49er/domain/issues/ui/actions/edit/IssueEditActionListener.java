package org.rares.miner49er.domain.issues.ui.actions.edit;

import org.rares.miner49er.domain.issues.ui.actions.add.IssueAddActionListener;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;

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
    public GenericMenuActions getMenuActionsProvider() {
        return addActionListener.getMenuActionsProvider();
    }
}
