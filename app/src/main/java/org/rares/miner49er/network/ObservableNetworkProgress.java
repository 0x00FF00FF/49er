package org.rares.miner49er.network;

import androidx.annotation.Nullable;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.subjects.CompletableSubject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ObservableNetworkProgress {

  public static final String ID_PROJECTS = "id_projects";
  public static final String ID_ISSUES = "id_issues";
  public static final String ID_ENTRIES = "id_entries";

  private Map<String, UUID> objectUuidIndex = new HashMap<>();
  private Map<UUID, Completable> projectEvents = new HashMap<>();

  CompletableSource addNetworkEvent(String objectId, UUID networkCall) {
    objectUuidIndex.put(objectId, networkCall);
    Completable callCompletable = CompletableSubject.create();
    projectEvents.put(networkCall, callCompletable);
    return callCompletable;
  }

  void removeProjectEvent(String objectId, UUID networkCall){
    objectUuidIndex.remove(objectId);
    projectEvents.remove(networkCall);
  }

  public @Nullable
  Completable getByUuid(UUID networkCall) {
    return networkCall == null ? null : projectEvents.get(networkCall);
  }

  public @Nullable
  Completable getByObjectId(String objectId) {
    if (objectId == null) {
      return null;
    }
    UUID networkCall = objectUuidIndex.get(objectId);
    return getByUuid(networkCall);
  }
}
