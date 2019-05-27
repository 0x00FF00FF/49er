package org.rares.miner49er.cache;

import android.util.Log;
import android.util.LruCache;
import androidx.annotation.CallSuper;
import com.pushtorefresh.storio3.Optional;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ViewModelCache implements EventBroadcaster, Disposable, Closeable {

    public long lastUpdateTime = -1;

    public UserData loggedInUser;

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
//        if (cacheUpdatedProcessor.hasSubscribers()) {
        cacheUpdatedProcessor.onNext(event);
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

    private CompositeDisposable checkDisposable() {
        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }
        return disposables;
    }

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

    public void dump() {
        for (ProjectData pd : projectDataCache.getData(Optional.of(null))) {
            List<UserData> team = pd.getTeam();
            Log.i(TAG, "dump: " + pd.getName() + "/" + (team == null ? "null" : team.size()));
        }
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

    private PublishProcessor<Byte> cacheUpdatedProcessor = PublishProcessor.create();
    private Flowable<Byte> cacheUpdateFlowable =
            cacheUpdatedProcessor
                    .window(50, TimeUnit.MILLISECONDS)
                    .flatMap(Flowable::distinct)
//                    .map(b -> {
//                        Log.i(TAG, "cache update event: \t\t" + b);
//                        return b;
//                    })
                    .onBackpressureBuffer(
                            128,
                            () -> Log.w(TAG, "[ CACHE UPDATE OVERFLOW ]"),
                            BackpressureOverflowStrategy.DROP_OLDEST)
                    .subscribeOn(Schedulers.computation())
                    .onBackpressureBuffer()
                    .share();
    private CompositeDisposable disposables = new CompositeDisposable();
}
