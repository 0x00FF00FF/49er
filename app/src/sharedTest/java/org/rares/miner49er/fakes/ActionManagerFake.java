package org.rares.miner49er.fakes;

import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuActionListener;

public class ActionManagerFake implements ActionListenerManager {

        public MenuActionListener listener;

        @Override
        public void registerActionListener(MenuActionListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean unregisterActionListener(MenuActionListener listener) {
            if (this.listener.equals(listener)) {
                this.listener = null;
                return true;
            } else {
                return false;
            }
        }
    }