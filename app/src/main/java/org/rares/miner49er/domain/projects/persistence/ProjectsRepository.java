package org.rares.miner49er.domain.projects.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er.cache.cacheadapter.AbstractAsyncCacheAdapter;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.cache.optimizer.CacheFeeder;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.model.ProjectsSort;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.EventBroadcaster;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.tables.IssueTable;
import org.rares.miner49er.persistence.storio.tables.ProjectTable;
import org.rares.miner49er.persistence.storio.tables.TimeEntryTable;
import org.rares.miner49er.persistence.storio.tables.UserTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProjectsRepository
        extends Repository<Project> {
    private static final String TAG = ProjectsRepository.class.getSimpleName();

//    private Flowable<Changes> projectTableObservable;

    private ProjectsSort projectsSort = new ProjectsSort();
    private AsyncGenericDao<ProjectData> asyncDao = InMemoryCacheAdapterFactory.ofType(ProjectData.class);

    public ProjectsRepository() {

        ns.registerProjectsConsumer(this);
//        projectTableObservable =
//                storio
//                        .observeChangesInTable(ProjectsTable.TABLE_NAME, BackpressureStrategy.LATEST)
//                        .subscribeOn(Schedulers.io());

        if (asyncDao instanceof EventBroadcaster) {
            ((EventBroadcaster) asyncDao).registerEventListener((o) -> {
                Log.d(TAG, "ProjectsRepository: cache event//");
                refreshData(true);
            });
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
//        Log.w(TAG, "shutdown() called.");
        disposables.dispose();
        if (asyncDao instanceof AbstractAsyncCacheAdapter) {
            ((AbstractAsyncCacheAdapter) asyncDao).shutdown();
        }
    }

    @Override
    protected boolean prepareEntities(List<Project> projects) {

        // TODO: 8/16/18 add pagination and/or result list so we know how many entities we have

//        Log.d(TAG, "persistProjects() called with: projects = [ ] + " + storio.hashCode());
        if (Collections.emptyList().equals(projects)) {
            Log.e(TAG, "RECEIVED EMPTY LIST. stopping here.");
            if (getData(true, false).size() == 0) {
                demoProcessor.onNext(initializeFakeData());
            }
            return false;
        }
        // for badly written/interpreted JSON, perhaps
        // a more refined solution would be a JsonAdapter.Factory
        // https://github.com/square/moshi/issues/295
        if (projects.size() == 1 && projects.get(0).getName() == null) {
            Log.e(TAG, "persistProjects: EMPTY LIST FROM SERVER");
            return false;
        }

        for (Project p : projects) {
            List<User> users = new ArrayList<>(p.getTeam());
            if (!users.contains(p.getOwner())) {
                users.add(p.getOwner());
            }

            for (User user : users) {
                if (!usersToAdd.keySet().contains(user.getId())) {
                    usersToAdd.put(user.getId(), user);
                }
            }

            List<Issue> issues = Collections.emptyList();
            if (p.getIssues() != null) {
                issues = p.getIssues();
            }

            for (Issue i : issues) {

                if (i.getTimeEntries() != null) {
                    for (TimeEntry te : i.getTimeEntries()) {
                        if (te.getUserId() == null || te.getUserId() == 0) {
                            te.setUserId(te.getUser().getId());
                        }
                        if (te.getIssueId() == null || te.getIssueId() == 0) {
                            te.setIssueId(te.getIssue().getId());
                        }
                        timeEntriesToAdd.put(te.getId(), te);
                    }
                }

                if (i.getOwnerId() == null || i.getOwnerId() == 0) {
                    i.setOwnerId(i.getOwner().getId());
                }

                if (i.getProjectId() == null || i.getProjectId() == 0) {
                    i.setProjectId(i.getProject().getId());
                }

                if (!usersToAdd.keySet().contains(i.getOwnerId())) {
                    usersToAdd.put(i.getOwner().getId(), i.getOwner());
                }

                issuesToAdd.put(i.getId(), i);
            }

            if (p.getOwnerId() == null || p.getOwnerId() == 0) {
                p.setOwnerId(p.getOwner().getId());
            }
            projectsToAdd.put(p.getId(), p);
        }

        return true;
    }

    @Override
    protected void clearTables(StorIOSQLite.LowLevel ll) {
        //          for whatever reason this does not clear the database as expected…
//            ll.executeSQL(RawQuery.builder()
//                    .query(
//                            "DELETE FROM " + TimeEntryTable.NAME + "; " +
//                            "DELETE FROM " + IssueTable.NAME + "; " +
//                            "DELETE FROM " + ProjectsTable.TABLE_NAME + "; " +
//                            "DELETE FROM " + UserTable.NAME + "; "
//                    ).build()
//            );

//          …but using the single instruction per line paradigm seems to do the trick
        ll.delete(DeleteQuery.builder().table(TimeEntryTable.NAME).build());
        ll.delete(DeleteQuery.builder().table(IssueTable.NAME).build());
        ll.delete(DeleteQuery.builder().table(ProjectTable.NAME).build());
        ll.delete(DeleteQuery.builder().table(UserTable.NAME).build());
        // TODO: 8/16/18  ^ should use cascade delete

    }


    @Override
    public void registerSubscriber(Consumer<List> consumer) {
//        Log.d(TAG, "registerSubscriber() called with: consumer = [" + consumer + "]");


        // TODO: 8/15/18 think if this should be moved inside the consumer
        // perhaps the following should be inside the consumer,
        // as this is consumer-specific code and we should only
        // add consumers and not care about what they do with
        // the changes.

        // on the other hand, the repository should be the single
        // source of data for the rv adapter, which expects a
        // list of items. since this is the PROJECTS repository,
        // it can/should contain projects-specific implementations
        disposables.add(
                Flowable.merge(
/*                        projectTableObservable
                                .map(changes -> getData(false)),*/
                        // changes to the db are processed in the
                        // background and added to the cache first
                        userActionsObservable
                                .map(action -> getData(true, false))   // TODO: 2/23/19 first load the user projects
                                .startWith(getData(true, true)),
                        demoProcessor
                                .subscribeOn(Schedulers.computation())
                                .map(projectsList -> DaoConverterFactory.of(Project.class, ProjectData.class).dmToVm(projectsList))
                )
                        .onBackpressureDrop()
                        // FIXME: 3/1/19 | comment next line out, do not use throttle in ViewModelCache events and fix the LayoutManager!
                        .throttleLatest(1, TimeUnit.SECONDS)
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));
    }

    @Override
    protected void refreshQuery() {
    }

    private List<ProjectData> getData(boolean lazy, boolean initialLoad) {
        List<ProjectData> toReturn = new ArrayList<>();
        if (initialLoad) {
            Disposable cacheFillDisposable = cacheFeeder.enqueueCacheFill();
            if (cacheFillDisposable == null) {
                toReturn = asyncDao.getAll(lazy).blockingGet();
            } else {
                disposables.add(cacheFillDisposable);
            }
        } else {
            toReturn = asyncDao.getAll(lazy).blockingGet();
        }
        List<ProjectData> clones = new ArrayList<>();
        for (ProjectData prd : toReturn) {
            ProjectData clone = new ProjectData();
            clone.updateData(prd);
            clones.add(clone);
        }
        return clones;
    }

    private final CacheFeeder cacheFeeder = new CacheFeeder();
    private final String[] localColors = {"#AA7986CB", "#AA5C6BC0"};
    private final String[] remoteColors = {"#9575CD", "#7E57C2"};

    private final String[] dummyData = {
            "Project 1",
            "Project 2",
            "Project 3",
            "Project 4",
            "Project 5",
            "Project 6",
            "Project 9",
            "Project 7",
            "Project 8",
            "Project 10",
            "Project 14",
            "Project 11",
            "Project 12",
            "Project 13",
            "Project 15",
            "Project 16",
            "Project 17",
            "Project 18"
    };

    @Override
    protected final List<Project> initializeFakeData() {

        List<Project> fakeData = new ArrayList<>();

        for (int i = 0; i < 18; i++) {
            Project projectData = new Project();
            projectData.setName(dummyData[i]);
            projectData.setId(-1L);
            projectData.setOwnerId(i + 2L);
            projectData.setPicture("");
            projectData.setIcon("xx");
//            projectData.setDescription("-");
            projectData.setLastUpdated(-1);
            projectData.setIssues(Collections.emptyList());
            projectData.setTeam(Collections.emptyList());
            fakeData.add(projectData);
        }

        return fakeData;
    }
}
