package org.rares.miner49er.domain.entries.persistence;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er._abstract.UiEvent;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.EventBroadcaster;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_ENTRY;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_ISSUE;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_PROJECT;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ENTRIES;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ENTRY;

public class TimeEntriesRepository extends Repository {

  private static final String TAG = TimeEntriesRepository.class.getSimpleName();

  private AsyncGenericDao<TimeEntryData> asyncDao = InMemoryCacheAdapterFactory.ofType(TimeEntryData.class);

  private Disposable adapterDisposable = null;

  public TimeEntriesRepository() {
  }

  @Override
  public void setup() {
    if (userActionProcessor.hasComplete()) {
      userActionProcessor = PublishProcessor.create();
      userActionsObservable = userActionProcessor.subscribeOn(Schedulers.io()).share();
    }

    if (disposables == null || disposables.isDisposed()) {
      disposables = new CompositeDisposable();
      if (asyncDao instanceof EventBroadcaster) {
        disposables.add(
            ((EventBroadcaster) asyncDao).getBroadcaster()
                .onBackpressureLatest()
//                                .filter(b -> CACHE_EVENT_UPDATE_ENTRIES.equals(b) ||
//                                        CACHE_EVENT_UPDATE_ENTRY.equals(b) ||
//                                        CACHE_EVENT_REMOVE_ENTRY.equals(b) ||
//                                        CACHE_EVENT_REMOVE_ISSUE.equals(b) ||
//                                        CACHE_EVENT_REMOVE_PROJECT.equals(b)
//                                )
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
//                                        Log.v(TAG, "TimeEntriesRepository: <<<< OTHER CACHE EVENT ....");
//                                    }
//                                })
                .throttleLatest(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> refreshData(true)));
      }
    }

  }

  @Override
  public void registerSubscriber(Consumer<List> consumer, Runnable runnable) {
    if (adapterDisposable != null && !adapterDisposable.isDisposed()) {
      adapterDisposable.dispose();
    }

    adapterDisposable = userActionsObservable
        .subscribeOn(Schedulers.io())
        .concatMapSingle(e -> getDbItems())
        .onBackpressureBuffer()
        .onErrorResumeNext(Flowable.just(Collections.emptyList()))
        .doOnSubscribe(s->{
          if (runnable != null) {
            disposables.add(
            Single.just("running optional command")
                .delay(10, TimeUnit.MILLISECONDS)
                .subscribe(a -> runnable.run()));
          }})
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(consumer);

  }

  @Override
  public void shutdown() {
    userActionProcessor.onComplete();
    if (adapterDisposable != null) {
      adapterDisposable.dispose();
    }
    disposables.clear();
  }

  @Override
  public void refreshData(boolean onlyLocal) {
//  Thread.dumpStack();
    userActionProcessor.onNext(UiEvent.TYPE_CLICK);
  }

  private Single<List<TimeEntryData>> getDbItems() {
    return asyncDao
        .getAll(parentProperties.getId(), true)
        .flatMapPublisher(Flowable::fromIterable)
        .filter(te -> !te.isDeleted())
        .map(TimeEntryData::clone)
        .toList();
  }
}
