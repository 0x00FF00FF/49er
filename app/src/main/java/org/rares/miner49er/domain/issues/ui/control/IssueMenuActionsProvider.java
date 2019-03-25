package org.rares.miner49er.domain.issues.ui.control;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.entries.ui.actions.add.TimeEntryAddActionListener;
import org.rares.miner49er.domain.entries.ui.actions.add.TimeEntryAddFormFragment;
import org.rares.miner49er.domain.issues.ui.actions.edit.IssueEditActionListener;
import org.rares.miner49er.domain.issues.ui.actions.edit.IssueEditFormFragment;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;

import static org.rares.miner49er.domain.issues.IssuesInterfaces.KEY_ISSUE_ID;

public class IssueMenuActionsProvider implements GenericMenuActions {

    private static final String TAG = IssueMenuActionsProvider.class.getSimpleName();
    private FragmentManager fragmentManager;
    private ActionListenerManager actionManager;

    private ActionFragment issueEditFormFragment;
    private IssueEditActionListener issueEditActionListener;

    private ActionFragment timeEntryAddFormFragment;
    private TimeEntryAddActionListener timeEntryAddActionListener;

    public IssueMenuActionsProvider(FragmentManager fragmentManager, ActionListenerManager actionManager) {
        this.fragmentManager = fragmentManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean add(long id) {
        // show add time entry fragment
        if (timeEntryAddFormFragment == null) {
            timeEntryAddFormFragment = new TimeEntryAddFormFragment();
        }
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putLong(KEY_ISSUE_ID, id);
        timeEntryAddFormFragment.setArguments(fragmentArgs);

        if (timeEntryAddActionListener == null) {
            timeEntryAddActionListener = new TimeEntryAddActionListener(timeEntryAddFormFragment, actionManager);
        }

        showFragment(timeEntryAddFormFragment);

        actionManager.registerActionListener(timeEntryAddActionListener);

        return true;
    }

    @Override
    public boolean edit(long id) {
        if (issueEditFormFragment == null) {
            issueEditFormFragment = IssueEditFormFragment.newInstance();
        }
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putLong(KEY_ISSUE_ID, id);
        issueEditFormFragment.setArguments(fragmentArgs);

        if (issueEditActionListener == null) {
            issueEditActionListener = new IssueEditActionListener(issueEditFormFragment, actionManager);
        }

        showFragment(issueEditFormFragment);

        actionManager.registerActionListener(issueEditActionListener);

        return true;
    }

    @Override
    public boolean remove(long id) {
        return false;
    }

    @Override
    public boolean details(long id) {
        return false;
    }

    @Override
    public boolean favorite(long id) {
        return false;
    }

    @Override
    public boolean search(long id) {
        return false;
    }

    @Override
    public boolean filter(long id) {
        return false;
    }

    @Override
    public boolean menuAction(int menuActionId, long id) {
        if (menuActionId == R.id.action_add_user) {
            // show add user fragment
        }
        return false;
    }

    private void showFragment(ActionFragment fragment) {
        String tag = fragment.getActionTag();
        if (fragmentManager.findFragmentByTag(tag) == null) {

            fragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.item_animation_from_left, R.anim.item_animation_to_left)
                    .replace(R.id.main_container, fragment, tag)
                    .addToBackStack(tag)
                    .show(fragment)
                    .commit();
        }
    }
}
