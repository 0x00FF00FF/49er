package org.rares.miner49er.domain.issues.persistence;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er._abstract.UiEvent;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.EventBroadcaster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IssuesRepository extends Repository {

    private static final String TAG = IssuesRepository.class.getSimpleName();

    private AsyncGenericDao<IssueData> asyncDao = InMemoryCacheAdapterFactory.ofType(IssueData.class);

    public IssuesRepository() {
//        ns.registerIssuesConsumer(this);
//        issueTableObservable =
//                storio
//                        .observeChangesInTable(IssueTable.NAME, BackpressureStrategy.LATEST)
//                        .subscribeOn(Schedulers.io());
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
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_PROJECTS");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_ISSUES)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ISSUES  ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_ENTRIES)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ENTRIES ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_USERS)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_USERS   ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_PROJECT)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_PROJECT ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_ISSUE)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ISSUE   ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_ENTRY)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_ENTRY   ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_UPDATE_USER)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_UPDATE_USER    ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_REMOVE_PROJECT)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_REMOVE_PROJECT ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_REMOVE_ISSUE)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_REMOVE_ISSUE   ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_REMOVE_ENTRY)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_REMOVE_ENTRY   ");
//                                    }
//                                    if (b.equals(CACHE_EVENT_REMOVE_USER)) {
//                                        Log.i(TAG, "setup: <<<< CACHE_EVENT_REMOVE_USER    ");
//                                    }
//                                })
//                                .filter(e -> CACHE_EVENT_UPDATE_ISSUES.equals(e) ||
//                                        CACHE_EVENT_UPDATE_ISSUE.equals(e) ||
//                                        CACHE_EVENT_REMOVE_ISSUE.equals(e) ||
//                                        CACHE_EVENT_REMOVE_PROJECT.equals(e) ||
//                                        CACHE_EVENT_UPDATE_ENTRY.equals(e)
//                                )
//                                .throttleLatest(1, TimeUnit.SECONDS)
                                .subscribe(o -> refreshData(true)));
            }
        }
    }

    @Override
    public void shutdown() {
        userActionProcessor.onComplete();
        disposables.dispose();
    }


    @Override
    public void registerSubscriber(Consumer<List> consumer) {
        disposables.add(
                userActionsObservable
//                        .doOnNext((a) -> Log.i(TAG, "LOCAL ON NEXT"))
                        .map(event -> {
//                            Log.w(TAG, "registerSubscriber: MAP");
                            return getDbItems();
                        })
                        .startWith(getDbItems())
                        .onBackpressureDrop()
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer)
        );
    }

    private List<IssueData> getDbItems() {
        List<IssueData> issueDataList = asyncDao.getAll(parentProperties.getId(), true).blockingGet();
        List<IssueData> clones = new ArrayList<>();
        for (IssueData issueData : issueDataList) {
            if (!issueData.deleted) {
                IssueData cloned = issueData.clone(false);
                clones.add(cloned);
            }
        }

        return clones;
    }

    @Override
    public void refreshData(boolean onlyLocal) {
//        Log.i(TAG, "refreshData: >>>>");
        userActionProcessor.onNext(UiEvent.TYPE_CLICK);
    }
}
