package org.rares.miner49er.domain.users.persistence;

import com.pushtorefresh.storio3.Optional;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.cacheadapter.AbstractAsyncCacheAdapter;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.AsyncGenericDaoFactory;

import java.util.List;

public class AsyncUserDataCacheAdapter
        extends AbstractAsyncCacheAdapter
        implements AsyncGenericDao<UserData> {

    private AsyncGenericDao<UserData> dao = AsyncGenericDaoFactory.ofType(UserData.class);
    private final Cache<UserData> userDataCache = cache.getCache(UserData.class);
    private final Cache<ProjectData> projectDataCache = cache.getCache(ProjectData.class);

    public static final String TAG = AsyncUserDataCacheAdapter.class.getSimpleName();

    @Override
    public Single<List<UserData>> getAll(boolean lazy) {
        SingleSubject<List<UserData>> singleSubject = SingleSubject.create();
        List<UserData> cachedUsers = userDataCache.getData(Optional.of(null));

        Single<List<UserData>> dataSingle = dao.getAll(lazy).subscribeOn(Schedulers.io());
        getDisposables().add(
                dataSingle
//                        .doOnSuccess((x) -> Log.v(TAG, "getAll: [][] onSuccess"))
                        .observeOn(Schedulers.computation())
                        .subscribe(
                                list -> {
//                                    Log.e(TAG, ">> >> getAllUsers: cache: " + cachedUsers.size() + ", db: " + list.size());
                                    userDataCache.putData(list, false);
                                    singleSubject.onSuccess(list);
                                }
                        ));
        return singleSubject;
    }

    @Override
    public Single<List<UserData>> getAll(long parentId, boolean lazy) {
        SingleSubject<List<UserData>> singleSubject = SingleSubject.create();
        ProjectData projectData = projectDataCache.getData(parentId);
        List<UserData> cachedUsers = projectData.getTeam();
        if (cachedUsers != null) {
            return Single.just(cachedUsers);
        }

        Single<List<UserData>> dataSingle = dao.getAll(parentId, lazy).subscribeOn(Schedulers.io());

        getDisposables().add(
                dataSingle.observeOn(Schedulers.computation()).subscribe(
                        list -> {
//                            Log.e(TAG, ">> >> getAllUsers: project: " + parentId + ", size: " + list.size());
                            projectData.setTeam(list);
                            singleSubject.onSuccess(list);
                        }
                )
        );
        return singleSubject;
    }

    @Override
    public Single<List<UserData>> getMatching(String term, boolean lazy) {
        return dao.getMatching(term, lazy).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Optional<UserData>> get(long id, boolean lazy) {
        SingleSubject<Optional<UserData>> singleSubject = SingleSubject.create();
        Optional<UserData> cachedUserDataOptional = Optional.of(userDataCache.getData(id));
        final UserData userData = new UserData();
        if (cachedUserDataOptional.isPresent()) {
//            Log.i(TAG, "get: return cached user "  + cachedUserDataOptional.get());
            return Single.just(cachedUserDataOptional);
        } else {
            Single<Optional<UserData>> dataSingle = dao.get(id, lazy).subscribeOn(Schedulers.io());

            getDisposables().add(dataSingle
                    .observeOn(Schedulers.computation())
                    .subscribe(op -> {
                        if (op.isPresent()) {
                            userData.updateData(op.get());
                            userDataCache.putData(userData, false);
                        }
                        singleSubject.onSuccess(op);
                    }));

            return singleSubject;
        }
    }

    @Override
    public Single<Long> insert(UserData toInsert) {
        SingleSubject<Long> singleSubject = SingleSubject.create();
        getDisposables().add(
                dao.insert(toInsert).subscribeOn(Schedulers.io())
                        .subscribe(id -> {
                            toInsert.id = id;
                            userDataCache.putData(toInsert, true);
                            singleSubject.onSuccess(id);
                        }));
        return singleSubject;
    }

    @Override
    public Single<Boolean> update(UserData toUpdate) {
        SingleSubject<Boolean> singleSubject = SingleSubject.create();
        getDisposables().add(
                dao.update(toUpdate).subscribeOn(Schedulers.io())
                        .subscribe(updated -> {
                            userDataCache.putData(toUpdate, false);
                            singleSubject.onSuccess(updated);
                        })
        );
        return singleSubject;
    }

    @Override
    public Single<Boolean> delete(UserData toDelete) {
        SingleSubject<Boolean> singleSubject = SingleSubject.create();
        getDisposables().add(dao.delete(toDelete).subscribeOn(Schedulers.io())
                .subscribe(deleted -> {
                    userDataCache.removeData(toDelete);
                    singleSubject.onSuccess(deleted);
                }));
        return singleSubject;
    }
}
