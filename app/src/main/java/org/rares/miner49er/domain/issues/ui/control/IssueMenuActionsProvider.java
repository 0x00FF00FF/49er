package org.rares.miner49er.domain.issues.ui.control;

import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;

public class IssueMenuActionsProvider implements GenericMenuActions {
    @Override
    public boolean add(long id) {

        // show add issue fragment
        return false;
    }

    @Override
    public boolean edit(long id) {
        return false;
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
}
