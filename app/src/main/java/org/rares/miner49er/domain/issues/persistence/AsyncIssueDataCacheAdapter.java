package org.rares.miner49er.domain.issues.persistence;

import android.util.Log;
import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.cache.AbstractAsyncCacheAdapter;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.AsyncGenericDaoFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncIssueDataCacheAdapter
        extends AbstractAsyncCacheAdapter
        implements AsyncGenericDao<IssueData> {

    private final AsyncGenericDao<IssueData> dao = AsyncGenericDaoFactory.ofType(IssueData.class);
    private final Cache<IssueData> issueDataCache = cache.getCache(IssueData.class);
    private final LruCache<Long, ProjectData> projectDataCache = cache.getProjectsCache();

    private static final String TAG = AsyncIssueDataCacheAdapter.class.getSimpleName();

    @Override   // typically used to get all data when the cache is empty
    public Single<List<IssueData>> getAll(boolean lazy) {
        List<IssueData> cachedIssues = new ArrayList<>(cache.getIssuesCache().snapshot().values());
        Single<List<IssueData>> dataSingle = dao.getAll(lazy).subscribeOn(Schedulers.io());
        getDisposables().add(dataSingle
                .observeOn(Schedulers.computation())
                .subscribe(list -> {
                    Log.e(TAG, String.format(">> >> getAllIssues: cached issues: %s vs db issues %s.", cachedIssues.size(), list.size()));
                    issueDataCache.putData(list, false);
                    Log.e(TAG, "getAll: ---- done linking issues.");
                })
        );

        return Single.just(Collections.emptyList());
    }

    @Override
    public Single<List<IssueData>> getAll(long parentId, boolean lazy) {
        final ProjectData parentProject = projectDataCache.get(parentId);
        final List<IssueData> cachedIssues = parentProject.getIssues();
        if (cachedIssues != null) {
            return Single.just(cachedIssues);
        } else {
            Log.d(TAG, " >> >> getAll() called with: parentId = [" + parentId + "], lazy = [" + lazy + "] " + Thread.currentThread().getName());

            getDisposables().add(
                    dao.getAll(parentId, lazy)
                            .observeOn(Schedulers.computation())
                            .subscribe(list -> {
                                Log.w(TAG, String.format("getAll: %s %s", list.size(), Thread.currentThread().getName()));
                                issueDataCache.putData(list, true);
                            }));

            return Single.just(Collections.emptyList());
        }
    }

    @Override
    public Single<List<IssueData>> getMatching(String term, boolean lazy) {
        return dao.getMatching(term, lazy);
    }

    @Override
    public Single<Optional<IssueData>> get(long id, boolean lazy) {
        Optional<IssueData> cachedIssueData = Optional.of(cache.getIssuesCache().get(id));
        IssueData issueData = new IssueData();
        if (cachedIssueData.isPresent()) {
            return Single.just(cachedIssueData);
        } else {
            Single<Optional<IssueData>> dataSingle = dao.get(id, lazy).subscribeOn(Schedulers.io());

            getDisposables().add(dataSingle
                    .observeOn(Schedulers.computation())
                    .subscribe(issueOptional -> {
                        if (issueOptional.isPresent()) {
                            IssueData newIssueData = issueOptional.get();
                            issueData.updateData(newIssueData);
                        }
                    }));

            return Single.just(Optional.of(issueData));
        }
    }

    @Override
    public Single<Long> insert(IssueData toInsert) {
        issueDataCache.putData(toInsert, true);
        return dao.insert(toInsert).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> update(IssueData toUpdate) {
        issueDataCache.putData(toUpdate, true);
        return dao.update(toUpdate).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> delete(IssueData toDelete) {
        issueDataCache.removeData(toDelete);
        return dao.delete(toDelete).subscribeOn(Schedulers.io());
    }
}
