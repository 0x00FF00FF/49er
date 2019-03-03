package org.rares.miner49er.cache;

import android.util.Log;
import android.util.LruCache;
import androidx.annotation.CallSuper;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.entries.persistence.TimeEntryDataCache;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.persistence.IssueDataCache;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.persistence.ProjectDataCache;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.persistence.UserDataCache;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.persistence.dao.EventBroadcaster;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

public class SimpleCache implements EventBroadcaster, Disposable, Closeable {

    private LruCache<Long, ProjectData> projectsCache = null;
    private LruCache<Long, IssueData> issuesCache = null;
    private LruCache<Long, TimeEntryData> timeEntriesCache = null;
    private LruCache<Long, UserData> usersCache = null;

    private Cache<ProjectData> projectDataCache = null;
    private Cache<IssueData> projectIssueDataCache = null;
    private Cache<TimeEntryData> issueTimeEntryDataCache = null;
    private Cache<UserData> projectUserDataCache = null;

    private static final String TAG = SimpleCache.class.getSimpleName();

    private SimpleCache() {
    }

    private final static SimpleCache INSTANCE = new SimpleCache();

    public static SimpleCache getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractViewModel> Cache<T> getCache(Class<T> t) {
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

    @Override
    public void registerEventListener(Consumer<Object> listener) {
        checkDisposable().add(
                cacheUpdateObservable
                        .subscribeOn(Schedulers.computation())
//                        .throttleLatest(1, TimeUnit.SECONDS)
                        .throttleLast(1, TimeUnit.SECONDS)
                        .doOnNext((x) -> Log.i(TAG, "sendEvent"))
                        .subscribe(listener));
    }

    @Override
    public void sendEvent() {
        if (cacheUpdatedProcessor.hasSubscribers()) {
            cacheUpdatedProcessor.onNext(updateEvent);
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

    public LruCache<Long, ProjectData> getProjectsCache() {
        if (projectsCache == null) {
            projectsCache = new LruCache<>(100);
        }
        return projectsCache;
    }

    public LruCache<Long, IssueData> getIssuesCache() {
        if (issuesCache == null) {
            issuesCache = new LruCache<>(2000);
        }
        return issuesCache;
    }

    public LruCache<Long, TimeEntryData> getTimeEntriesCache() {
        if (timeEntriesCache == null) {
            timeEntriesCache = new LruCache<>(10000);
        }
        return timeEntriesCache;
    }

    public LruCache<Long, UserData> getUsersCache() {
        if (usersCache == null) {
            usersCache = new LruCache<>(1000);
        }
        return usersCache;
    }

    public Cache<ProjectData> getProjectDataCache() {
        if (projectDataCache == null) {
            projectDataCache = new ProjectDataCache();
        }
        return projectDataCache;
    }

    public Cache<IssueData> getProjectIssueDataCache() {
        if (projectIssueDataCache == null) {
            projectIssueDataCache = new IssueDataCache();
        }
        return projectIssueDataCache;
    }

    public Cache<TimeEntryData> getIssueTimeEntryDataCache() {
        if (issueTimeEntryDataCache == null) {
            issueTimeEntryDataCache = new TimeEntryDataCache();
        }
        return issueTimeEntryDataCache;
    }

    public Cache<UserData> getProjectUserDataCache() {
        if (projectUserDataCache == null) {
            projectUserDataCache = new UserDataCache();
        }
        return projectUserDataCache;
    }

    private PublishProcessor<Object> cacheUpdatedProcessor = PublishProcessor.create();
    private Flowable<Object> cacheUpdateObservable = cacheUpdatedProcessor
            .subscribeOn(Schedulers.computation())
            .onBackpressureDrop()
            .share();
    private CompositeDisposable disposables = new CompositeDisposable();
    private final Object updateEvent = new Object();
}
