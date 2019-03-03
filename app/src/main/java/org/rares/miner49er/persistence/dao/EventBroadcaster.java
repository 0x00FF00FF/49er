package org.rares.miner49er.persistence.dao;

import io.reactivex.functions.Consumer;

public interface EventBroadcaster {
    void registerEventListener(Consumer<Object> listener);
    void sendEvent();
}
