package org.rares.miner49er.cache;

import com.pushtorefresh.storio3.Optional;
import io.reactivex.functions.Predicate;
import org.rares.miner49er.persistence.dao.AbstractViewModel;

import java.util.List;

public interface Cache<Type extends AbstractViewModel> {

    void putData(List<Type> list, Predicate<Type> ptCondition, boolean linkWithParent);
    void putData(List<Type> list, boolean linkWithParent);
    void putData(Type t, boolean linkWithParent);

    void removeData(Type t);

    Type getData(Long id);
    List<Type> getData(Optional<Long> parentId);
}
