package org.rares.miner49er.domain.projects.persistence;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er.cache.cacheadapter.AbstractAsyncCacheAdapter;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.cache.optimizer.CacheFeeder;
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
        if (asyncDao instanceof EventBroadcaster) {
            disposables.add(
                    ((EventBroadcaster) asyncDao).getBroadcaster()
                            .throttleLatest(500, TimeUnit.MILLISECONDS)
                            .subscribe(o -> refreshData(true)));
        }
    }

    @Override
    public void setup() {
        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
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

    @Override
    public void registerSubscriber(Consumer<List> consumer) {
        disposables.add(
//                Flowable.merge(
                userActionsObservable
                        .map(action -> getData())
                        .startWith(() -> {
                            Disposable cacheFillDisposable = cacheFeeder.enqueueCacheFill();
                            if (cacheFillDisposable != null) {
                                disposables.add(cacheFillDisposable);
                                return Collections.<List<ProjectData>>emptyList().iterator();
                            } else {
                                return Collections.singleton(getData()).iterator();
                            }
                        })

//                )
                        .onBackpressureDrop()
                        // FIXME: 3/1/19 | comment next line out, do not use throttle in ViewModelCache events and fix the LayoutManager!
                        .debounce(1, TimeUnit.SECONDS)
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
//                        .doOnNext(x -> Log.i(TAG, "registerSubscriber: -> " + x.size()))
//                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
//                        .doOnComplete(() -> Log.d(TAG, "registerSubscriber: success"))
//                        .doOnSubscribe(x -> Log.i(TAG, "registerSubscriber: on subscribe"))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));
    }

    private List<ProjectData> getData() {
        List<ProjectData> toReturn = asyncDao.getAll(true)
                .timeout(1, TimeUnit.SECONDS)   // 1 second should be enough to get data (from cache)
                .doOnError(e -> Log.e(TAG, "getData: timeout? ", e))
                .onErrorReturnItem(Collections.emptyList())
                // something gets stuck when there is no data in the db
                // and data is being transferred
                .blockingGet();

        Log.i(TAG, "getData: toReturn: " + toReturn.size());

        List<ProjectData> clones = new ArrayList<>();
        for (ProjectData prd : toReturn) {
            ProjectData clone = new ProjectData();
            clone.updateData(prd);
            clones.add(clone);
        }
        return clones;
//        return toReturn;
    }

    private final CacheFeeder cacheFeeder = new CacheFeeder();
}
