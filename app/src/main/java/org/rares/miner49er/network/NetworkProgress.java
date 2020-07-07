package org.rares.miner49er.network;

import org.reactivestreams.Subscriber;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;

@Deprecated
public class NetworkProgress {

    private Map<String, Set<String>> networkCalls = new HashMap<>();
    private Map<String, Set<Subscriber<String>>> networkCallListeners = new HashMap<>();

    public Set<String> addNetworkCall(String ncParentId, String ncCallId, Subscriber<String> listener) {
      Set<String> subCalls = networkCalls.get(ncParentId);
      Set<Subscriber<String>> listeners = networkCallListeners.get(ncParentId);
      if (subCalls == null) {
        subCalls = new HashSet<>();
      }
      if (listeners == null) {
        listeners = new HashSet<>();
      }
      if (listener != null) {
        listeners.add(listener);
        listener.onSubscribe(null);
      }
      subCalls.add(ncCallId);
      networkCalls.put(ncParentId, subCalls);
      networkCallListeners.put(ncParentId, listeners);
      return subCalls;
    }

    public void completeNetworkCall(String ncParentId, String callId) {
      Set<String> subCalls = networkCalls.get(ncParentId);
      Set<Subscriber<String>> listeners = networkCallListeners.get(ncParentId);
      if (subCalls != null) {
        subCalls.remove(callId);
        if (listeners != null) {
          for (Subscriber<String> listener : listeners) {
            listener.onNext(callId);  // why onNext?
          }
        }
      }
      if (subCalls != null && subCalls.size() == 0) {
        if (listeners != null) {
          for (Subscriber<String> listener : listeners) {
            listener.onComplete();
          }
          listeners.clear();
          networkCallListeners.remove(ncParentId);
          networkCalls.remove(ncParentId);
        }
      }
    }

    public void cancelNetworkCall(String ncParentId, String callId) {
      Set<String> subCalls = networkCalls.get(ncParentId);
      Set<Subscriber<String>> listeners = networkCallListeners.get(ncParentId);
      if (subCalls != null) {
        subCalls.remove(callId);
        if (listeners != null) {
          for (Subscriber<String> listener : listeners) {
            listener.onError(new CancellationException(callId + " was cancelled."));
          }
        }
      }
      if (subCalls != null && subCalls.size() == 0) {
        if (listeners != null) {
          for (Subscriber<String> listener : listeners) {
            listener.onError(new CancellationException(callId + " did not complete."));
          }
          listeners.clear();
          networkCallListeners.remove(ncParentId);
          networkCalls.remove(ncParentId);
        }
      }
    }
  }