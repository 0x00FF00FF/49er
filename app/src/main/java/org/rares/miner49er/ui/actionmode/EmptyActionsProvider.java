package org.rares.miner49er.ui.actionmode;

public class EmptyActionsProvider implements GenericMenuActions {
    @Override
    public boolean add(long id) {
        return true;
    }

    @Override
    public boolean edit(long id) {
        return true;
    }

    @Override
    public boolean remove(long id) {
        return true;
    }

    @Override
    public boolean details(long id) {
        return true;
    }

    @Override
    public boolean favorite(long id) {
        return true;
    }

    @Override
    public boolean search(long id) {
        return true;
    }

    @Override
    public boolean filter(long id) {
        return true;
    }

    @Override
    public boolean menuAction(int menuActionId, long id) {
        return true;
    }
}
