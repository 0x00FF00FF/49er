package org.rares.miner49er.domain.entries.ui.actions.edit;

import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.entries.ui.actions.add.TimeEntryAddActionListener;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;

public class TimeEntryEditActionListener implements
        ToolbarActionManager.MenuActionListener,
        ActionEnforcer.FragmentResultListener {

    private ActionListenerManager actionManager;

    @Setter
    private TimeEntryAddActionListener addActionListener;

    public TimeEntryEditActionListener(ActionFragment fragment, ActionListenerManager actionManager) {
        this.actionManager = actionManager;
        addActionListener = new TimeEntryAddActionListener(fragment, actionManager);
        fragment.setResultListener(this);
    }

    @Override
    public boolean onToolbarBackPressed() {
        return addActionListener.onToolbarBackPressed();
    }

    @Override
    public void configureCustomActionMenu(ToolbarActionManager.MenuConfig config) {
        addActionListener.configureCustomActionMenu(config);
        config.titleRes = R.string.time_entry_form_header_edit;
    }

    @Override
    public GenericMenuActions getMenuActionsProvider() {
        return addActionListener.getMenuActionsProvider();
    }

    @Override
    public void onFragmentDismiss() {
        actionManager.unregisterActionListener(this);
    }
}