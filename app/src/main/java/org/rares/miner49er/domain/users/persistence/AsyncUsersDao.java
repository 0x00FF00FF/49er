package org.rares.miner49er.domain.users.persistence;

import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.UserStorIOSQLiteGetResolver;
import org.rares.miner49er.persistence.storio.tables.UserTable;

import java.util.List;

public class AsyncUsersDao implements AsyncGenericDao<UserData> {

    public static AsyncGenericDao<UserData> getInstance() {
        return INSTANCE;
    }

    @Override
    public Single<List<UserData>> getAll(boolean lazy) {
        return eagerResolver.getAllAsync(storio)
                .subscribeOn(Schedulers.io())
                .map(daoConverter::dmToVm);
    }

    @Override
    public Single<List<UserData>> getAll(long projectId, boolean lazy) {
        return eagerResolver.getAllAsync(storio, projectId)
                .subscribeOn(Schedulers.io())
                .map(daoConverter::dmToVm);
    }

    @Override
    public Single<List<UserData>> getMatching(String term, Optional<Long> parentId, boolean lazy) {
        return eagerResolver
                .getMatchingNameAsync(storio, term)
                .subscribeOn(Schedulers.io())
                .map(daoConverter::dmToVm);
    }

    @Override
    public Single<Optional<UserData>> get(long id, boolean lazy) {
        return eagerResolver.getByIdAsync(storio, id)
                .subscribeOn(Schedulers.io())
                .map(projectOptional ->
                        projectOptional.isPresent() ?
                                Optional.of(daoConverter.dmToVm(projectOptional.get())) :
                                Optional.of(null));
    }

    @Override
    public Single<Long> insert(UserData toInsert) {
        assertInsertReady(toInsert);
        return storio.put()
                .object(daoConverter.vmToDm(toInsert))
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::insertedId);
    }

    @Override
    public Single<Boolean> update(UserData toUpdate) {
        assertUpdateReady(toUpdate);
        return storio.put()
                .object(daoConverter.vmToDm(toUpdate))
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::wasUpdated);
    }

    @Override
    public Single<Boolean> delete(UserData toDelete) {
        assertDeleteReady(toDelete);
        return storio.delete()
                .object(daoConverter.vmToDm(toDelete))
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(dr -> dr.numberOfRowsDeleted() == 0);
    }

    private AsyncUsersDao() {
    }

    private static AsyncUsersDao INSTANCE = new AsyncUsersDao();

    private StorIOSQLite storio = StorioFactory.INSTANCE.get();
    private UserStorIOSQLiteGetResolver eagerResolver = StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver();
    private DaoConverter<User, UserData> daoConverter = DaoConverterFactory.of(User.class, UserData.class);
    @Getter
    private final Flowable<Changes> dbChangesFlowable = storio.observeChangesInTable(UserTable.NAME, BackpressureStrategy.LATEST);
}
