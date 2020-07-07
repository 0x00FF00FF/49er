package org.rares.miner49er.domain.entries.ui.actions.edit;

import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.entries.ui.actions.add.TimeEntryAddActionListener;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuActionListener;

import java.lang.ref.WeakReference;

public class TimeEntryEditActionListener implements
        ToolbarActionManager.MenuActionListener,
        ActionEnforcer.FragmentResultListener {

    private WeakReference<ActionListenerManager> actionManager;
    private WeakReference<ActionEnforcer> fragment;

    @Setter
    private TimeEntryAddActionListener addActionListener;

    public TimeEntryEditActionListener(ActionEnforcer fragment, ActionListenerManager actionManager) {
        this.actionManager = new WeakReference<>(actionManager);
        this.fragment = new WeakReference<>(fragment);
        addActionListener = new TimeEntryAddActionListener(fragment, null);
        fragment.setResultListener(this);   // override what is set by addActionListener
    }

    @Override
    public boolean onToolbarBackPressed() {
        if (addActionListener != null) {
            return addActionListener.onToolbarBackPressed();
        }
        return false;
    }

    @Override
    public void configureCustomActionMenu(ToolbarActionManager.MenuConfig config) {
        MenuActionListener.super.configureCustomActionMenu(config);
        config.titleRes = R.string.time_entry_form_header_edit;
    }

    @Override
    public GenericMenuActions getMenuActionsProvider() {
        if (addActionListener != null) {
            return addActionListener.getMenuActionsProvider();
        }
        return null;
    }

    @Override
    public void onFragmentDismiss() {
        if (actionManager != null && actionManager.get() != null) {
            actionManager.get().unregisterActionListener(this);
            actionManager.clear();
            actionManager = null;
        }
        if (addActionListener != null) {
            addActionListener.onFragmentDismiss();  // also clear addActionListener references
            addActionListener = null;
        }
        if (fragment != null && fragment.get() != null) {
            fragment.get().setResultListener(null);
            // ^ the fragment's result listener is cleared
            // in the addActionListener.onFragmentDismiss()
            // but only if the addActionListener is not null
            fragment.clear();
            fragment = null;
        }
    }

    @Override
    public long getMenuActionEntityId() {
        if (addActionListener != null) {
            return addActionListener.getMenuActionEntityId();
        }
        return -1L;
    }

    @Override
    public void setMenuActionEntityId(long entityId) {
        if (addActionListener != null) {
            addActionListener.setMenuActionEntityId(entityId);
        }
    }
}