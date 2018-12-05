package org.rares.miner49er.domain.issues.ui.control;

import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;

public class IssueMenuActionsProvider implements GenericMenuActions {
    @Override
    public boolean add(int id) {

        // show add issue fragment
        return false;
    }

    @Override
    public boolean edit(int id) {
        return false;
    }

    @Override
    public boolean remove(int id) {
        return false;
    }

    @Override
    public boolean details(int id) {
        return false;
    }

    @Override
    public boolean favorite(int id) {
        return false;
    }

    @Override
    public boolean search(int id) {
        return false;
    }

    @Override
    public boolean filter(int id) {
        return false;
    }

    @Override
    public boolean menuAction(int menuActionId, int id) {

        if (menuActionId == R.id.action_add_user) {
            // show add user fragment
        }
        return false;
    }
}
