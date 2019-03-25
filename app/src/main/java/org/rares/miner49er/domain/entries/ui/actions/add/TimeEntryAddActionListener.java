package org.rares.miner49er.domain.entries.ui.actions.add;

import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.EmptyActionsProvider;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuActionListener;

public class TimeEntryAddActionListener
        implements
        ToolbarActionManager.MenuActionListener,
        ActionEnforcer.FragmentResultListener {

    private static final String TAG = TimeEntryAddActionListener.class.getSimpleName();

    private ActionFragment fragment;
    private ActionListenerManager actionManager;

    public TimeEntryAddActionListener(ActionFragment fragment, ActionListenerManager actionManager) {
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
        MenuActionListener.super.configureCustomActionMenu(config);
        config.titleRes = R.string.time_entry_form_header_add;
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
