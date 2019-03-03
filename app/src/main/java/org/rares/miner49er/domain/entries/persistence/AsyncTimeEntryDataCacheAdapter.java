package org.rares.miner49er.domain.entries.persistence;

import android.util.Log;
import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.cache.AbstractAsyncCacheAdapter;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.AsyncGenericDaoFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncTimeEntryDataCacheAdapter
        extends AbstractAsyncCacheAdapter
        implements AsyncGenericDao<TimeEntryData> {

    private AsyncGenericDao<TimeEntryData> dao = AsyncGenericDaoFactory.ofType(TimeEntryData.class);
    private final Cache<TimeEntryData> timeEntryDataCache = cache.getCache(TimeEntryData.class);
    private final LruCache<Long, IssueData> issueDataCache = cache.getIssuesCache();
    private final LruCache<Long, UserData> userDataCache = cache.getUsersCache();

    public static final String TAG = AsyncTimeEntryDataCacheAdapter.class.getSimpleName();

    @Override
    public Single<List<TimeEntryData>> getAll(boolean lazy) {
        List<TimeEntryData> cachedTimeEntries = new ArrayList<>(cache.getTimeEntriesCache().snapshot().values());
        final List<TimeEntryData> timeEntryDataList = Collections.emptyList();
        Single<List<TimeEntryData>> dataSingle = dao.getAll(lazy).subscribeOn(Schedulers.io());

        getDisposables().add(
                dataSingle
                        .observeOn(Schedulers.computation())
                        .subscribe(list -> {
                            Log.e(TAG, ">> >> getAllTimeEntries: cache: " + cachedTimeEntries.size() + ", db: " + list.size());
                            timeEntryDataCache.putData(list, false);
                            Log.e(TAG, "getAll: done linking time entries");
                        })
        );

        return Single.just(timeEntryDataList);
    }

    @Override
    public Single<List<TimeEntryData>> getAll(final long parentId, final boolean lazy) {
        final IssueData parentIssue = issueDataCache.get(parentId);
        final List<TimeEntryData> cachedTimeEntries = parentIssue.getTimeEntries();// timeEntryDataCache.getData(parentIssue);
        final List<TimeEntryData> timeEntryDataList = new ArrayList<>();
        if (cachedTimeEntries != null) {
            return Single.just(cachedTimeEntries);
        } else {
            Single<List<TimeEntryData>> dataSingle = dao.getAll(parentId, lazy).subscribeOn(Schedulers.io());

            getDisposables().add(
                    dataSingle
                            .observeOn(Schedulers.computation())
                            .flatMapObservable(
                                    list -> {
                                        Log.e(TAG, ">> >> getAllTimeEntries: cache: " + 0 + ", db: " + list.size());
                                        return Observable.fromIterable(list);
                                    }
                            )
                            .subscribe(timeEntryData -> {
                                setUserData(timeEntryData);
                                timeEntryDataCache.putData(timeEntryData, true);
                            }));

            return Single.just(timeEntryDataList);
        }
    }

    @Override
    public Single<List<TimeEntryData>> getMatching(String term, boolean lazy) {
        return dao.getMatching(term, lazy).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Optional<TimeEntryData>> get(long id, boolean lazy) {
        Optional<TimeEntryData> cachedTimeEntryDataOptional = Optional.of(cache.getTimeEntriesCache().get(id));
        final TimeEntryData timeEntryData = new TimeEntryData();
        if (cachedTimeEntryDataOptional.isPresent()) {
            return Single.just(cachedTimeEntryDataOptional);
        } else {
            Single<Optional<TimeEntryData>> dataSingle = dao.get(id, lazy).subscribeOn(Schedulers.io());

            getDisposables().add(dataSingle
                    .observeOn(Schedulers.computation())
                    .subscribe(timeEntryDataOptional -> {
                        if (timeEntryDataOptional.isPresent()) {
                            TimeEntryData newTimeEntryData = timeEntryDataOptional.get();
                            setUserData(newTimeEntryData);
                            timeEntryDataCache.putData(newTimeEntryData, true);
                        }
                    }));

            return Single.just(Optional.of(timeEntryData));
        }
    }

    @Override
    public Single<Long> insert(TimeEntryData toInsert) {
        timeEntryDataCache.putData(toInsert, true);
        return dao.insert(toInsert).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> update(TimeEntryData toUpdate) {
        timeEntryDataCache.putData(toUpdate, false);
        return dao.update(toUpdate).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> delete(TimeEntryData toDelete) {
        timeEntryDataCache.removeData(toDelete);
        return dao.delete(toDelete).subscribeOn(Schedulers.io());
    }

    private void setUserData(TimeEntryData timeEntryData) {
        if (timeEntryData.getUserName() == null || timeEntryData.getUserPhoto() == null) {
            UserData user = userDataCache.get(timeEntryData.getUserId());
            if (user != null) {
                timeEntryData.setUserName(user.getName());
                timeEntryData.setUserPhoto(user.getPicture());
            }
        }
    }
}
