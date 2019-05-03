package org.rares.miner49er.domain.projects.persistence;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er.cache.cacheadapter.AbstractAsyncCacheAdapter;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.model.ProjectsSort;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.EventBroadcaster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProjectsRepository extends Repository {
    private static final String TAG = ProjectsRepository.class.getSimpleName();

    private ProjectsSort projectsSort = new ProjectsSort();
    private AsyncGenericDao<ProjectData> asyncDao = InMemoryCacheAdapterFactory.ofType(ProjectData.class);

    public ProjectsRepository() {
    }

    @Override
    public void setup() {
        if (disposables == null || disposables.isDisposed()) {
            disposables = new CompositeDisposable();
            if (asyncDao instanceof EventBroadcaster) {
                disposables.add(
                        ((EventBroadcaster) asyncDao).getBroadcaster()
                                .onBackpressureBuffer()
//                                .doOnNext(b -> {
//                                    if (b.equals(CACHE_EVENT_UPDATE_PROJECTS)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_PROJECTS");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_ISSUES)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ISSUES  ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_ENTRIES)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ENTRIES ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_USERS)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_USERS   ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_PROJECT)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_PROJECT ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_ISSUE)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ISSUE   ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_ENTRY)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ENTRY   ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_USER)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_UPDATE_USER    ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_REMOVE_PROJECT)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_REMOVE_PROJECT ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_REMOVE_ISSUE)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_REMOVE_ISSUE   ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_REMOVE_ENTRY)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_REMOVE_ENTRY   ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_REMOVE_USER)) {
//                                        Log.v(TAG, "setup: <<<< CACHE_EVENT_REMOVE_USER    ");
//                                    }
//                                    Log.i(TAG, "setup: " + asyncDao.getAll(true).blockingGet().size());
//                                })
//                                .throttleLast(500, TimeUnit.MILLISECONDS)
                                .subscribe(o -> refreshData(true)));
            }
        }
    }

    @Override
    public void shutdown() {
//        Log.w(TAG, "shutdown() called.");
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
    public void registerSubscriber(Consumer<List> consumer) {
        disposables.add(
//                Flowable.merge(
                userActionsObservable
                        .onBackpressureBuffer()
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .map(action -> getData())       // todo: [getFromCache|getFromDB]|getFromNetwork -> take 1 ->
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));
    }

    private List<ProjectData> getData() {
        List<ProjectData> toReturn = asyncDao.getAll(true)
                .timeout(250, TimeUnit.MILLISECONDS)   // should be enough to get data (from cache)
                .doOnError(e -> Log.e(TAG, "getData: setup timeout? ", e))
                .onErrorReturnItem(Collections.emptyList())
                // something gets stuck when there is no data in the db
                // and data is being transferred
                .blockingGet();
        List<ProjectData> clones = new ArrayList<>();
        for (ProjectData prd : toReturn) {
            if (!prd.deleted) {
                clones.add(prd.clone(false));
            }
        }
        return clones;
    }
}
