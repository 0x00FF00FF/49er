package org.rares.miner49er.domain.projects.repository;

import android.graphics.Color;
import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.model.ProjectsSort;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.tables.IssueTable;
import org.rares.miner49er.persistence.tables.ProjectTable;
import org.rares.miner49er.persistence.tables.ProjectsTable;
import org.rares.miner49er.persistence.tables.TimeEntryTable;
import org.rares.miner49er.persistence.tables.UserTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: 8/7/18 add abstraction layer so we could easily switch to using any other persistence layer library

public class ProjectsRepository extends Repository<Project> {
    private static final String TAG = ProjectsRepository.class.getSimpleName();

    private Flowable<Changes> projectTableObservable;

    private ProjectsSort projectsSort = new ProjectsSort();

    public ProjectsRepository() {

        ns.registerProjectsConsumer(this);
        projectTableObservable =
                storio
                        .observeChangesInTable(ProjectsTable.TABLE_NAME, BackpressureStrategy.LATEST)
                        .subscribeOn(Schedulers.io());
    }

    @Override
    public ProjectsRepository setup() {

        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }

        return this;
    }

    @Override
    public ProjectsRepository shutdown() {
//        Log.w(TAG, "shutdown() called.");
        disposables.dispose();

        return this;
    }

    @Override
    protected ProjectsRepository prepareEntities(List<Project> projects) {

        // TODO: 8/16/18 add pagination and/or result list so we know how many entities we have

//        Log.d(TAG, "persistProjects() called with: projects = [ ] + " + storio.hashCode());
        if (Collections.emptyList().equals(projects)) {
            Log.e(TAG, "RECEIVED EMPTY LIST. stopping here.");
            if (getDbProjects().size() == 0) {
                demoProcessor.onNext(initializeFakeData());
            }
            return this;
        }
        // for badly written/interpreted JSON, perhaps
        // a more refined solution would be a JsonAdapter.Factory
        // https://github.com/square/moshi/issues/295
        if (projects.size() == 1 && projects.get(0).getName() == null) {
            Log.e(TAG, "persistProjects: EMPTY LIST FROM SERVER");
            return this;
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
                        if (te.getUserId() == 0) {
                            te.setUserId(te.getUser().getId());
                        }
                        if (te.getIssueId() == 0) {
                            te.setIssueId(te.getIssue().getId());
                        }
                        timeEntriesToAdd.put(te.getId(), te);
                    }
                }

                if (i.getOwnerId() == 0) {
                    i.setOwnerId(i.getOwner().getId());
                }

                if (i.getProjectId() == 0) {
                    i.setProjectId(i.getProject().getId());
                }

                if (!usersToAdd.keySet().contains(i.getOwnerId())) {
                    usersToAdd.put(i.getOwner().getId(), i.getOwner());
                }

                issuesToAdd.put(i.getId(), i);
            }

            if (p.getOwnerId() == 0) {
                p.setOwnerId(p.getOwner().getId());
            }
            projectsToAdd.put(p.getId(), p);
        }

        persistEntities();

        return this;
    }

    @Override
    protected ProjectsRepository clearTables(StorIOSQLite.LowLevel ll) {
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

        return this;
    }


    @Override
    public ProjectsRepository registerSubscriber(Consumer<List> consumer) {
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
                        projectTableObservable
                                .map(changes -> getDbProjects())
                                .map(data -> db2vm(data, false)),
                        userActionsObservable
                                .map(b -> getDbProjects())
                                .startWith(getDbProjects())
                                .map(data -> db2vm(data, true)),
                        demoProcessor
                                .subscribeOn(Schedulers.io())
                                .map(data -> db2vm(data, true))
                )
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));

        return this;
    }

/*    @Override
    public void accept(List<Project> projects) throws Exception {
        // TODO: compare projects with the in-memory version

        Single<List<Project>> projectsPersistSingle = Single.just(projects).subscribeOn(Schedulers.io());
        Disposable persistDisposable = projectsPersistSingle.subscribe(this::prepareEntities);

        disposables.add(persistDisposable);

    }*/

    private int counter = 0;

    private List<ProjectData> db2vm(List<Project> pl, boolean local) {
        List<ProjectData> projectDataList = new ArrayList<>();


        counter++;
        int i = 0;

        for (Project p : pl) {
//            Log.i(TAG, "db2vm: project issues: " + (p.getIssues() != null ? p.getIssues().size() : "null"));
//            Log.v(TAG, "db2vm: project team: " + (p.getTeam() != null ? p.getTeam().size() : "null"));
            ++i;
            ProjectData converted = new ProjectData();
            converted.setName(p.getName() + (local ? "" : " *"));
            converted.setIcon(p.getIcon());
            converted.setId(p.getId());
            converted.setDescription(p.getDescription());
            converted.setDateAdded(p.getDateAdded());
            converted.setPicture(p.getPicture());
            converted.setIcon(p.getIcon());
            // color will be deducted from icon (RenderScript?)
            converted.setColor(Color.parseColor(local ? redColors[i % 2] : blueColors[i % 2]));
            projectDataList.add(converted);
        }

        if (counter > 100) {
            i = 0;
            counter = 0;
        }
//        if (System.currentTimeMillis() % 2 == 1) {
//            Collections.reverse(projectDataList);
//        }


        return projectDataList;// projectsSort.sort(projectDataList);
    }

    @Override
    protected ProjectsRepository refreshQuery() {
        return this;
    }

    private List<Project> getDbProjects() {
        return getDbItems(ProjectsTable.AllProjectsQuery, Project.class);
    }

    private final String[] redColors = {"#AA7986CB", "#AA5C6BC0"};
    private final String[] blueColors = {"#9575CD", "#7E57C2"};

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
            projectData.setId(-1);
            projectData.setOwnerId(i + 2);
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
