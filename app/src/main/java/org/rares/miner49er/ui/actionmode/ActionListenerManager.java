package org.rares.miner49er.ui.actionmode;

import org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuActionListener;

public interface ActionListenerManager {

    void registerActionListener(MenuActionListener listener);

    boolean unregisterActionListener(MenuActionListener listener);
}