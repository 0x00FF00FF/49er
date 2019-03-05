package org.rares.miner49er.cache.optimizer;

import android.util.Log;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CacheFeeder {

    public Disposable enqueueCacheFill() {
        final CompositeDisposable disposables = new CompositeDisposable();

        if (cache.getCache(ProjectData.class).getData(Optional.of(null)).size() != 0) {     // TODO: 3/4/19
            return null;
        }

        final PublishProcessor<Integer> progressProcessor = PublishProcessor.create();

        final AsyncGenericDao<ProjectData> pDao = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        final AsyncGenericDao<IssueData> iDao = InMemoryCacheAdapterFactory.ofType(IssueData.class);
        final AsyncGenericDao<TimeEntryData> tDao = InMemoryCacheAdapterFactory.ofType(TimeEntryData.class);

        List<AsyncGenericDao> daoList = new ArrayList<>();
        daoList.add(uDao);
        daoList.add(pDao);
        daoList.add(iDao);
        daoList.add(tDao);


        disposables.add(
                Flowable.fromIterable(daoList)
                        .parallel(4)
                        .runOn(Schedulers.computation())
                        .map(dao -> {
                            disposables.add(dao.getAll(true).subscribe(x -> progressProcessor.onNext(0)));
                            return dao;
                        })
                        .sequential()
                        .subscribe());

        disposables.add(progressProcessor
                .limit(daoList.size())
                .count()
                .subscribe(x -> linkData())
        );

        return disposables;
    }

    private Disposable linkData() {
        CompositeDisposable disposables = new CompositeDisposable();

        Log.v(TAG, "linkData: ------------------------ start " + Thread.currentThread().getName());
        cachedProjects.addAll(projectDataCache.getData(Optional.of(null)));
        cachedIssues.addAll(issueDataCache.getData(Optional.of(null)));
        cachedTimeEntries.addAll(timeEntryDataCache.getData(Optional.of(null)));


        disposables.add(
                Flowable.fromIterable(cachedTimeEntries)
                        .parallel(4)
                        .runOn(Schedulers.computation())
                        .map(timeEntryData -> {
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
//                                Log.i(TAG, String.format("linkData te: [%s][%s/%s]\t\t[%s]",
//                                        timeEntryData.id,
//                                        issueData.id,
//                                        teList.size(),
//                                        Thread.currentThread().getName()));
                            }
//                            timeEntryData.lastUpdated = System.currentTimeMillis();
                            return timeEntryData;
                        })
                        .sequential()
                        .doOnComplete(() -> {
                            Log.v(TAG, "linkData: start work on issues");
                            Flowable.fromIterable(cachedIssues)
                                    .parallel(4)
                                    .runOn(Schedulers.computation())
                                    .map(issueData -> {
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
//                                            Log.i(TAG, String.format("linkData i: [id:%s][pid:%s/i#%s][te#%s]\t\t[%s]",
//                                                    issueData.id,
//                                                    projectData.id,
//                                                    idList.size(),
//                                                    issueData.getTimeEntries().size(),
//                                                    Thread.currentThread().getName()
//                                            ));
                                        }
//                                        issueData.lastUpdated = System.currentTimeMillis();
                                        return issueData;
                                    })
                                    .sequential()
                                    .doOnComplete(() -> {
                                        Log.v(TAG, "linkData: start work on projects");
                                        Flowable.fromIterable(cachedProjects)
                                                .parallel(4)
                                                .runOn(Schedulers.computation())
                                                .map(
                                                        projectData -> {
//                                                            Log.i(TAG, String.format("linkData p: [id:%s][i#%s]\t\t[%s]",
//                                                                    projectData.id,
//                                                                    projectData.getIssues() == null ? 0 : projectData.getIssues().size(),
//                                                                    Thread.currentThread().getName()
//                                                            ));
                                                            uDao.getAll(projectData.getId(), true);
                                                            projectData.setOwner(userDataCache.getData(projectData.parentId));
//                                                            projectData.lastUpdated = System.currentTimeMillis();
                                                            return projectData;
                                                        })
                                                .sequential()
                                                .doOnComplete(() -> {
                                                    Log.w(TAG, "linkData: ------------------------ end adding team " + disposables.size());
                                                    cache.sendEvent();
                                                })
                                                .subscribe();
                                    })
                                    .subscribe();
                        })
                        .subscribe()
        );


        return disposables;
    }

    private final ViewModelCache cache = ViewModelCache.getInstance();
    private final Collection<ProjectData> cachedProjects = new ArrayList<>();
    private final Collection<IssueData> cachedIssues = new ArrayList<>();
    private final Collection<TimeEntryData> cachedTimeEntries = new ArrayList<>();

    private final AsyncGenericDao<UserData> uDao = InMemoryCacheAdapterFactory.ofType(UserData.class);

    private final Cache<ProjectData> projectDataCache = cache.getCache(ProjectData.class);
    private final Cache<IssueData> issueDataCache = cache.getCache(IssueData.class);
    private final Cache<TimeEntryData> timeEntryDataCache = cache.getCache(TimeEntryData.class);
    private final Cache<UserData> userDataCache = cache.getCache(UserData.class);


    private final String TAG = CacheFeeder.class.getSimpleName();

    private final Object syncLock = new Object();
}
