package org.rares.miner49er.cache.optimizer.fakes;

import com.pushtorefresh.storio3.Optional;
import io.reactivex.Single;
import org.rares.miner49er.persistence.dao.GenericEntityDao;

import java.util.ArrayList;
import java.util.List;

public class GenericDaoFake<ET> implements GenericEntityDao<ET> {

    public boolean booleanToReturn;
    public long longToReturn;
    public List<ET> list = new ArrayList<>();
    public ET object;
    public long parentId;
    public long id;
    public String term;

    @Override
    public Single<List<ET>> getAll() {
        return Single.just(list);
    }

    @Override
    public Single<List<ET>> getAll(long parentId) {
        this.parentId = parentId;
        return Single.just(list);
    }

    @Override
    public Single<List<ET>> getMatching(String term) {
        this.term = term;
        return Single.just(list);
    }

    @Override
    public Single<Optional<ET>> get(long id) {
        this.id = id;
        return Single.just(Optional.of(object));
    }

    @Override
    public Single<Boolean> insert(ET toInsert) {
        object = toInsert;
        return Single.just(booleanToReturn);
    }

    @Override
    public Single<Boolean> insert(List<ET> insert) {
        list = insert;
        return Single.just(booleanToReturn);
    }

    @Override
    public Single<Boolean> update(ET toInsert) {
        object = toInsert;
        return Single.just(booleanToReturn);
    }

    @Override
    public Single<Boolean> update(List<ET> update) {
        list = update;
        return Single.just(booleanToReturn);
    }

    @Override
    public Single<Boolean> delete(ET toDelete) {
        object = toDelete;
        return Single.just(booleanToReturn);
    }

    @Override
    public Single<Boolean> delete(List<ET> toDelete) {
        list = toDelete;
        return Single.just(booleanToReturn);
    }

}
