package org.rares.miner49er.domain.issues.ui.control;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.entries.ui.actions.add.TimeEntryAddActionListener;
import org.rares.miner49er.domain.entries.ui.actions.add.TimeEntryAddFormFragment;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.ui.actions.details.IssueDetailsActionListener;
import org.rares.miner49er.domain.issues.ui.actions.details.IssueDetailsFragment;
import org.rares.miner49er.domain.issues.ui.actions.edit.IssueEditActionListener;
import org.rares.miner49er.domain.issues.ui.actions.edit.IssueEditFormFragment;
import org.rares.miner49er.domain.issues.ui.actions.remove.IssueRemoveAction;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.fragments.YesNoDialogFragment;

import static org.rares.miner49er.domain.issues.IssuesInterfaces.KEY_ISSUE_ID;

public class IssueMenuActionsProvider implements GenericMenuActions {

    private static final String TAG = IssueMenuActionsProvider.class.getSimpleName();
    private FragmentManager fragmentManager;
    private ActionListenerManager actionManager;
    private IssueRemoveAction issueRemoveAction;

    public IssueMenuActionsProvider(FragmentManager fragmentManager, ActionListenerManager actionManager, IssueRemoveAction issueRemoveAction) {
        this.fragmentManager = fragmentManager;
        this.actionManager = actionManager;
        this.issueRemoveAction = issueRemoveAction;
    }

    @Override
    public boolean add(long id) {
        // show add time entry fragment
        ActionFragment timeEntryAddFormFragment = new TimeEntryAddFormFragment();
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putLong(KEY_ISSUE_ID, id);
        timeEntryAddFormFragment.setArguments(fragmentArgs);

        TimeEntryAddActionListener timeEntryAddActionListener = new TimeEntryAddActionListener(timeEntryAddFormFragment, actionManager);

        showFragment(timeEntryAddFormFragment);

        actionManager.registerActionListener(timeEntryAddActionListener);

        return true;
    }

    @Override
    public boolean edit(long id) {
        ActionFragment issueEditFormFragment = IssueEditFormFragment.newInstance();
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putLong(KEY_ISSUE_ID, id);
        issueEditFormFragment.setArguments(fragmentArgs);

        IssueEditActionListener issueEditActionListener = new IssueEditActionListener(issueEditFormFragment, actionManager);

        showFragment(issueEditFormFragment);

        actionManager.registerActionListener(issueEditActionListener);

        return true;
    }

    @Override
    public boolean remove(long id) {
        String issueName = InMemoryCacheAdapterFactory.ofType(IssueData.class).get(id, true).blockingGet().get().getName();
        YesNoDialogFragment removeYnDialog =
                YesNoDialogFragment.newInstance(issueName, R.string.question_delete_issue, R.string.details_question_delete_issue);

        issueRemoveAction.setIssueId(id);

        removeYnDialog.setListener(issueRemoveAction);
        removeYnDialog.show(fragmentManager, YesNoDialogFragment.TAG);
        return true;
    }

    @Override
    public boolean details(long id) {
        ActionFragment issueDetailsFragment = IssueDetailsFragment.newInstance();
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putLong(KEY_ISSUE_ID, id);
        issueDetailsFragment.setArguments(fragmentArgs);

        IssueDetailsActionListener issueDetailsActionListener = new IssueDetailsActionListener(issueDetailsFragment, actionManager);

        showFragment(issueDetailsFragment);

        actionManager.registerActionListener(issueDetailsActionListener);

        return true;
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

        fragmentManager
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.item_animation_from_left, R.anim.item_animation_to_left)
                .replace(R.id.main_container, fragment, tag)
                .show(fragment)
                .commit();
    }
}
