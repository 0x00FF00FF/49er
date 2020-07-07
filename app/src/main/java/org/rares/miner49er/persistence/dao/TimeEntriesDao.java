package org.rares.miner49er.persistence.dao;

import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.LazyTimeEntryGetResolver;

import java.util.List;
import java.util.Map;

public class TimeEntriesDao implements GenericEntityDao<TimeEntry> {

    private LazyTimeEntryGetResolver getResolver = StorioFactory.INSTANCE.getLazyTimeEntryGetResolver();
    private StorIOSQLite storio = StorioFactory.INSTANCE.get();

    @Override
    public Single<List<TimeEntry>> getAll() {
        return getResolver.getAllAsync(storio).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<TimeEntry>> getAll(long parentId) {
        return getResolver.getAllAsync(storio, parentId).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<TimeEntry>> getMatching(String term) {
        return null;
    }

    @Override
    public Single<Optional<TimeEntry>> get(long id) {
        return getResolver.getByIdAsync(storio, id).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> insert(TimeEntry toInsert) {
//        assertInsertReady(toInsert);
        return storio.put()
                .object(toInsert)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> {
                    toInsert.setId(res.insertedId());
                    return res.wasInserted();
                });
    }

    @Override
    public Single<Boolean> insert(List<TimeEntry> toInsert) {
        return storio.put()
                .objects(toInsert)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.numberOfInserts() == toInsert.size());
    }

    @Override
    public Single<Boolean> update(TimeEntry toUpdate) {
        return storio.put()
                .object(toUpdate)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::wasUpdated);
    }

    @Override
    public Single<Boolean> update(List<TimeEntry> toUpdate) {
        return storio.put()
                .objects(toUpdate)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.numberOfInserts() + res.numberOfUpdates() == toUpdate.size());
    }

    @Override
    public Single<Boolean> delete(TimeEntry toDelete) {
        return storio.delete()
                .object(toDelete)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(dr -> dr.numberOfRowsDeleted() != 0);
    }

    @Override
    public Single<Boolean> delete(List<TimeEntry> toDelete) {
        return storio.delete()
                .objects(toDelete)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.results().size() == toDelete.size());
    }

    @Override
    public Flowable<TimeEntry> insertWithResult(List<TimeEntry> toInsert) {
        return storio
                .put()
                .objects(toInsert)
                .prepare()
                .asRxSingle()
                .flatMapPublisher(putResult->{
                    Map<TimeEntry, PutResult> resultMap = putResult.results();
                    return Flowable
                            .fromArray(resultMap.keySet().toArray(new TimeEntry[0]))
                            .map(p -> {
                                PutResult result = resultMap.get(p);
                                if (result != null && result.insertedId() != null) {
                                    p.setId(result.insertedId());
                                }
                                return p;
                            });
                });
    }

    @Override
    public Flowable<TimeEntry> getByObjectIdIn(List<String> objectIds) {
        return getResolver.getByObjectIdInAsync(storio, objectIds);
    }

    /*    @Override
    public Single<Boolean> wipe() {
        return storio.delete()
                .byQuery(DeleteQuery.builder().table(TimeEntryTable.NAME).build())
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.affectedTables().size() > 0);
    }*/

    @Getter
    private static final TimeEntriesDao instance = new TimeEntriesDao();
    private TimeEntriesDao(){}
}
