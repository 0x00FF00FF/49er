package org.rares.miner49er.domain.issues.ui.actions.add;

import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.ActionEnforcer;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.EmptyActionsProvider;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuActionListener;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig;

import java.lang.ref.WeakReference;

public class IssueAddActionListener
        implements
        ToolbarActionManager.MenuActionListener,
        ActionEnforcer.FragmentResultListener {
    private static final String TAG = IssueAddActionListener.class.getSimpleName();

    private WeakReference<ActionFragment> fragment;
    private WeakReference<ActionListenerManager> actionManager;

    @Getter @Setter
    private long menuActionEntityId;

    public IssueAddActionListener(ActionFragment fragment, ActionListenerManager actionManager) {
        this.fragment = new WeakReference<>(fragment);
        this.actionManager = new WeakReference<>(actionManager);
        this.fragment.get().setResultListener(this);
    }

    @Override
    public void onFragmentDismiss() {
        actionManager.get().unregisterActionListener(this);

        fragment.clear();
        actionManager.clear();
        fragment = null;
        actionManager = null;
    }

    @Override
    public boolean onToolbarBackPressed() {
        fragment.get().prepareExit();
        return false; // toolbar manager should not unregister this listener, as it unregisters itself
    }

    @Override
    public void configureCustomActionMenu(MenuConfig config) {
        MenuActionListener.super.configureCustomActionMenu(config);
        config.titleRes = R.string.issue_form_header_add;
    }

    @Override
    public GenericMenuActions getMenuActionsProvider() {
        // need access to fragment
        return actionsProvider;
    }

    private GenericMenuActions actionsProvider = new EmptyActionsProvider() {
        @Override
        public boolean add(long id) {
            fragment.get().applyAction();
            return true;
        }
    };
}
