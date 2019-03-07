package org.rares.miner49er.cache;

import com.pushtorefresh.storio3.Optional;
import io.reactivex.functions.Predicate;

import java.util.List;

public interface Cache<Type> {

    Byte CACHE_EVENT_UPDATE_PROJECTS    = 0;
    Byte CACHE_EVENT_UPDATE_ISSUES      = 1;
    Byte CACHE_EVENT_UPDATE_ENTRIES     = 2;
    Byte CACHE_EVENT_UPDATE_USERS       = 3;

    Byte CACHE_EVENT_UPDATE_PROJECT     = 10;
    Byte CACHE_EVENT_UPDATE_ISSUE       = 11;
    Byte CACHE_EVENT_UPDATE_ENTRY       = 12;
    Byte CACHE_EVENT_UPDATE_USER        = 13;

    Byte CACHE_EVENT_REMOVE_PROJECT     = -10;
    Byte CACHE_EVENT_REMOVE_ISSUE       = -11;
    Byte CACHE_EVENT_REMOVE_ENTRY       = -12;
    Byte CACHE_EVENT_REMOVE_USER        = -13;

    void putData(List<Type> list, Predicate<Type> ptCondition, boolean linkWithParent);
    void putData(List<Type> list, boolean linkWithParent);
    void putData(Type t, boolean linkWithParent);

    void removeData(Type t);

    Type getData(Long id);
    List<Type> getData(Optional<Long> parentId);

    int getSize();
}
