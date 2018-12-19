package org.rares.miner49er.ui.actionmode;

public interface ActionEnforcer {



    FragmentResultListener getResultListener();
    void setResultListener(FragmentResultListener listener);

    interface FragmentResultListener {
        void onFragmentDismiss();
//        void onFragmentSuccess();
    }
}
