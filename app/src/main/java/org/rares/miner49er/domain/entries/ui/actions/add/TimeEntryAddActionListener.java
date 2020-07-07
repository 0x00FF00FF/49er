package org.rares.miner49er.domain.entries.ui.actions.add;

import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.EmptyActionsProvider;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuActionListener;

import java.lang.ref.WeakReference;

/**
 * When an action fragment is present
 * it usually involves action mode being set on the toolbar.
 * Besides the action menu being changed, the 'back' button
 * appears on the toolbar.
 * This class takes care of bidirectional handling of events.
 * When 'back' or 'save' is sent from the toolbar, the fragment
 * must react. When the fragment is closed, the toolbar must
 * be informed.
 */
public class TimeEntryAddActionListener
        implements
        ToolbarActionManager.MenuActionListener,
        ActionEnforcer.FragmentResultListener {

    private static final String TAG = TimeEntryAddActionListener.class.getSimpleName();

    private WeakReference<ActionEnforcer> fragment;
    private WeakReference<ActionListenerManager> actionManager;

    @Getter
    @Setter
    private long menuActionEntityId;

    public TimeEntryAddActionListener(ActionEnforcer fragment, ActionListenerManager actionManager) {
        this.fragment = new WeakReference<>(fragment);
        this.actionManager = new WeakReference<>(actionManager);
        this.fragment.get().setResultListener(this);
    }

    @Override
    public boolean onToolbarBackPressed() {
        if (fragment != null && fragment.get() != null) {
            fragment.get().prepareExit();
        }
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
            if (fragment != null && fragment.get() != null) {
                fragment.get().applyAction();
                return true;
            }
            return false;
        }
    };

    @Override
    public void onFragmentDismiss() {
        if (actionManager != null && actionManager.get() != null) {
            actionManager.get().unregisterActionListener(this);
            actionManager.clear();
            actionManager = null;
        }

        if (fragment != null && fragment.get() != null) {
            fragment.get().setResultListener(null);
            fragment.clear();
            fragment = null;
        }
    }
}
