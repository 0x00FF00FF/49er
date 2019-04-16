package org.rares.miner49er.domain.entries.ui.control;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.entries.ui.actions.edit.TimeEntryEditActionListener;
import org.rares.miner49er.domain.entries.ui.actions.edit.TimeEntryEditFormFragment;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;

import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.KEY_TIME_ENTRY_ID;

public class TimeEntryMenuActionsProvider implements GenericMenuActions {

    private static final String TAG = TimeEntryMenuActionsProvider.class.getSimpleName();
    private FragmentManager fragmentManager;
    private ActionListenerManager actionManager;

    public TimeEntryMenuActionsProvider(FragmentManager fragmentManager, ActionListenerManager actionManager) {
        this.fragmentManager = fragmentManager;
        this.actionManager = actionManager;
    }

    @Override
    public boolean add(long id) {
        // nothing to add here
        return true;
    }

    @Override
    public boolean edit(long id) {

        ActionFragment timeEntryEditFormFragment = TimeEntryEditFormFragment.newInstance();

        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putLong(KEY_TIME_ENTRY_ID, id);
        timeEntryEditFormFragment.setArguments(fragmentArgs);

        TimeEntryEditActionListener timeEntryEditActionListener = new TimeEntryEditActionListener(timeEntryEditFormFragment, actionManager);

        showFragment(timeEntryEditFormFragment);

        actionManager.registerActionListener(timeEntryEditActionListener);

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

            fragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.item_animation_from_left, R.anim.item_animation_to_left)
                    .replace(R.id.main_container, fragment, tag)
                    .show(fragment)
                    .commit();
    }
}
