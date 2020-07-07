package org.rares.miner49er.domain.agnostic;

import android.util.Log;
import lombok.Getter;

import java.util.Stack;

@Deprecated
public class SelectedEntityManager {
  private String TAG = SelectedEntityManager.class.getSimpleName();

  @Getter
  private Stack<SelectedEntityProvider> providerStack = new Stack<>();

  public void registerProvider(SelectedEntityProvider provider) {
    if (providerStack.isEmpty() || !providerStack.peek().equals(provider)) {
      providerStack.push(provider);
    }
  }

  public void deregisterProvider(SelectedEntityProvider provider) {
    if (!providerStack.peek().equals(provider)) {
//      dumpStack();
      Log.w(TAG, "deregisterProvider: SOMEBODY ACCIDENTALLY THE STACK!");
    }
    providerStack.pop();
  }

  public SelectedEntityProvider getSelectedEntityProvider() {
    return providerStack.empty() ? null : providerStack.peek();
  }

  public void dumpStack(){
    Log.e(TAG, "dumpStack: ---------------dump " + hashCode());
    for (SelectedEntityProvider selectedEntityProvider : providerStack) {
      Log.e(TAG, "dumpStack: \t" + selectedEntityProvider.toString());
    }
    Log.e(TAG, "dumpStack: ---------------dump");
  }
}
