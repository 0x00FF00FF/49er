package org.rares.miner49er.domain.entries.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.cacheadapter.AbstractAsyncCacheAdapter;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.AsyncGenericDaoFactory;

import java.util.ArrayList;
import java.util.List;

public class AsyncTimeEntryDataCacheAdapter
        extends AbstractAsyncCacheAdapter
        implements AsyncGenericDao<TimeEntryData> {

    private AsyncGenericDao<TimeEntryData> dao = AsyncGenericDaoFactory.ofType(TimeEntryData.class);
    private final Cache<TimeEntryData> timeEntryDataCache = cache.getCache(TimeEntryData.class);
    private final Cache<IssueData> issueDataCache = cache.getCache(IssueData.class);
    private final Cache<UserData> userDataCache = cache.getCache(UserData.class);

    public static final String TAG = AsyncTimeEntryDataCacheAdapter.class.getSimpleName();

    @Override
    public Single<List<TimeEntryData>> getAll(boolean lazy) {

        SingleSubject<List<TimeEntryData>> singleSubject = SingleSubject.create();
        List<TimeEntryData> cachedTimeEntries = new ArrayList<>(timeEntryDataCache.getData(Optional.of(null)));
        Single<List<TimeEntryData>> dataSingle = dao.getAll(lazy).subscribeOn(Schedulers.io());

        getDisposables().add(
                dataSingle
                        .doOnSuccess((x) -> Log.v(TAG, "getAll: [][] onSuccess"))
                        .observeOn(Schedulers.computation())
                        .subscribe(list -> {
                            Log.e(TAG, ">> >> getAllTimeEntries: cache: " + cachedTimeEntries.size() + ", db: " + list.size());
                            timeEntryDataCache.putData(list, false);
                            Log.e(TAG, "getAll: done linking time entries");
                            singleSubject.onSuccess(list);
                        })
        );

        return singleSubject;
    }

    @Override
    public Single<List<TimeEntryData>> getAll(final long parentId, final boolean lazy) {
        SingleSubject<List<TimeEntryData>> singleSubject = SingleSubject.create();
        final IssueData parentIssue = issueDataCache.getData(parentId);
        final List<TimeEntryData> cachedTimeEntries = parentIssue.getTimeEntries();// timeEntryDataCache.getData(parentIssue);
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
                                        singleSubject.onSuccess(list);
                                        return Observable.fromIterable(list);
                                    }
                            )
                            .subscribe(timeEntryData -> {
                                setUserData(timeEntryData);
                                timeEntryDataCache.putData(timeEntryData, true);
                            }));

            return dataSingle;
        }
    }

    @Override
    public Single<List<TimeEntryData>> getMatching(String term, boolean lazy) {
        return dao.getMatching(term, lazy).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Optional<TimeEntryData>> get(long id, boolean lazy) {
        SingleSubject<Optional<TimeEntryData>> singleSubject = SingleSubject.create();
        Optional<TimeEntryData> cachedTimeEntryDataOptional = Optional.of(timeEntryDataCache.getData(id));
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
                        singleSubject.onSuccess(timeEntryDataOptional);
                    }));

            return dataSingle;
        }
    }

    @Override
    public Single<Long> insert(TimeEntryData toInsert) {
        SingleSubject<Long> singleSubject = SingleSubject.create();
        getDisposables().add(
                dao.insert(toInsert).subscribeOn(Schedulers.io())
                        .subscribe(id -> {
                            toInsert.id = id;
                            timeEntryDataCache.putData(toInsert, true);
                            singleSubject.onSuccess(id);
                        }));
        return singleSubject;
    }

    @Override
    public Single<Boolean> update(TimeEntryData toUpdate) {
        SingleSubject<Boolean> singleSubject = SingleSubject.create();
        getDisposables().add(
                dao.update(toUpdate).subscribeOn(Schedulers.io())
                        .subscribe(updated -> {
                            timeEntryDataCache.putData(toUpdate, false);
                            singleSubject.onSuccess(updated);
                        })
        );
        return singleSubject;
    }

    @Override
    public Single<Boolean> delete(TimeEntryData toDelete) {
        SingleSubject<Boolean> singleSubject = SingleSubject.create();
        getDisposables().add(dao.delete(toDelete).subscribeOn(Schedulers.io())
                .subscribe(deleted -> {
                    timeEntryDataCache.removeData(toDelete);
                    singleSubject.onSuccess(deleted);
                }));
        return singleSubject;
    }

    private void setUserData(TimeEntryData timeEntryData) {
        if (timeEntryData.getUserName() == null || timeEntryData.getUserPhoto() == null) {
            UserData user = userDataCache.getData(timeEntryData.getUserId());
            if (user != null) {
                timeEntryData.setUserName(user.getName());
                timeEntryData.setUserPhoto(user.getPicture());
            }
        }
    }
}
