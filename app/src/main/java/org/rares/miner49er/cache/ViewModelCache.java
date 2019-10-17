package org.rares.miner49er.cache;

import android.util.Log;
import android.util.LruCache;
import androidx.annotation.CallSuper;
import io.reactivex.BackpressureOverflowStrategy;
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
import java.util.concurrent.TimeUnit;

public class ViewModelCache implements EventBroadcaster, Disposable, Closeable {

    public long lastUpdateTime = -1;

    public UserData loggedInUser;

    protected ViewModelCache() {
        cacheUpdatedProcessor = PublishProcessor.create();
        cacheUpdateFlowable = createCacheUpdateFlowable(true);
    }

    protected ViewModelCache(boolean throttleEvents) {
        cacheUpdatedProcessor = PublishProcessor.create();
        cacheUpdateFlowable = createCacheUpdateFlowable(throttleEvents);
    }

    private Flowable<Byte> createCacheUpdateFlowable(boolean throttleEvents) {
        if (throttleEvents) {
            return
                    cacheUpdatedProcessor
                            .subscribeOn(Schedulers.computation())
                            .onBackpressureBuffer(
                                    128,
                                    () -> Log.w(TAG, "[ CACHE UPDATE OVERFLOW ]"),
                                    BackpressureOverflowStrategy.DROP_OLDEST)
                            .window(50, TimeUnit.MILLISECONDS)
                            .flatMap(Flowable::distinct)
//                            .map(b -> {
////                                Log.i(TAG, "cache update event: \t\t" + b);
//                                System.out.println("[" + System.currentTimeMillis() + "] cache update event: \t\t" + getProjectDataCache().translate(b) + " " + Thread.currentThread().getName());
//                                return b;
//                            })
                            .share();
        } else {
            return
                    cacheUpdatedProcessor
                            .subscribeOn(Schedulers.computation())
                            .onBackpressureBuffer(128,
                                    () -> Log.w(TAG, "[ CACHE UPDATE OVERFLOW ]"),
//                                    () -> System.out.println("[ CACHE UPDATE OVERFLOW ]"),
                                    BackpressureOverflowStrategy.DROP_OLDEST)
//                            .map(b -> {
////                                Log.i(TAG, "cache update event: \t\t" + b);
//                                System.out.println("[" + System.currentTimeMillis() + "] cache update event: \t\t" + getProjectDataCache().translate(b) + " " + Thread.currentThread().getName());
//                                return b;
//                            })
                            .share();
        }
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


    @Override
    public Flowable<Byte> getBroadcaster() {
        return cacheUpdateFlowable;
    }

    @Override
    public void sendEvent(Byte event) {
//        if (cacheUpdatedProcessor.hasSubscribers()) {
        cacheUpdatedProcessor.onNext(event);
//        System.out.println("_cache_ event: " + getProjectDataCache().translate(event) + " " + Thread.currentThread().getName());
//        if (!cacheUpdatedProcessor.offer(event)) {
//            Log.w(TAG, "sendEvent: CACHE EVENT OFFER FAILED " + event);
//         wait some time, then retry....
//        }
//        }
    }

    @CallSuper
    public void clear() {
        if (usersCache != null) {
            usersCache.evictAll();
        }
        if (projectsCache != null) {
            projectsCache.evictAll();
        }
        if (issuesCache != null) {
            issuesCache.evictAll();
        }
        if (timeEntriesCache != null) {
            timeEntriesCache.evictAll();
        }

        // FIXME: 28.03.2019
        // * unfortunately it's not enough to clear this,
        // * we need to also clear every reference to
        // * these and references to all the cached objects
        // * (in other objects)
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

//    private CompositeDisposable checkDisposable() {
//        if (disposables.isDisposed()) {
//            disposables = new CompositeDisposable();
//        }
//        return disposables;
//    }

    /**
     * This method should be used to increase the cache size. <br />
     * It only sets a bigger value and creates a new LruCache
     * (containing all previously stored items). <br />
     *
     * @param newSize the new size of the cache
     */
    LruCache<Long, ProjectData> increaseProjectsCacheSize(int newSize) {
        if (newSize > getProjectsLruCache().maxSize()) {
            LruCache<Long, ProjectData> newCache = new LruCache<>(newSize);
            for (Long key : projectsCache.snapshot().keySet()) {
                newCache.put(key, projectsCache.get(key));
            }
            projectsCache = newCache;
        }
        return projectsCache;
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
            timeEntriesCache = new LruCache<>(30000);
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
            projectDataCache = new ProjectDataCache(this);
        }
        return projectDataCache;
    }

    private Cache<IssueData> getProjectIssueDataCache() {
        if (projectIssueDataCache == null) {
            projectIssueDataCache = new IssueDataCache(this);
        }
        return projectIssueDataCache;
    }

    private Cache<TimeEntryData> getIssueTimeEntryDataCache() {
        if (issueTimeEntryDataCache == null) {
            issueTimeEntryDataCache = new TimeEntryDataCache(this);
        }
        return issueTimeEntryDataCache;
    }

    private Cache<UserData> getProjectUserDataCache() {
        if (projectUserDataCache == null) {
            projectUserDataCache = new UserDataCache(this);
        }
        return projectUserDataCache;
    }

    private LruCache<Long, ProjectData> projectsCache = null;
    private LruCache<Long, IssueData> issuesCache = null;
    private LruCache<Long, TimeEntryData> timeEntriesCache = null;
    private LruCache<Long, UserData> usersCache = null;

    private Cache<ProjectData> projectDataCache = null;
    private Cache<IssueData> projectIssueDataCache = null;
    private Cache<TimeEntryData> issueTimeEntryDataCache = null;
    private Cache<UserData> projectUserDataCache = null;

    private static final String TAG = ViewModelCache.class.getSimpleName();

    private PublishProcessor<Byte> cacheUpdatedProcessor;
    private Flowable<Byte> cacheUpdateFlowable;
    private CompositeDisposable disposables = new CompositeDisposable();

    public void dumpCaches(){
        Log.e(TAG, "dumpCaches: -------------------------------- (start)");
        Log.i(TAG, "dumpCaches: projects:");
        Log.v(TAG, "dumpCaches: " + getProjectsLruCache().toString());
        Log.v(TAG, "dumpCaches: " + getProjectsLruCache().snapshot().toString());
        Log.i(TAG, "dumpCaches: users:");
        Log.v(TAG, "dumpCaches: " + getUsersLruCache().toString());
        Log.v(TAG, "dumpCaches: " + getUsersLruCache().snapshot().toString());
        Log.i(TAG, "dumpCaches: issues:");
        Log.v(TAG, "dumpCaches: " + getIssuesLruCache().toString());
        Log.v(TAG, "dumpCaches: " + getIssuesLruCache().snapshot().toString());
        Log.i(TAG, "dumpCaches: time entries:");
        Log.v(TAG, "dumpCaches: " + getTimeEntriesLruCache().toString());
        Log.v(TAG, "dumpCaches: " + getTimeEntriesLruCache().snapshot().toString());
        Log.e(TAG, "dumpCaches: -------------------------------- (end)");
    }
}
