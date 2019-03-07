package org.rares.miner49er.persistence.dao;

import io.reactivex.Flowable;

public interface EventBroadcaster {
//    void registerEventListener(Consumer<Byte> listener);
    Flowable<Byte> getBroadcaster();
    void sendEvent(Byte event);
}
