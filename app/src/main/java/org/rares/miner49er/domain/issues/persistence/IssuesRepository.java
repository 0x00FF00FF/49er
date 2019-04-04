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
import java.util.concurrent.TimeUnit;

import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_ISSUE;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_PROJECT;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ENTRY;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ISSUE;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_ISSUES;

public class IssuesRepository extends Repository {

    private static final String TAG = IssuesRepository.class.getSimpleName();

    private AsyncGenericDao<IssueData> asyncDao = InMemoryCacheAdapterFactory.ofType(IssueData.class);

    public IssuesRepository() {
//        ns.registerIssuesConsumer(this);
//        issueTableObservable =
//                storio
//                        .observeChangesInTable(IssueTable.NAME, BackpressureStrategy.LATEST)
//                        .subscribeOn(Schedulers.io());
        if (asyncDao instanceof EventBroadcaster) {
            disposables.add(
                    ((EventBroadcaster) asyncDao).getBroadcaster()
                            .onBackpressureLatest()
                            .filter(e -> CACHE_EVENT_UPDATE_ISSUES.equals(e) ||
                                    CACHE_EVENT_UPDATE_ISSUE.equals(e) ||
                                    CACHE_EVENT_REMOVE_ISSUE.equals(e) ||
                                    CACHE_EVENT_REMOVE_PROJECT.equals(e) ||
                                    CACHE_EVENT_UPDATE_ENTRY.equals(e)
                            )
                            .throttleLatest(1, TimeUnit.SECONDS)
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
            IssueData clone = new IssueData();
            clone.updateData(issueData);
            clone.id = issueData.id;
            clone.parentId = issueData.parentId;
            clone.lastUpdated = issueData.lastUpdated;
            clones.add(clone);
        }

        return clones;
    }

    @Override
    public void refreshData(boolean onlyLocal) {
//        Log.i(TAG, "refreshData: >>>>");
        userActionProcessor.onNext(UiEvent.TYPE_CLICK);
    }
}
