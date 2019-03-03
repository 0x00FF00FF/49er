package org.rares.miner49er.cache;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CacheFeeder {

    public Disposable enqueueCacheFill() {
        final CompositeDisposable disposables = new CompositeDisposable();

        if (cache.getProjectsCache().size() != 0) {
            return null;
        }

        AsyncGenericDao<ProjectData> pDao = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        AsyncGenericDao<IssueData> iDao = InMemoryCacheAdapterFactory.ofType(IssueData.class);
        AsyncGenericDao<TimeEntryData> tDao = InMemoryCacheAdapterFactory.ofType(TimeEntryData.class);

        disposables.add(
                Observable.just(uDao, pDao, iDao, tDao)
                        .subscribeOn(Schedulers.computation())
                        .doOnComplete(() -> // TODO: 3/3/19 should detect last cache update caused by this subscription and link data upon it.
                                disposables.add(
                                        Single.just(0)
                                                .subscribeOn(Schedulers.computation())
                                                .delay(5, TimeUnit.SECONDS)
                                                .subscribe((x) -> linkData())))
                        .subscribe(dao -> dao.getAll(true)));

        return disposables;
    }

    private Disposable linkData() {
        CompositeDisposable disposables = new CompositeDisposable();

        Log.v(TAG, "linkData: ------------------------ start " + Thread.currentThread().getName());
        cachedProjects.addAll(cache.getProjectsCache().snapshot().values());
        cachedIssues.addAll(cache.getIssuesCache().snapshot().values());
        cachedTimeEntries.addAll(cache.getTimeEntriesCache().snapshot().values());


        disposables.add(
                Flowable.fromIterable(cachedTimeEntries)
                        .parallel(4)
                        .runOn(Schedulers.computation())
                        .map(timeEntryData -> {
                            if (timeEntryData.getUserName() == null || timeEntryData.getUserPhoto() == null) {
                                UserData userData = cache.getUsersCache().get(timeEntryData.getUserId());
                                if (userData != null) {
                                    timeEntryData.setUserName(userData.getName());
                                    timeEntryData.setUserPhoto(userData.getPicture());
                                }
                            }

                            synchronized (cache.getIssuesCache().get(timeEntryData.parentId)) {
                                IssueData issueData = cache.getIssuesCache().get(timeEntryData.parentId);
                                List<TimeEntryData> teList = issueData.getTimeEntries();
                                if (teList != null) {
                                    teList.add(timeEntryData);
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
                            timeEntryData.lastUpdated = System.currentTimeMillis();
                            return timeEntryData;
                        })
                        .sequential()
                        .doOnComplete(() -> {
                            Log.v(TAG, "linkData: start work on issues");
                            Flowable.fromIterable(cachedIssues)
                                    .parallel(4)
                                    .runOn(Schedulers.computation())
                                    .map(issueData -> {
                                        synchronized (cache.getProjectsCache().get(issueData.parentId)) {
                                            ProjectData projectData = cache.getProjectsCache().get(issueData.parentId);
                                            List<IssueData> idList = projectData.getIssues();
                                            if (idList != null) {
                                                idList.add(issueData);
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
                                        issueData.lastUpdated = System.currentTimeMillis();
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
                                                            projectData.lastUpdated = System.currentTimeMillis();
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

    private final SimpleCache cache = SimpleCache.getInstance();
    private final Collection<ProjectData> cachedProjects = new ArrayList<>();
    private final Collection<IssueData> cachedIssues = new ArrayList<>();
    private final Collection<TimeEntryData> cachedTimeEntries = new ArrayList<>();

    private final AsyncGenericDao<UserData> uDao = InMemoryCacheAdapterFactory.ofType(UserData.class);

    private final String TAG = CacheFeeder.class.getSimpleName();
}
