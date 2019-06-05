package org.rares.miner49er.fakes;

import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.Changes;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.util.ArrayList;
import java.util.List;

public class AsyncGenericDaoFake<VMT extends AbstractViewModel> implements AsyncGenericDao<VMT> {

    public boolean booleanToReturn;
    public long longToReturn;
    public List<VMT> list = new ArrayList<>();
    public VMT object;
    public long parentId;
    public long id;
    public String term;

    @Override
    public Single<List<VMT>> getAll(boolean lazy) {
        return Single.just(list);
    }

    @Override
    public Single<List<VMT>> getAll(long parentId, boolean lazy) {
        this.parentId = parentId;
        return Single.just(list);
    }

    @Override
    public Single<List<VMT>> getMatching(String term, Optional<Long> parentId, boolean lazy) {
        this.term = term;
        return Single.just(list);
    }

    @Override
    public Single<Optional<VMT>> get(long id, boolean lazy) {
        this.id = id;
        return Single.just(Optional.of(object));
    }

    @Override
    public Single<Long> insert(VMT toInsert) {
        System.out.println("insert called.");
        object = toInsert;
        return Single.just(id);
    }

    @Override
    public Single<Boolean> update(VMT toUpdate) {
        object = toUpdate;
        return Single.just(booleanToReturn);
    }

    @Override
    public Single<Boolean> delete(VMT toDelete) {
        object = toDelete;
        return Single.just(booleanToReturn);
    }

    @Override
    public Flowable<Changes> getDbChangesFlowable() {
        return null;
    }
}
