package org.rares.miner49er.domain.agnostic;

import android.util.Log;

import java.util.Stack;

public interface SelectedEntityManager {
  String TAG = SelectedEntityManager.class.getSimpleName();

  Stack<SelectedEntityProvider> providerStack = new Stack<>();

  default void registerProvider(SelectedEntityProvider provider) {
    if (providerStack.isEmpty() || !providerStack.peek().equals(provider)) {
      providerStack.push(provider);
    }
  }

  default void deregisterProvider(SelectedEntityProvider provider) {
    if(!providerStack.peek().equals(provider)){
      Log.w(TAG, "deregisterProvider: SOMEBODY ACCIDENTALLY THE STACK!" );
    }
    providerStack.pop();
  }

  default SelectedEntityProvider getSelectedEntityProvider() {
    return providerStack.empty() ? null : providerStack.peek();
  }
}
