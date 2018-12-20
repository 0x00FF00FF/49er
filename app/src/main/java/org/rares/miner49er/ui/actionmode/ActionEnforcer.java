package org.rares.miner49er.ui.actionmode;

public interface ActionEnforcer {   // find better name

    boolean validateForm();

    void applyAction();

    String getActionTag();

    void prepareEntry();

    void prepareExit();

    FragmentResultListener getResultListener();
    void setResultListener(FragmentResultListener listener);

    interface FragmentResultListener {
        void onFragmentDismiss();
//        void onFragmentSuccess();
    }
}
