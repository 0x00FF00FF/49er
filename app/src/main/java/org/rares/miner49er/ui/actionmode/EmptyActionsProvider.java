package org.rares.miner49er.ui.actionmode;

public class EmptyActionsProvider implements GenericMenuActions {
    @Override
    public boolean add(int id) {
        return true;
    }

    @Override
    public boolean edit(int id) {
        return true;
    }

    @Override
    public boolean remove(int id) {
        return true;
    }

    @Override
    public boolean details(int id) {
        return true;
    }

    @Override
    public boolean favorite(int id) {
        return true;
    }

    @Override
    public boolean search(int id) {
        return true;
    }

    @Override
    public boolean filter(int id) {
        return true;
    }

    @Override
    public boolean menuAction(int menuActionId, int id) {
        return true;
    }
}
