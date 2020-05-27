package org.rares.miner49er.domain.projects.persistence;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er.cache.cacheadapter.AbstractAsyncCacheAdapter;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.model.ProjectsSort;
import org.rares.miner49er.network.DataUpdater;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.EventBroadcaster;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProjectsRepository extends Repository {
  private static final String TAG = ProjectsRepository.class.getSimpleName();

  private ProjectsSort projectsSort = new ProjectsSort();
  private AsyncGenericDao<ProjectData> asyncDao = InMemoryCacheAdapterFactory.ofType(ProjectData.class);

  private DataUpdater networkDataUpdater;

  public ProjectsRepository(DataUpdater networkDataUpdater) {
    this.networkDataUpdater = networkDataUpdater;
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
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_PROJECTS");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_ISSUES)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ISSUES  ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_ENTRIES)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ENTRIES ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_USERS)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_USERS   ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_PROJECT)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_PROJECT ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_ISSUE)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ISSUE   ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_ENTRY)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ENTRY   ");
//                  }
//                  if (b.equals(CACHE_EVENT_UPDATE_USER)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_USER    ");
//                  }
//                  if (b.equals(CACHE_EVENT_REMOVE_PROJECT)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_REMOVE_PROJECT ");
//                  }
//                  if (b.equals(CACHE_EVENT_REMOVE_ISSUE)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_REMOVE_ISSUE   ");
//                  }
//                  if (b.equals(CACHE_EVENT_REMOVE_ENTRY)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_REMOVE_ENTRY   ");
//                  }
//                  if (b.equals(CACHE_EVENT_REMOVE_USER)) {
//                    Log.v(TAG, "setup: <<<< CACHE_EVENT_REMOVE_USER    ");
//                  }
//                  Log.i(TAG, "setup: " + asyncDao.getAll(true).blockingGet().size());
//                })
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> refreshData()));
      }
    }
  }

  @Override
  public void shutdown() {
//        Log.w(TAG, "shutdown() called.");
    userActionProcessor.onComplete();
    disposables.dispose();
    if (asyncDao instanceof AbstractAsyncCacheAdapter) {
      ((AbstractAsyncCacheAdapter) asyncDao).shutdown();
    }
  }

  /*
   * todo:
   * Observable.concat(
   *	[getFromLocalCache(id),
   *	getFromPersistentCache(id)],
   *	getFromNetwork(id)
   *      )
   *	.take(1)
   *	.singleOrError()
   *
   */

  @Override
  public void registerSubscriber(Consumer<List> consumer, Runnable runnable) {
    Log.i(TAG, "registerSubscriber: ");
    disposables.add(
        userActionsObservable
            .subscribeOn(Schedulers.io())
            .concatMapSingle(action -> getDbItems())
            .onBackpressureBuffer()
            .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
            .doOnSubscribe(s -> {
              if (runnable != null) {
                disposables.add(
                    Single.just("running optional command")
                        .delay(50, TimeUnit.MILLISECONDS)
                        .subscribe(a -> runnable.run()));
              }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(consumer));
  }

  private Single<List<ProjectData>> getDbItems() {
//    Thread.dumpStack();
//    Log.i(TAG, "getDbItems: <<<<<>>>>>");
    return asyncDao.getAll(true)
        .doOnError(e -> Log.e(TAG, "getDbItems: setup timeout? ", e))
        .flatMapPublisher(Flowable::fromIterable)
        .filter(p -> !p.isDeleted())
        .map(p -> p.clone(false))
        .toList()
        .doOnSuccess((list) -> {
//          Log.i(TAG, "getDbItems: db project list: " + list);
          boolean callNetwork = false;
          if (list != null && list.size() > 0) {
            AbstractViewModel data = list.get(0); // TODO: perhaps check all and if any does not comply, then network refresh
            // this rule should probably not be in the repository
//            Log.i(TAG, "getDbItems: data.lastUpdated: " + data.lastUpdated);
            if (data.lastUpdated <= 0 || (System.currentTimeMillis() - data.lastUpdated > BaseInterfaces.UPDATE_INTERVAL)) {
              callNetwork = true;
            }
          } else {
            callNetwork = true;
          }
          if (callNetwork) { // only do a network call if needed
//            Log.d(TAG, "getDbItems: network call > " + networkDataUpdater);
            networkDataUpdater.lightProjectsUpdate();
          }
        });
  }
}