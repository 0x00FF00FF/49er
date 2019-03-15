package org.rares.miner49er.domain.issues.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.IssueStorIOSQLiteGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.LazyIssueGetResolver;
import org.rares.miner49er.persistence.storio.tables.IssueTable;

import java.util.List;


public class AsyncIssuesDao implements AsyncGenericDao<IssueData> {

    public static AsyncGenericDao<IssueData> getInstance() {
        return INSTANCE;
    }


    @Override
    public Single<List<IssueData>> getAll(boolean lazy) {
        return (lazy ?
                lazyResolver.getAllAsync(storio) :
                eagerResolver.getAllAsync(storio))
                .subscribeOn(Schedulers.io())
                .map(list -> daoConverter.dmToVm(list));
    }

    @Override
    public Single<List<IssueData>> getAll(long parentId, boolean lazy) {
        Log.d(TAG, "getAll() called with: parentId = [" + parentId + "], lazy = [" + lazy + "] " + Thread.currentThread().getName());
        return (lazy ?
                lazyResolver.getAllAsync(storio, parentId) :
                eagerResolver.getAllAsync(storio, parentId))
                .doOnSuccess(x -> Log.i(TAG, "getAll: SUCCESS " + Thread.currentThread().getName()))
                .doOnError(x -> Log.e(TAG, "getAll: ERROR " + Thread.currentThread().getName(), x))
                .doOnDispose(() -> Log.i(TAG, "getAll: DISPOSE " + Thread.currentThread().getName()))
                .subscribeOn(Schedulers.io())
                .map(list -> {
                    Log.i(TAG, "getAll: map " + Thread.currentThread().getName());
                    return daoConverter.dmToVm(list);
                });
    }

    @Override
    public Single<List<IssueData>> getMatching(String term, boolean lazy) {
        return (lazy ?
                lazyResolver.getMatchingNameAsync(storio, term) :
                eagerResolver.getMatchingNameAsync(storio, term))
                .subscribeOn(Schedulers.io())
                .map(list -> daoConverter.dmToVm(list));
    }

    @Override
    public Single<Optional<IssueData>> get(long id, boolean lazy) {
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
    public Single<Long> insert(IssueData toInsert) {
        Log.d(TAG, "insert() called with: toInsert = [" + toInsert + "]");
        assertInsertReady(toInsert);
        return storio.put()
                .object(daoConverter.vmToDm(toInsert))
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::insertedId);
    }

    @Override
    public Single<Boolean> update(IssueData toUpdate) {
        assertUpdateReady(toUpdate);
        return storio.put()
                .object(daoConverter.vmToDm(toUpdate))
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::wasUpdated);
    }

    @Override
    public Single<Boolean> delete(IssueData toDelete) {
        assertDeleteReady(toDelete);
        return storio.delete()
                .object(daoConverter.vmToDm(toDelete))
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(dr -> dr.numberOfRowsDeleted() == 0);
    }

    private AsyncIssuesDao() {
    }

    private static final String TAG = AsyncIssuesDao.class.getSimpleName();
    private static final AsyncIssuesDao INSTANCE = new AsyncIssuesDao();
    private StorIOSQLite storio = StorioFactory.INSTANCE.get();

    private LazyIssueGetResolver lazyResolver = StorioFactory.INSTANCE.getLazyIssueGetResolver();
    private IssueStorIOSQLiteGetResolver eagerResolver = StorioFactory.INSTANCE.getIssueStorIOSQLiteGetResolver();
    private DaoConverter<Issue, IssueData> daoConverter = DaoConverterFactory.of(Issue.class, IssueData.class);
    @Getter
    private final Flowable<Changes> dbChangesFlowable = storio.observeChangesInTable(IssueTable.NAME, BackpressureStrategy.LATEST);
}
