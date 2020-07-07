package org.rares.miner49er.network;

import androidx.annotation.Nullable;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.CompletableSubject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ObservableNetworkProgress {  // network call tracker
  private static final String TAG = ObservableNetworkProgress.class.getSimpleName();

  public static final String ID_PROJECTS_LIGHT = "id_projects_light";

  private Map<String, UUID> objectUuidIndex = new HashMap<>();
  private Map<UUID, Completable> projectEvents = new HashMap<>();
  private Map<String, Disposable> disposableMap = new HashMap<>();

  /*CompletableSource*/ void addNetworkEvent(String objectId, UUID networkCall, Disposable disposable) {
//    Log.i(TAG, "addNetworkEvent() called with: objectId = [" + objectId + "], networkCall = [" + networkCall + "]");
    UUID existing = objectUuidIndex.get(objectId);
    if (existing != null) {
//      Log.w(TAG, "addNetworkEvent: >> WARNING! << A call for the same object id already exists!");
//      Log.d(TAG, "addNetworkEvent: removing previous call: " + existing + ".");
      removeNetworkEvent(objectId, existing);
      // or just return and reject the call/addition + dispose?
    }
    objectUuidIndex.put(objectId, networkCall);
    Completable callCompletable = CompletableSubject.create();
    projectEvents.put(networkCall, callCompletable);
    disposableMap.put(objectId, disposable);
//    return callCompletable;
  }

  void removeNetworkEvent(String objectId, UUID networkCall) {
//    Log.d(TAG, "removeNetworkEvent() called with: objectId = [" + objectId + "], networkCall = [" + networkCall + "]");
    if (objectId == null) {
      return;
    }
    UUID uuid = objectUuidIndex.get(objectId);
//    if (uuid != networkCall) {
//      Log.w(TAG, "removeNetworkEvent: Supplied uuid different than indexed one.");
//    }
    objectUuidIndex.remove(objectId);
    if (uuid == null) {
      return;
    }
    CompletableSubject cs = (CompletableSubject) projectEvents.get(uuid);
    if (cs != null) {
      if (!cs.hasComplete()) {
//        Log.w(TAG, "removeNetworkEvent: WARNING! Removing an uncompleted event!");
        if (cs.getThrowable() == null) {
          cs.onError(new Throwable("Call did not complete."));
//          cs.onComplete(); // silently complete
        }
      }
      projectEvents.remove(uuid);
    }
    Disposable disposable = disposableMap.get(objectId);
    if (disposable != null) {
//      Log.w(TAG, "removeNetworkEvent: Disposable not null, disposing and removing.");
      disposable.dispose();         /////// is this necessary?
      disposableMap.remove(objectId);
    }
  }

  @Nullable
  public Completable getByUuid(UUID networkCall) {
    CompletableSubject completable = null;
    if (networkCall != null) {
      completable = (CompletableSubject) projectEvents.get(networkCall);
      if (completable != null && completable.hasComplete()) {
        completable = null;
      }
    }
    return completable;
  }

  @Nullable
  public Completable getByObjectId(String objectId) {
    if (objectId == null) {
      return null;
    }
    // i did this because i cannot remove
    // the call/event when the disposable is disposed.
    Disposable disposable = getNetworkUpdateDisposable(objectId);
    if (disposable != null) {
      if (disposable.isDisposed()) {
//        Log.w(TAG, "getByObjectId: Removing network event because network call is disposed.");
        removeNetworkEvent(objectId, objectUuidIndex.get(objectId));
        return null;
      }
    }
    return getByUuid(objectUuidIndex.get(objectId));
  }

  @Nullable
  public Disposable getNetworkUpdateDisposable(String objectId) {
    if (objectId == null) {
      return null;
    }
    return disposableMap.get(objectId);
  }
}
