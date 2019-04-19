package org.rares.miner49er.domain.users.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_USER;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_USER;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_USERS;

public class UsersRepository extends Repository {

    private static final String TAG = UsersRepository.class.getSimpleName();

    private AsyncGenericDao<UserData> asyncDao = InMemoryCacheAdapterFactory.ofType(UserData.class);

    private Flowable<Changes> userTableObservable;

    @Setter
    private int userId;

    UsersRepository() {
//        ns.registerUsersConsumer(this);
//        userTableObservable =
//                storio
//                        .observeChangesInTable(UserTable.NAME, BackpressureStrategy.LATEST)
//                        .subscribeOn(Schedulers.io());
    }

    @Override
    public void setup() {

        if (disposables == null || disposables.isDisposed()) {
            disposables = new CompositeDisposable();
            if (asyncDao instanceof EventBroadcaster) {
                disposables.add(
                        ((EventBroadcaster) asyncDao).getBroadcaster()
                                .onBackpressureLatest()
                                .filter(e -> e.equals(CACHE_EVENT_UPDATE_USER) ||
                                        e.equals(CACHE_EVENT_UPDATE_USERS) ||
                                        e.equals(CACHE_EVENT_REMOVE_USER))
                                .throttleLatest(1, TimeUnit.SECONDS)
                                .subscribe(o -> refreshData(true)));
            }
        }

    }

    @Override
    public void shutdown() {
        disposables.dispose();
    }

    @Override
    public void registerSubscriber(Consumer<List> consumer) {

        disposables.add(
                userActionsObservable
                        .doOnNext((a) -> Log.i(TAG, "LOCAL ON NEXT"))
//                        .map(event -> getDbItems(usersQuery, User.class))
//                        .startWith(getDbItems(usersQuery, User.class))
//                        .map(list -> db2vm(list, true))
                        .map(o -> getData())
                        .startWith(getData())
                        .onBackpressureDrop()
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer)
        );

    }

    private List<UserData> getData() {
        return asyncDao.getAll(true).blockingGet();
    }

    @Override
    public void refreshData(boolean onlyLocal) {
        userActionProcessor.onNext(UiEvent.TYPE_CLICK);
    }

}
