package org.rares.miner49er.domain.projects.persistence;

import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.Changes;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.cacheadapter.AbstractAsyncCacheAdapter;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.AsyncGenericDaoFactory;

import java.util.ArrayList;
import java.util.List;

public class AsyncProjectDataCacheAdapter
        extends AbstractAsyncCacheAdapter
        implements AsyncGenericDao<ProjectData> {

    private final AsyncGenericDao<ProjectData> dao = AsyncGenericDaoFactory.ofType(ProjectData.class);
    private final Cache<ProjectData> projectDataCache = cache.getCache(ProjectData.class);

    public static final String TAG = AsyncProjectDataCacheAdapter.class.getSimpleName();

    @Override
    public Single<List<ProjectData>> getAll(boolean lazy) {
        SingleSubject<List<ProjectData>> singleSubject = SingleSubject.create();
        List<ProjectData> cached = projectDataCache.getData(Optional.of(null));
        if (cached != null && cached.size() > 0) {
//            Log.e(TAG, String.format("[ ] [ ] getAll: PROJECTS CACHE HIT. [lazy: %b] [#: %s] [%s]", lazy, cached.size(), Thread.currentThread().getName()));
            return Single.just(cached);
        } else {
            Single<List<ProjectData>> daoDataSingle = dao.getAll(lazy).subscribeOn(Schedulers.io());

            getDisposables().add(daoDataSingle
//                    .doOnSuccess((x) -> Log.v(TAG, "getAll: [][] onSuccess"))
                    .observeOn(Schedulers.computation())
                    .subscribe(list -> {
//                        Log.w(TAG, String.format(">> >> [db] getAllProjects: [#: %s], [%s]", list.size(), Thread.currentThread().getName()));
                        projectDataCache.putData(list, false);
                        singleSubject.onSuccess(list);
                    }));

            return singleSubject;
        }
    }

    @Override
    public Single<List<ProjectData>> getAll(long parentId, boolean lazy) {
        SingleSubject<List<ProjectData>> singleSubject = SingleSubject.create();
        List<ProjectData> cached = projectDataCache.getData(Optional.of(null));
        if (cached.size() > 0) {
            List<ProjectData> userProjects = new ArrayList<>();
            for (ProjectData pd : cached) {
                if (pd.getOwner() != null && pd.getOwner().id == parentId) {
                    userProjects.add(pd);
                }
            }
            return Single.just(userProjects);
        } else {
            Single<List<ProjectData>> daoDataSingle = dao.getAll(parentId, lazy).subscribeOn(Schedulers.io());
            getDisposables().add(daoDataSingle
                    .observeOn(Schedulers.computation())
                    .subscribe(list -> {
                        projectDataCache.putData(list, false);
                        singleSubject.onSuccess(list);
                    }));
            return singleSubject;
        }
    }

    @Override
    public Single<List<ProjectData>> getMatching(String term, boolean lazy) {
        return dao.getMatching(term, lazy);
    }

    @Override
    public Single<Optional<ProjectData>> get(long id, boolean lazy) {
        SingleSubject<Optional<ProjectData>> singleSubject = SingleSubject.create();
        ProjectData data = projectDataCache.getData(id);
        ProjectData dbData = new ProjectData();
        if (data != null) {
            return Single.just(Optional.of(data));
        } else {
            Single<Optional<ProjectData>> daoData = dao.get(id, lazy).subscribeOn(Schedulers.io());

            getDisposables().add(daoData
                    .observeOn(Schedulers.computation())
                    .subscribe(opt -> {
                        if (opt.isPresent()) {
                            dbData.updateData(opt.get());
                            projectDataCache.putData(opt.get(), false);
                        }
                        singleSubject.onSuccess(opt);
                    }));

            return singleSubject;
        }
    }

    @Override
    public Single<Long> insert(final ProjectData toInsert) {

        SingleSubject<Long> singleSubject = SingleSubject.create();
        getDisposables().add(
                dao.insert(toInsert).subscribeOn(Schedulers.io())
                        .subscribe(id -> {
                            toInsert.id = id;
                            projectDataCache.putData(toInsert, true);
                            singleSubject.onSuccess(id);
                        }));
        return singleSubject;
    }

    @Override
    public Single<Boolean> update(final ProjectData toUpdate) {
        SingleSubject<Boolean> singleSubject = SingleSubject.create();
        getDisposables().add(
                dao.update(toUpdate).subscribeOn(Schedulers.io())
                        .subscribe(updated ->
                        {
                            projectDataCache.putData(toUpdate, false);
                            singleSubject.onSuccess(updated);
                        })
        );
        return singleSubject;
    }

    @Override
    public Single<Boolean> delete(final ProjectData toDelete) {
        SingleSubject<Boolean> singleSubject = SingleSubject.create();
        getDisposables().add(dao.delete(toDelete).subscribeOn(Schedulers.io())
                .subscribe(deleted -> {
                    projectDataCache.removeData(toDelete);
                    singleSubject.onSuccess(deleted);
                }));
        return singleSubject;
    }

    @Override
    public Flowable<Changes> getDbChangesFlowable() {
        return dao.getDbChangesFlowable();
    }
}
