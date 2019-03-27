package org.rares.miner49er.domain.projects.ui.actions.add;

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

public class ProjectAddActionListener
        implements
        ToolbarActionManager.MenuActionListener,
        ActionEnforcer.FragmentResultListener {

    private static final String TAG = ProjectAddActionListener.class.getSimpleName();

    private ActionFragment fragment;
    private ActionListenerManager actionManager;

    @Getter
    @Setter
    private long menuActionEntityId;

    public ProjectAddActionListener(ActionFragment fragment, ActionListenerManager actionManager) {
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
        config.titleRes = R.string.project_form_header_add;
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
