package org.rares.miner49er.domain.entries.persistence;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er._abstract.UiEvent;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.EventBroadcaster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_ENTRY;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_ISSUE;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_PROJECT;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ENTRIES;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ENTRY;

public class TimeEntriesRepository extends Repository {

    private static final String TAG = TimeEntriesRepository.class.getSimpleName();

    private AsyncGenericDao<TimeEntryData> asyncDao = InMemoryCacheAdapterFactory.ofType(TimeEntryData.class);

    public TimeEntriesRepository() {
    }

    @Override
    public void setup() {
        if (disposables == null || disposables.isDisposed()) {
            disposables = new CompositeDisposable();
            if (asyncDao instanceof EventBroadcaster) {
                disposables.add(
                        ((EventBroadcaster) asyncDao).getBroadcaster()
                                .onBackpressureLatest()
                                .filter(b -> CACHE_EVENT_UPDATE_ENTRIES.equals(b) ||
                                        CACHE_EVENT_UPDATE_ENTRY.equals(b) ||
                                        CACHE_EVENT_REMOVE_ENTRY.equals(b) ||
                                        CACHE_EVENT_REMOVE_ISSUE.equals(b) ||
                                        CACHE_EVENT_REMOVE_PROJECT.equals(b)
                                )
//                                .doOnNext(b -> {
//                                    if (b.equals(CACHE_EVENT_REMOVE_ENTRY)) {
//                                        Log.v(TAG, "TimeEntriesRepository: <<<< CACHE_EVENT_REMOVE_ENTRY");
//                                    } else if (b.equals(CACHE_EVENT_UPDATE_ENTRIES)) {
//                                        Log.v(TAG, "TimeEntriesRepository: <<<< CACHE_EVENT_UPDATE_ENTRIES");
//                                    } else if (b.equals(CACHE_EVENT_UPDATE_ENTRY)) {
//                                        Log.v(TAG, "TimeEntriesRepository: <<<< CACHE_EVENT_UPDATE_ENTRY");
//                                    } else if (b.equals(CACHE_EVENT_REMOVE_ISSUE)) {
//                                        Log.v(TAG, "TimeEntriesRepository: <<<< CACHE_EVENT_REMOVE_ISSUE");
//                                    } else if (b.equals(CACHE_EVENT_REMOVE_PROJECT)) {
//                                        Log.v(TAG, "TimeEntriesRepository: <<<< CACHE_EVENT_REMOVE_PROJECT");
//                                    } else {
//                                        Log.v(TAG, "TimeEntriesRepository: <<<< OTHER....");
//                                    }
//                                })
                                .throttleLatest(1, TimeUnit.SECONDS)
                                .subscribe(o -> refreshData(true)));
            }
        }

    }

    @Override
    public void registerSubscriber(Consumer<List> consumer) {
        disposables.add(
                userActionsObservable
//                        .map(c -> getDbItems(getTimeEntriesQuery(), TimeEntry.class))
//                        .startWith(getDbItems(getTimeEntriesQuery(), TimeEntry.class))
                        .map(c -> {
                            Log.i(TAG, "registerSubscriber: MAP");
                            return getDbItems();
                        })
                        .startWith(getDbItems())
//                        .map(list -> db2vm(list, true))
                        .onBackpressureDrop()
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));


    }

    @Override
    public void shutdown() {
        userActionProcessor.onComplete();
        disposables.dispose();
    }

    @Override
    public void refreshData(boolean onlyLocal) {
        Log.d(TAG, "refreshData() called with: onlyLocal = [" + onlyLocal + "]");
        userActionProcessor.onNext(UiEvent.TYPE_CLICK);
    }

    private List<TimeEntryData> getDbItems() {
        List<TimeEntryData> timeEntryDataList = asyncDao.getAll(parentProperties.getId(), true).blockingGet();
        List<TimeEntryData> clones = new ArrayList<>();
        for (TimeEntryData teData : timeEntryDataList) {
            if (!teData.deleted) {
                clones.add(teData.clone());
            }
        }
        return clones;
    }
}
