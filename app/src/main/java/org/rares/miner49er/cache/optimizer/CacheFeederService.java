package org.rares.miner49er.cache.optimizer;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.Nullable;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er._abstract.NetworkingService;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_PROJECTS;

public class CacheFeederService extends IntentService /*implements EntityOptimizer.DbUpdateFinishedListener*/ {

    //    private volatile boolean working = false;
    private NetworkingService ns = NetworkingService.INSTANCE;

    private final ViewModelCache cache = ViewModelCache.getInstance();
    private final Collection<ProjectData> cachedProjects = new ArrayList<>();
    private final Collection<IssueData> cachedIssues = new ArrayList<>();
    private final Collection<TimeEntryData> cachedTimeEntries = new ArrayList<>();

    private final AsyncGenericDao<UserData> uDao = InMemoryCacheAdapterFactory.ofType(UserData.class);

    private final Cache<ProjectData> projectDataCache = cache.getCache(ProjectData.class);
    private final Cache<IssueData> issueDataCache = cache.getCache(IssueData.class);
    private final Cache<TimeEntryData> timeEntryDataCache = cache.getCache(TimeEntryData.class);
    private final Cache<UserData> userDataCache = cache.getCache(UserData.class);


    private final String TAG = CacheFeederService.class.getSimpleName();

    private CompositeDisposable disposables = null;

    public CacheFeederService() {
        super(CacheFeederService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        disposables = new CompositeDisposable();
    }

    @Override
    public void onDestroy() {
//        disposables.dispose();
//        not disposing, as we want to have the work done
        super.onDestroy();
    }

    public void enqueueCacheFill() {

        cache.lastUpdateTime = System.currentTimeMillis();

        final PublishProcessor<Integer> progressProcessor = PublishProcessor.create();

        final AsyncGenericDao<ProjectData> pDao = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        final AsyncGenericDao<IssueData> iDao = InMemoryCacheAdapterFactory.ofType(IssueData.class);
        final AsyncGenericDao<TimeEntryData> tDao = InMemoryCacheAdapterFactory.ofType(TimeEntryData.class);

        List<AsyncGenericDao<? extends AbstractViewModel>> daoList = new ArrayList<>();
        daoList.add(uDao);
        daoList.add(pDao);
        daoList.add(iDao);
        daoList.add(tDao);

        // if this is registered _AFTER_ the following disposables.add call,
        // it may miss its window of opportunity and link data is never called.
        disposables.add(progressProcessor
                .limit(daoList.size())
                .count()
                .subscribe(x -> {
                    Optional<UserData> userDataOptional = uDao.get(12, true).blockingGet();
                    if (userDataOptional.isPresent()) {
                        cache.loggedInUser = userDataOptional.get();   ///
                    }
                    cache.sendEvent(CACHE_EVENT_UPDATE_PROJECTS);
                    linkData();
                })
        );

        disposables.add(
                Flowable.fromIterable(daoList)
                        .parallel(4)
//                        .runOn(Schedulers.computation())
                        .map(dao -> {
                            disposables.add(dao.getAll(true).subscribe(x -> progressProcessor.onNext(0)));
                            return dao;
                        })
                        .sequential()
                        .toList()
                        .subscribe());
    }

    private void linkData() {

        Log.v(TAG, "linkData: ------------------------ start " + Thread.currentThread().getName());
        cachedProjects.addAll(projectDataCache.getData(Optional.of(null)));
        cachedIssues.addAll(issueDataCache.getData(Optional.of(null)));
        cachedTimeEntries.addAll(timeEntryDataCache.getData(Optional.of(null)));

        if (cachedProjects.size() == 0) {
            Log.i(TAG, "linkData: ------------------------ no data ");
            ns.refreshData();
            stopSelf();
            return;
        }

        disposables.add(
                Flowable.fromIterable(cachedTimeEntries)
//                            .subscribeOn(Schedulers.computation())
                        .parallel(4)
                        .runOn(Schedulers.computation())
                        .map(mapTimeEntryToIssueData)
                        .sequential()
                        // in this case doOnComplete should be
                        // faster and more memory efficient
                        // than adding toList + subscribe
                        .doOnComplete(() -> {
                            Log.v(TAG, "linkData: start work on issues");
                            Flowable.fromIterable(cachedIssues)
                                    .parallel(4)
                                    .runOn(Schedulers.computation())
                                    .map(mapIssueToProjectData)
                                    .sequential()
                                    .doOnComplete(() -> {
                                        Log.v(TAG, "linkData: start work on projects");
                                        Flowable.fromIterable(cachedProjects)
                                                .parallel(4)
                                                .runOn(Schedulers.computation())
                                                .map(mapToProjectData)
                                                .sequential()
                                                .delay(51, TimeUnit.MILLISECONDS)
                                                .doOnComplete(projectsLinkedAction)
                                                .subscribe();
                                    })
                                    .subscribe();
                        })
                        .subscribe()
        );
    }

    private Action projectsLinkedAction = () -> {
        Log.v(TAG, "linkData: ------------------------ end adding teams ");
        cache.lastUpdateTime = System.currentTimeMillis();
        cache.sendEvent(CACHE_EVENT_UPDATE_PROJECTS);
    };

    private Function<ProjectData, ProjectData> mapToProjectData =
            projectData -> {
                uDao.getAll(projectData.getId(), true);
                UserData userData = userDataCache.getData(projectData.parentId);
                projectData.setOwner(userData);
                if (projectData.getTeam() == null) {
                    projectData.setTeam(Collections.emptyList());
                }
                if (projectData.getIssues() == null) {
                    projectData.setIssues(Collections.emptyList());
                }
                return projectData;
            };

    private Function<IssueData, IssueData> mapIssueToProjectData =
            issueData -> {
                if (issueData.getOwner() == null) {
                    UserData owner = userDataCache.getData(issueData.getOwnerId());
                    issueData.setOwner(owner);
                }
                synchronized (projectDataCache.getData(issueData.parentId)) {
                    ProjectData projectData = projectDataCache.getData(issueData.parentId);
                    List<IssueData> idList = projectData.getIssues();
                    if (idList != null) {
                        boolean found = false;
                        for (IssueData iData : idList) {
                            if (iData.id.equals(issueData.id)) {
                                iData.updateData(issueData);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            idList.add(issueData);
                        }
                    } else {
                        idList = new ArrayList<>();
                        idList.add(issueData);
                        projectData.setIssues(idList);
                    }
                }
                return issueData;
            };

    private Function<TimeEntryData, TimeEntryData> mapTimeEntryToIssueData =
            timeEntryData -> {
                if (timeEntryData.getUserName() == null || timeEntryData.getUserPhoto() == null) {
                    UserData userData = userDataCache.getData(timeEntryData.getUserId());
                    if (userData != null) {
                        timeEntryData.setUserName(userData.getName());
                        timeEntryData.setUserPhoto(userData.getPicture());
                    }
                }

                synchronized (issueDataCache.getData(timeEntryData.parentId)) {
                    IssueData issueData = issueDataCache.getData(timeEntryData.parentId);
                    List<TimeEntryData> teList = issueData.getTimeEntries();
                    if (teList != null) {
                        boolean found = false;
                        for (TimeEntryData ted : teList) {
                            // if the data is already there and a cache update is called
                            // and this time entry was modified elsewhere, update the data
                            if (ted.id.equals(timeEntryData.id)) {
                                ted.updateData(timeEntryData);
                                found = true;
                                break;
                            }
                        }
                        // only add the data if it is not found in the cache
                        if (!found) {
                            teList.add(timeEntryData);
                        }
                    } else {
                        teList = new ArrayList<>();
                        teList.add(timeEntryData);
                        issueData.setTimeEntries(teList);
                    }
                }
                return timeEntryData;
            };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        enqueueCacheFill();
    }

}
