package org.rares.miner49er.persistence.dao;

import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import com.pushtorefresh.storio3.sqlite.queries.RawQuery;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.UserStorIOSQLiteGetResolver;
import org.rares.miner49er.persistence.storio.tables.UserProjectTable;
import org.rares.miner49er.persistence.storio.tables.UserTable;

import java.util.List;
import java.util.Map;

public class UsersDao implements GenericEntityDao<User> {

    private UserStorIOSQLiteGetResolver userGetResolver = StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver();
    private StorIOSQLite storio = StorioFactory.INSTANCE.get();

    @Override
    public Single<List<User>> getAll() {
        return storio.get()
                .listOfObjects(User.class)
                .withQuery(Query.builder().table(UserTable.NAME).build())
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<User>> getAll(long projectId) {
        return storio.get()
                .listOfObjects(User.class)
                .withQuery(RawQuery.builder().query(
                        "SELECT " + UserTable.NAME + ".* FROM " + UserTable.NAME + ", " + UserProjectTable.NAME +
                                " WHERE " + UserProjectTable.PROJECT_ID_COLUMN + " = ?").args(projectId).build())
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<User>> getMatching(String term) {
        return userGetResolver.getMatchingNameAsync(storio, term)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Optional<User>> get(long id) {
        return userGetResolver.getByIdAsync(storio, id)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> insert(User toInsert) {
        return storio.put()
                .object(toInsert)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(putResult -> {
                    toInsert.setId(putResult.insertedId());
                    return putResult.wasInserted();
                });
    }

    @Override
    public Single<Boolean> update(User toUpdate) {
        return storio.put()
                .object(toUpdate)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::wasUpdated);
    }

    @Override
    public Single<Boolean> insert(List<User> toInsert) {
        return storio.put()
                .objects(toInsert)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(putResult -> putResult.numberOfInserts() == toInsert.size());
    }

    @Override
    public Single<Boolean> update(List<User> toUpdate) {
        return storio.put()
                .objects(toUpdate)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(putResult -> putResult.numberOfUpdates() + putResult.numberOfInserts() == toUpdate.size());
    }

    @Override
    public Single<Boolean> delete(User toDelete) {
        return storio.delete()
                .object(toDelete)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(deleteResult -> deleteResult.numberOfRowsDeleted() == 1);
    }

    @Override
    public Single<Boolean> delete(List<User> toDelete) {
        return storio.delete()
                .objects(toDelete)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(deleteResult -> deleteResult.results().size() == toDelete.size());
    }

    @Override
    public Single<User> insertWithResult(User toInsert) {
        return storio.put()
                .object(toInsert)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(putResult -> {
                    toInsert.setId(putResult.insertedId());
                    return toInsert;
                });
    }

    @Override
    public Flowable<User> insertWithResult(List<User> toInsert) {
        return storio.put()
                .objects(toInsert)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .flatMapPublisher(putResult -> {
                    Map<User, PutResult> resultMap = putResult.results();
                    return Flowable
                            .fromArray(resultMap.keySet().toArray(new User[0]))
                            .map(u -> {
                                PutResult result = resultMap.get(u);
                                if (result != null && result.insertedId() != null) {
                                    u.setId(result.insertedId());
                                }
                                return u;
                            });
                })
                .map(user -> {
//                    System.out.println("user after saving: " + user);
                    return user;
                })
                .doOnError(Throwable::printStackTrace);
    }

  public Flowable<User> getByObjectIdIn(List<String> objectIds) {
//        Log.d("TAG", Thread.currentThread().getName() + " getByObjectIdIn() called with: objectIds = [" + objectIds + "]");
    return userGetResolver.getByObjectIdInAsync(storio, objectIds)
/*                .flatMap(u -> {
//                    System.out.println("user by oids: " + u);
                    return Flowable.just(u);
                })*/
        ;
  }

    /*    @Override
    public Single<Boolean> wipe() {
        return storio.delete()
                .byQuery(DeleteQuery.builder().table(UserTable.NAME).build())
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.affectedTables().size() > 0);
    }*/

    @Getter
    private final static UsersDao instance = new UsersDao();

    private UsersDao() {
    }
}
