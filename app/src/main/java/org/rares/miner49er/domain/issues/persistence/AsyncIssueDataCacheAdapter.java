package org.rares.miner49er.domain.issues.persistence;

import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.Changes;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.cacheadapter.AbstractAsyncCacheAdapter;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.AsyncGenericDaoFactory;

import java.util.List;

public class AsyncIssueDataCacheAdapter
        extends AbstractAsyncCacheAdapter
        implements AsyncGenericDao<IssueData> {

    private final AsyncGenericDao<IssueData> dao = AsyncGenericDaoFactory.ofType(IssueData.class);
    private final Cache<IssueData> issueDataCache = cache.getCache(IssueData.class);
    private final Cache<ProjectData> projectDataCache = cache.getCache(ProjectData.class);

    private static final String TAG = AsyncIssueDataCacheAdapter.class.getSimpleName();

    @Override   // typically used to get all data when the cache is empty
    public Single<List<IssueData>> getAll(boolean lazy) {

        SingleSubject<List<IssueData>> singleSubject = SingleSubject.create();

        List<IssueData> cachedIssues = issueDataCache.getData(Optional.of(null));
        Single<List<IssueData>> dataSingle = dao.getAll(lazy).subscribeOn(Schedulers.io());
        getDisposables().add(dataSingle
//                .doOnSuccess((x) -> Log.v(TAG, "getAll: [][] onSuccess"))
                .observeOn(Schedulers.computation())
                .subscribe(list -> {
//                    Log.e(TAG, String.format(">> >> getAllIssues: cached issues: %s vs db issues %s.", cachedIssues.size(), list.size()));
                    issueDataCache.putData(list, false);
                    singleSubject.onSuccess(list);
                })
        );

        return singleSubject;
    }

    @Override
    public Single<List<IssueData>> getAll(long parentId, boolean lazy) {
        SingleSubject<List<IssueData>> singleSubject = SingleSubject.create();
        final List<IssueData> cachedIssues = issueDataCache.getData(Optional.of(parentId));
        if (cachedIssues != null && !cachedIssues.isEmpty()) {
            return Single.just(cachedIssues);
        } else {
//            Log.d(TAG, " >> >> getAll() called with: parentId = [" + parentId + "], lazy = [" + lazy + "] " + Thread.currentThread().getName());

            Single<List<IssueData>> dataSingle = dao.getAll(parentId, lazy).subscribeOn(Schedulers.io());
            getDisposables().add(
                    dataSingle
                            .observeOn(Schedulers.computation())
                            .subscribe(list -> {
//                                Log.w(TAG, String.format("getAll: %s %s", list.size(), Thread.currentThread().getName()));
                                issueDataCache.putData(list, true);
                                singleSubject.onSuccess(list);
                            }));

            return singleSubject;
        }
    }

    @Override
    public Single<List<IssueData>> getMatching(String term, Optional<Long> parentId, boolean lazy) {
        return dao.getMatching(term, parentId, lazy);
    }

    @Override
    public Single<Optional<IssueData>> get(long id, boolean lazy) {
        SingleSubject<Optional<IssueData>> singleSubject = SingleSubject.create();
        Optional<IssueData> cachedIssueData = Optional.of(issueDataCache.getData(id));
        if (cachedIssueData.isPresent()) {
            return Single.just(cachedIssueData);
        } else {
            Single<Optional<IssueData>> dataSingle = dao.get(id, lazy).subscribeOn(Schedulers.io());

            getDisposables().add(dataSingle
                    .observeOn(Schedulers.computation())
                    .subscribe(issueOptional -> {
                        if (issueOptional.isPresent()) {
                            issueDataCache.putData(issueOptional.get(), true);
                        }
                        singleSubject.onSuccess(issueOptional);
                    }));

            return singleSubject;
        }
    }

    @Override
    public Single<Long> insert(IssueData toInsert) {
        SingleSubject<Long> singleSubject = SingleSubject.create();
        getDisposables().add(
                dao.insert(toInsert).subscribeOn(Schedulers.io())
                        .subscribe(id -> {
                            toInsert.id = id;
                            issueDataCache.putData(toInsert, true);
                            singleSubject.onSuccess(id);
                        }));
        return singleSubject;
    }

    @Override
    public Single<Boolean> update(IssueData toUpdate) {
        SingleSubject<Boolean> singleSubject = SingleSubject.create();
        getDisposables().add(
                dao.update(toUpdate).subscribeOn(Schedulers.io())
                        .subscribe(updated->{
                            issueDataCache.putData(toUpdate, true);
                            singleSubject.onSuccess(updated);
                        })
        );
        return singleSubject;
    }

    @Override
    public Single<Boolean> delete(IssueData toDelete) {
        SingleSubject<Boolean> singleSubject = SingleSubject.create();
        getDisposables().add(dao.delete(toDelete).subscribeOn(Schedulers.io())
                .subscribe(deleted->{
                    issueDataCache.removeData(toDelete);
                    singleSubject.onSuccess(deleted);
                }));
        return singleSubject;
    }

    @Override
    public Flowable<Changes> getDbChangesFlowable() {
        return dao.getDbChangesFlowable();
    }
}
