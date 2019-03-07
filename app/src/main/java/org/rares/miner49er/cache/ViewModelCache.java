package org.rares.miner49er.cache;

import android.util.LruCache;
import androidx.annotation.CallSuper;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.EventBroadcaster;

import java.io.Closeable;

public class ViewModelCache implements EventBroadcaster, Disposable, Closeable {

    private LruCache<Long, ProjectData> projectsCache = null;
    private LruCache<Long, IssueData> issuesCache = null;
    private LruCache<Long, TimeEntryData> timeEntriesCache = null;
    private LruCache<Long, UserData> usersCache = null;

    private Cache<ProjectData> projectDataCache = null;
    private Cache<IssueData> projectIssueDataCache = null;
    private Cache<TimeEntryData> issueTimeEntryDataCache = null;
    private Cache<UserData> projectUserDataCache = null;

    private static final String TAG = ViewModelCache.class.getSimpleName();

    private ViewModelCache() {
    }

    private final static ViewModelCache INSTANCE = new ViewModelCache();

    public static ViewModelCache getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> Cache<T> getCache(Class<T> t) {
        if (ProjectData.class.equals(t)) {
            return (Cache<T>) getProjectDataCache();
        }
        if (IssueData.class.equals(t)) {
            return (Cache<T>) getProjectIssueDataCache();
        }
        if (TimeEntryData.class.equals(t)) {
            return (Cache<T>) getIssueTimeEntryDataCache();
        }
        if (UserData.class.equals(t)) {
            return (Cache<T>) getProjectUserDataCache();
        }

        throw new UnsupportedOperationException("No existing cache was found for " + t.getSimpleName() + ".");
    }

//    @Override
//    public void registerEventListener(Consumer<Byte> listener) {
//        checkDisposable().add(
//                cacheUpdateObservable
//                        .subscribeOn(Schedulers.computation())
////                        .throttleLatest(1, TimeUnit.SECONDS)
//                        .throttleLast(1, TimeUnit.SECONDS)
//                        .doOnNext((x) -> Log.i(TAG, "sendEvent"))
//                        .subscribe(listener));
//    }


    @Override
    public Flowable<Byte> getBroadcaster() {
        return cacheUpdateFlowable;
    }

    @Override
    public void sendEvent(Byte event) {
        if (cacheUpdatedProcessor.hasSubscribers()) {
            cacheUpdatedProcessor.onNext(event);
        }
    }

    @CallSuper
    public void clear() {
        usersCache.evictAll();
        projectsCache.evictAll();
        issuesCache.evictAll();
        timeEntriesCache.evictAll();
    }

    @Override
    public void close() {
        clear();
        dispose();
    }

    @Override
    public void dispose() {
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposables.isDisposed();
    }

    private CompositeDisposable checkDisposable() {
        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }
        return disposables;
    }

    LruCache<Long, ProjectData> getProjectsLruCache() {
        if (projectsCache == null) {
            projectsCache = new LruCache<>(100);
        }
        return projectsCache;
    }

    LruCache<Long, IssueData> getIssuesLruCache() {
        if (issuesCache == null) {
            issuesCache = new LruCache<>(2000);
        }
        return issuesCache;
    }

    LruCache<Long, TimeEntryData> getTimeEntriesLruCache() {
        if (timeEntriesCache == null) {
            timeEntriesCache = new LruCache<>(10000);
        }
        return timeEntriesCache;
    }

    LruCache<Long, UserData> getUsersLruCache() {
        if (usersCache == null) {
            usersCache = new LruCache<>(1000);
        }
        return usersCache;
    }


    private Cache<ProjectData> getProjectDataCache() {
        if (projectDataCache == null) {
            projectDataCache = new ProjectDataCache();
        }
        return projectDataCache;
    }

    private Cache<IssueData> getProjectIssueDataCache() {
        if (projectIssueDataCache == null) {
            projectIssueDataCache = new IssueDataCache();
        }
        return projectIssueDataCache;
    }

    private Cache<TimeEntryData> getIssueTimeEntryDataCache() {
        if (issueTimeEntryDataCache == null) {
            issueTimeEntryDataCache = new TimeEntryDataCache();
        }
        return issueTimeEntryDataCache;
    }

    private Cache<UserData> getProjectUserDataCache() {
        if (projectUserDataCache == null) {
            projectUserDataCache = new UserDataCache();
        }
        return projectUserDataCache;
    }

    private PublishProcessor<Byte> cacheUpdatedProcessor = PublishProcessor.create();
    private Flowable<Byte> cacheUpdateFlowable = cacheUpdatedProcessor
            .subscribeOn(Schedulers.computation())
            .onBackpressureDrop()
            .share();
    private CompositeDisposable disposables = new CompositeDisposable();
}