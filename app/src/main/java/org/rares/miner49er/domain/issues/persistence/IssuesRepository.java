package org.rares.miner49er.domain.issues.persistence;

import android.util.Log;
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
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.EventBroadcaster;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_ENTRY;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_ISSUE;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_PROJECT;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_USER;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ENTRIES;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ENTRY;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ISSUE;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ISSUES;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_PROJECT;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_PROJECTS;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_USER;
//import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_USERS;

public class IssuesRepository extends Repository {

  private static final String TAG = IssuesRepository.class.getSimpleName();

  private AsyncGenericDao<IssueData> asyncDao = InMemoryCacheAdapterFactory.ofType(IssueData.class);

  private Disposable adapterDisposable = null;

  public IssuesRepository() {
//        ns.registerIssuesConsumer(this);
//        issueTableObservable =
//                storio
//                        .observeChangesInTable(IssueTable.NAME, BackpressureStrategy.LATEST)
//                        .subscribeOn(Schedulers.io());
  }

  @Override
  public void setup() {

    if (userActionProcessor.hasComplete()) {
      userActionProcessor = PublishProcessor.create();
      userActionsObservable = userActionProcessor.subscribeOn(Schedulers.io());
    }

    if (disposables == null || disposables.isDisposed()) {
      disposables = new CompositeDisposable();
      if (asyncDao instanceof EventBroadcaster) {
        disposables.add(
            ((EventBroadcaster) asyncDao).getBroadcaster()
                .onBackpressureBuffer()
//                .doOnNext(b -> {
//                  if (b.equals(CACHE_EVENT_UPDATE_PROJECTS)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_PROJECTS");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_ISSUES)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ISSUES  ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_ENTRIES)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ENTRIES ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_USERS)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_USERS   ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_PROJECT)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_PROJECT ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_ISSUE)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ISSUE   ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_ENTRY)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ENTRY   ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_USER)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_USER    ");
//                  }
//                  if (b.equals(CACHE_EVENT_REMOVE_PROJECT)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_REMOVE_PROJECT ");
//                  }
//                  if (b.equals(CACHE_EVENT_REMOVE_ISSUE)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_REMOVE_ISSUE   ");
//                  }
//                  if (b.equals(CACHE_EVENT_REMOVE_ENTRY)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_REMOVE_ENTRY   ");
//                  }
//                  if (b.equals(CACHE_EVENT_REMOVE_USER)) {
//                    Log.i(TAG, "setup: <<<< CACHE_EVENT_REMOVE_USER    ");
//                  }
//                })
//                .filter(e -> CACHE_EVENT_UPDATE_ISSUES.equals(e) ||
//                    CACHE_EVENT_UPDATE_ISSUE.equals(e) ||
//                    CACHE_EVENT_REMOVE_ISSUE.equals(e) ||
//                    CACHE_EVENT_REMOVE_PROJECT.equals(e) ||
//                    CACHE_EVENT_UPDATE_ENTRY.equals(e)
//                )
                .throttleLatest(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> refreshData(true)));
      }
    }
  }

  @Override
  public void shutdown() {
    userActionProcessor.onComplete();
    if (adapterDisposable != null && !adapterDisposable.isDisposed()) {
      adapterDisposable.dispose();
    }
    disposables.clear();
  }


  @Override
  public void registerSubscriber(Consumer<List> consumer, Runnable runnable) {
//    Log.i(TAG, "registerSubscriber: called");
    if (adapterDisposable != null && !adapterDisposable.isDisposed()) {
      adapterDisposable.dispose();
    }

    adapterDisposable =
        userActionsObservable
            .subscribeOn(Schedulers.io())
            .concatMapSingle(event -> getDbItems())
            .onBackpressureDrop()
            .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
            .onErrorResumeNext(Flowable.just(Collections.emptyList()))
            .doOnSubscribe(s -> {
              if (runnable != null) {
                disposables.add(
                    Single.just("running optional command")
                        .delay(50, TimeUnit.MILLISECONDS)
                        .subscribe(a -> runnable.run()));
              }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(consumer);


//    disposables.add(
//        Flowable.interval(800, TimeUnit.MILLISECONDS)
//            .zipWith(Flowable.range(1, 5), (aLong, integer) -> integer)
//            .subscribe(s -> System.out.println(
//                adapterDisposable == null ? "x" : adapterDisposable.isDisposed()
//            )));
  }


  private Single<List<IssueData>> getDbItems() {
    Log.d(TAG, "getDbItems() called: > " + parentProperties.getId());
    return asyncDao.getAll(parentProperties.getId(), true)
        .doOnError(e -> Log.e(TAG, "getDbItems: ", e))
        .flatMapPublisher(Flowable::fromIterable)
        .filter(i -> !i.isDeleted())
        .map(i -> i.clone(false))
        .toList();
  }

  @Override
  public void refreshData(boolean onlyLocal) {
//    Thread.dumpStack();
//    slowDown();
//    Log.d(TAG, "refreshData() called with: [" + userActionProcessor.hasSubscribers() + " " + disposables.isDisposed() + "]");
    userActionProcessor.onNext(UiEvent.TYPE_CLICK);
  }
}
