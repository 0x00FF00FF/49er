package org.rares.miner49er.domain.users.persistence;

import android.util.Log;
import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.cache.AbstractAsyncCacheAdapter;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.AsyncGenericDaoFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncUserDataCacheAdapter
        extends AbstractAsyncCacheAdapter
        implements AsyncGenericDao<UserData> {

    private AsyncGenericDao<UserData> dao = AsyncGenericDaoFactory.ofType(UserData.class);
    private final Cache<UserData> userDataCache = cache.getCache(UserData.class);
    private final LruCache<Long, ProjectData> projectDataCache = cache.getProjectsCache();

    public static final String TAG = AsyncUserDataCacheAdapter.class.getSimpleName();

    @Override
    public Single<List<UserData>> getAll(boolean lazy) {
        List<UserData> cachedUsers = new ArrayList<>(cache.getUsersCache().snapshot().values());
        final List<UserData> dbUsers = Collections.emptyList();

        Single<List<UserData>> dataSingle = dao.getAll(lazy).subscribeOn(Schedulers.io());
        getDisposables().add(
                dataSingle.observeOn(Schedulers.computation()).subscribe(
                        list -> {
                            Log.e(TAG, ">> >> getAllUsers: cache: " + cachedUsers.size() + ", db: " + list.size());
                            userDataCache.putData(list, false);
                        }
                ));
        return Single.just(dbUsers);
    }

    @Override
    public Single<List<UserData>> getAll(long parentId, boolean lazy) {
        ProjectData projectData = projectDataCache.get(parentId);
        List<UserData> cachedUsers = projectData.getTeam();
        if (cachedUsers != null) {
            return Single.just(cachedUsers);
        }

        Single<List<UserData>> dataSingle = dao.getAll(parentId, lazy).subscribeOn(Schedulers.io());

        getDisposables().add(
                dataSingle.observeOn(Schedulers.computation()).subscribe(
                        list -> {
                            Log.e(TAG, ">> >> getAllUsers: project: " + parentId + ", size: " + list.size());
                            projectData.setTeam(list);
//                            userDataCache.putData(Optional.of(projectData), list);
                        }
                )
        );
        return null;
    }

    @Override
    public Single<List<UserData>> getMatching(String term, boolean lazy) {
        return dao.getMatching(term, lazy).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Optional<UserData>> get(long id, boolean lazy) {
        Optional<UserData> cachedUserDataOptional = Optional.of(cache.getUsersCache().get(id));
        final UserData userData = new UserData();
        if (cachedUserDataOptional.isPresent()) {
            return Single.just(cachedUserDataOptional);
        } else {
            Single<Optional<UserData>> dataSingle = dao.get(id, lazy).subscribeOn(Schedulers.io());

            getDisposables().add(dataSingle
                    .observeOn(Schedulers.computation())
                    .subscribe(timeEntryDataOptional -> {
                        if (timeEntryDataOptional.isPresent()) {
                            userData.updateData(timeEntryDataOptional.get());
                            userDataCache.putData(userData, false);
                        }
                    }));

            return Single.just(Optional.of(userData));
        }
    }

    @Override
    public Single<Long> insert(UserData toInsert) {
        userDataCache.putData(toInsert, true);
        return dao.insert(toInsert).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> update(UserData toUpdate) {
        userDataCache.putData(toUpdate, false);
        return dao.update(toUpdate).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> delete(UserData toDelete) {
        userDataCache.removeData(toDelete);
        return dao.delete(toDelete).subscribeOn(Schedulers.io());
    }
}
