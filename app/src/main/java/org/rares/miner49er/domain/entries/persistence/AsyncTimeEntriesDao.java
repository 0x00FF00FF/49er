package org.rares.miner49er.domain.entries.persistence;

import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.LazyTimeEntryGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.TimeEntryStorIOSQLiteGetResolver;
import org.rares.miner49er.persistence.storio.tables.TimeEntryTable;

import java.util.List;

public class AsyncTimeEntriesDao implements AsyncGenericDao<TimeEntryData> {

    public static AsyncGenericDao<TimeEntryData> getInstance() {
        return INSTANCE;
    }

    @Override
    public Single<List<TimeEntryData>> getAll(boolean lazy) {
        return (lazy ?
                lazyResolver.getAllAsync(storio) :
                eagerResolver.getAllAsync(storio))
                .subscribeOn(Schedulers.io())
                .map(list -> daoConverter.dmToVm(list));
    }

    @Override
    public Single<List<TimeEntryData>> getAll(long parentId, boolean lazy) {
        return (lazy ?
                lazyResolver.getAllAsync(storio, parentId) :
                eagerResolver.getAllAsync(storio, parentId))
                .subscribeOn(Schedulers.io())
                .map(list -> daoConverter.dmToVm(list));
    }

    @Override
    public Single<List<TimeEntryData>> getMatching(String term, boolean lazy) {
        return null;
    }

    @Override
    public Single<Optional<TimeEntryData>> get(long id, boolean lazy) {
        return (lazy ?
                lazyResolver.getByIdAsync(storio, id) :
                eagerResolver.getByIdAsync(storio, id))
                .subscribeOn(Schedulers.io())
                .map(projectOptional ->
                        projectOptional.isPresent() ?
                                Optional.of(daoConverter.dmToVm(projectOptional.get())) :
                                Optional.of(null));
    }

    @Override
    public Single<Long> insert(TimeEntryData toInsert) {
        assertInsertReady(toInsert);
        return storio.put()
                .object(daoConverter.vmToDm(toInsert))
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::insertedId);
    }

    @Override
    public Single<Boolean> update(TimeEntryData toUpdate) {
        assertUpdateReady(toUpdate);
        return storio.put()
                .object(daoConverter.vmToDm(toUpdate))
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::wasUpdated);
    }

    @Override
    public Single<Boolean> delete(TimeEntryData toDelete) {
        assertDeleteReady(toDelete);

        TimeEntry timeEntry = daoConverter.vmToDm(toDelete);

        return storio.delete()
                .object(timeEntry)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(dr -> dr.numberOfRowsDeleted() != 0);
    }

    private AsyncTimeEntriesDao() {
    }

    private final static AsyncTimeEntriesDao INSTANCE = new AsyncTimeEntriesDao();
    private StorIOSQLite storio = StorioFactory.INSTANCE.get();
    private LazyTimeEntryGetResolver lazyResolver = StorioFactory.INSTANCE.getLazyTimeEntryGetResolver();
    private TimeEntryStorIOSQLiteGetResolver eagerResolver = StorioFactory.INSTANCE.getTimeEntryStorIOSQLiteGetResolver();
    private DaoConverter<TimeEntry, TimeEntryData> daoConverter = DaoConverterFactory.of(TimeEntry.class, TimeEntryData.class);
    @Getter
    private final Flowable<Changes> dbChangesFlowable = storio.observeChangesInTable(TimeEntryTable.NAME, BackpressureStrategy.LATEST);
}
