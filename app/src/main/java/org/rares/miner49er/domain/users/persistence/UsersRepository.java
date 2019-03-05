package org.rares.miner49er.domain.users.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import lombok.Setter;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er._abstract.UiEvent;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.EventBroadcaster;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.tables.UserTable;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsersRepository extends Repository<User> {

    private static final String TAG = UsersRepository.class.getSimpleName();

    private AsyncGenericDao<UserData> asyncDao = InMemoryCacheAdapterFactory.ofType(UserData.class);

    private Flowable<Changes> userTableObservable;
    private Query usersQuery;

    @Setter
    private int userId;

    UsersRepository() {
//        ns.registerUsersConsumer(this);
//        userTableObservable =
//                storio
//                        .observeChangesInTable(UserTable.NAME, BackpressureStrategy.LATEST)
//                        .subscribeOn(Schedulers.io());
        if (asyncDao instanceof EventBroadcaster) {
            ((EventBroadcaster) asyncDao).registerEventListener(o -> refreshData(true));
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
        disposables.dispose();
    }

    @Override
    protected boolean prepareEntities(List<User> entityList) {

        for (User i : entityList) {
            usersToAdd.put(i.getId(), i);
        }

        storio.put()
                .objects(usersToAdd.values())
                .withPutResolver(userPutResolver)
                .prepare()
                .executeAsBlocking();

        return false;
    }

    @Override
    protected void clearTables(StorIOSQLite.LowLevel ll) {
    }

    @Override
    public void registerSubscriber(Consumer<List> consumer) {

        disposables.add(
                userActionsObservable
                        .doOnNext((a) -> Log.i(TAG, "LOCAL ON NEXT"))
//                        .map(event -> getDbItems(usersQuery, User.class))
//                        .startWith(getDbItems(usersQuery, User.class))
//                        .map(list -> db2vm(list, true))
                        .map(o-> getData())
                        .startWith(getData())
                        .onBackpressureDrop()
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer)
        );

    }

    private List<UserData> getData(){
        return asyncDao.getAll(true).blockingGet();
    }

    @Override
    protected void refreshQuery() {
        usersQuery = Query.builder()
                .table(UserTable.NAME)
                .where(UserTable.ID_COLUMN + " = ? ")
                .whereArgs(userId)
                .build();
    }

    @Override
    public void refreshData(boolean onlyLocal) {
        userActionProcessor.onNext(UiEvent.TYPE_CLICK);
    }

    private List<UserData> db2vm(List<User> users, boolean local) {

        List<UserData> userDataList = new ArrayList<>();

        for (User user : users) {
            UserData converted = new UserData();

            converted.setId(user.getId());
            converted.setRole(user.getRole());
            converted.setName((local ? "" : "*") + user.getName());
            converted.setEmail(user.getEmail());
            converted.setPicture(user.getPhoto());
            converted.setApiKey(user.getApiKey());
            userDataList.add(converted);
        }

        return userDataList;
    }

    @Override
    protected final List<User> initializeFakeData() {
        List<User> dataList = new ArrayList<>();
        for (int i = 0; i < NumberUtils.getRandomInt(5, 30); i++) {
            User data = new User();
            data.setId(-1L);
            data.setName("User #" + i);
            dataList.add(data);
        }
        return dataList;
    }

}
