package org.rares.miner49er.fakes;

import org.rares.miner49er.ui.actionmode.ActionEnforcer;

public class ActionEnforcerFake implements ActionEnforcer {

        public boolean validateFormCalled = false;
        public boolean applyActionCalled = false;
        public boolean getActionTagCalled = false;
        public boolean prepareEntryCalled = false;
        public boolean prepareExitCalled = false;
        public boolean getResultListenerCalled = false;
        public boolean setResultListenerCalled = false;
        public boolean onFragmentDismissCalled = false;    //


        public boolean validateFormReturn;
        public String actionTag = "actionTag";
        public FragmentResultListener resultListener = () -> onFragmentDismissCalled = true;

        @Override
        public boolean validateForm() {
            validateFormCalled = true;
            return validateFormReturn;
        }

        @Override
        public void applyAction() {
            applyActionCalled = true;
        }

        @Override
        public String getActionTag() {
            getActionTagCalled = true;
            return actionTag;
        }

        @Override
        public void prepareEntry() {
            prepareEntryCalled = true;
        }

        @Override
        public void prepareExit() {
            prepareExitCalled = true;
        }

        @Override
        public FragmentResultListener getResultListener() {
            getResultListenerCalled = true;
            return resultListener;
        }

        @Override
        public void setResultListener(FragmentResultListener listener) {
            setResultListenerCalled = true;
            resultListener = listener;
        }
    }