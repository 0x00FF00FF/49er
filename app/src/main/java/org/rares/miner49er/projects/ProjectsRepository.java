package org.rares.miner49er.projects;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import com.pushtorefresh.storio3.sqlite.queries.RawQuery;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.tables.IssueTable;
import org.rares.miner49er.persistence.tables.ProjectsTable;
import org.rares.miner49er.persistence.tables.TimeEntryTable;
import org.rares.miner49er.persistence.tables.UserTable;
import org.rares.miner49er.projects.model.ProjectData;
import org.rares.miner49er.projects.model.ProjectsSort;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: 8/7/18 add abstraction layer so we could easily switch to using any other persistence layer library
public class ProjectsRepository extends Repository
        implements
        Consumer<List<Project>>

{
    private static final String TAG = ProjectsRepository.class.getSimpleName();

    private Flowable<Changes> projectTableObservable;
    private CompositeDisposable disposables = new CompositeDisposable();

    private ProjectsSort projectsSort = new ProjectsSort();

    @Override
    public void setup() {
        Log.d(TAG, "setup() called." + storio.hashCode());

        disposables = new CompositeDisposable();
        ns.registerProjectsConsumer(this);
        projectTableObservable =
                storio
                        .observeChangesInTable(ProjectsTable.TABLE_NAME, BackpressureStrategy.LATEST)
                        .doOnNext(d -> Log.i(TAG, "   >>>   : changes happened."));
    }

    @Override
    public void shutdown() {
        Log.w(TAG, "shutdown() called.");
        disposables.dispose();
    }

    private void removeAllProjects() {
        storio.delete()
                .byQuery(DeleteQuery.builder()
                        .table(ProjectsTable.TABLE_NAME)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    private void persistProjects(List<Project> projects) {
        Log.d(TAG, "persistProjects() called with: projects = [ ] + " + storio.hashCode());
        if (Collections.emptyList().equals(projects)) {
            Log.e(TAG, "RECEIVED EMPTY LIST. stopping here.");
            return;
        }
        // TODO: 8/1/18 for badly written/interpreted JSON, perhaps a more refined solution would be a JsonAdapter.Factory
        // https://github.com/square/moshi/issues/295
        if (projects.size() == 1 && projects.get(0).getName() == null) {
            Log.e(TAG, "persistProjects: EMPTY LIST FROM SERVER");
            return;
        }

        long s = System.currentTimeMillis();

        StorIOSQLite.LowLevel ll = storio.lowLevel();
        try {
            ll.beginTransaction();

            Set<String> affectedTables = new HashSet<>();

            affectedTables.add(UserTable.NAME);
            affectedTables.add(ProjectsTable.TABLE_NAME);
            affectedTables.add(IssueTable.NAME);
            affectedTables.add(TimeEntryTable.NAME);

            // fastest way is still bundling stuff into a raw query ...
            ll.rawQuery(RawQuery.builder()
                    .query(
                            "DELETE FROM " + TimeEntryTable.NAME + "; " +
                                    "DELETE FROM " + IssueTable.NAME + "; " +
                                    "DELETE FROM " + ProjectsTable.TABLE_NAME + "; " +
                                    "DELETE FROM " + UserTable.NAME).build()
            ).close();

            for (Project p : projects) {

                List<User> users = new ArrayList<>(p.getTeam());
                if (!users.contains(p.getOwner())) {
                    users.add(p.getOwner());
                }

                storio.put()
                        .objects(users)
                        .prepare()
//                        .executeAsBlocking();
                        .asRxCompletable()
//                        .doOnComplete(() -> Log.d(TAG, "Successfully synced " + users.size() + " users."))
                        .doOnError(Throwable::printStackTrace)
//                        .doOnTerminate(() -> Log.i(TAG, "persistProjects: >>> put users terminated."))
                        .subscribe();

                List<Issue> issues = p.getIssues();

                for (Issue i : issues) {
                    for (TimeEntry t : i.getTimeEntries()) {
                        if (t.getUserId() == 0) {
                            t.setUserId(t.getUser().getId());
                        }
                        if (t.getIssueId() == 0) {
                            t.setIssueId(t.getIssue().getId());
                        }
                    }

                    if (i.getOwnerId() == 0) {
                        i.setOwnerId(i.getOwner().getId());
                    }

                    if (i.getProjectId() == 0) {
                        i.setProjectId(i.getProject().getId());
                    }

                    storio.put()
                            .objects(i.getTimeEntries())
                            .prepare()
//                            .executeAsBlocking();
                            .asRxCompletable()
//                            .doOnComplete(() -> Log.d(TAG, "Successfully synced " + i.getTimeEntries().size() + " time entries."))
                            .doOnError(Throwable::printStackTrace)
//                            .doOnTerminate(() -> Log.i(TAG, "persistProjects: >>> put time entries terminated."))
                            .subscribe();
                }

                storio.put()
                        .objects(issues)
                        .prepare()
//                        .executeAsBlocking();
                        .asRxCompletable()
//                        .doOnComplete(() -> Log.d(TAG, "Successfully synced " + issues.size() + " issues."))
                        .doOnError(Throwable::printStackTrace)
//                        .doOnTerminate(() -> Log.i(TAG, "persistProjects: >>> put issues terminated."))
                        .subscribe();
                if (p.getOwnerId() == 0) {
                    p.setOwnerId(p.getOwner().getId());
                }

//                storio.put()      // this in fact did emit more Changes
//                        .object(p)
//                        .prepare()
//                        .asRxCompletable()
//                        .doOnError(Throwable::printStackTrace)
//                        .subscribe();
            }

            storio
                    .put()
                    .objects(projects)
                    .prepare()
//                    .executeAsBlocking();
                    .asRxCompletable()
//                    .doOnComplete(() -> Log.d(TAG, "Successfully synced " + projects.size() + " projects."))
                    .doOnError((e) -> {
                        Log.e(TAG, "!!!!!!!!!!!!");
                        throw new IllegalStateException(e);
                    })
//                    .doOnTerminate(() -> Log.i(TAG, "persistProjects: >>> put projects terminated."))
                    .subscribe();

            ll.setTransactionSuccessful();
            ll.notifyAboutChanges(Changes.newInstance(affectedTables));
        } catch (Exception x) {
            Log.e(TAG, "persistProjects: ERRRORICAAAA", x);
        } finally {
            ll.endTransaction();
        }

// TODO: 8/7/18 optimize the number of changes emitted.
// should only be one, caused by the transaction. this may
// mean that we won't use the cool builder/decorator
// pattern that storio provides, instead use the low level.

        Log.w(TAG, "persistProjects: done insert/update _______________________________ " + (System.currentTimeMillis() - s));
    }

    @Override
    public void registerSubscriber(Consumer<List> consumer) {
        Log.d(TAG, "registerSubscriber() called with: consumer = [" + consumer + "]");

        disposables.add(
                projectTableObservable
                        .doOnNext(x -> Log.i(TAG, "registerSubscriber: change: " + x.affectedTables()))
                        .buffer(2)      // "inoffensive little hack" that allows us to only act on one change instead of two
                        .map(changes -> {
                            Changes change1 = changes.get(0);
                            Changes change2 = changes.get(1);
                            Changes bigSet;
                            Changes smallSet;

                            if (change1.affectedTables().size() >= change2.affectedTables().size()) {
                                bigSet = change1;
                                smallSet = change2;
                            } else {
                                bigSet = change2;
                                smallSet = change1;
                            }

                            for (String s : smallSet.affectedTables()) {
                                if (!bigSet.affectedTables().contains(s)) {
                                    bigSet.affectedTables().add(s);
                                }
                            }

                            Changes change = bigSet;

                            Log.i(TAG, "registerSubscriber: CHANGE >>> ADAPTER" + change.affectedTables().toString());

                            boolean shouldConsume = false;
                            for (String s : change.affectedTables()) {
                                if (s.equals(ProjectsTable.TABLE_NAME)) {
                                    shouldConsume = true;
                                    break;
                                }
                            }
                            if (!shouldConsume) {
                                Log.i(TAG, "registerSubscriber: CHANGE >>> not consumed.");
                                return Collections.<Project>emptyList();
                            }

                            return storio
                                    .get()
                                    .listOfObjects(Project.class)
                                    .withQuery(Query.builder().table(ProjectsTable.TABLE_NAME).build())
                                    .prepare()
                                    .executeAsBlocking();
                        })
                        .startWith(initializeFakeData())
//        )
//                .flatMap(Flowable::fromIterable)
                        .map(this::db2vm)
//                .toSortedList((p1, p2) -> (int) (p1.getId() - p2.getId()))    <- this will never finish|will not emit
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnTerminate(() -> Log.d(TAG, "Termination of consumer."))
                        .subscribe(consumer));
    }

    @Override
    public void accept(List<Project> projects) throws Exception {
        // TODO: compare projects with the in-memory version

        Log.w(TAG, "[][][][][] DISPOSABLES SIZE: " + disposables.size());
        persistProjects(projects);
    }

    private List<ProjectData> db2vm(List<Project> pl) {
//        Log.d(TAG, "db2vm() called with: p = [" + pl + "]");
        List<ProjectData> projectDataList = new ArrayList<>();


        for (Project p : pl) {
            ProjectData converted = new ProjectData();

            converted.setName(p.getName());
            converted.setIcon(p.getIcon());
            converted.setId(p.getId());
            converted.setDescription(p.getDescription());
            converted.setDateAdded(p.getDateAdded());
            converted.setPicture(p.getPicture());
            converted.setIcon(p.getIcon());
            converted.setColor(projectsColors[NumberUtils.getRandomInt(0, projectsColors.length - 1)]);
            projectDataList.add(converted);
        }

        if (System.currentTimeMillis() % 2 == 1) {
            Collections.reverse(projectDataList);
        }


        return projectDataList;// projectsSort.sort(projectDataList);
    }

    private final String[] projectsColors = {
            "#cbbeb5",
            "#e9aac8",
            "#c9aac8",
            "#a9aac8",
            "#96e7cf",
            "#96c7cf",
            "#96a7cf",
            "#baa0a7",
            "#bac0c7",
            "#bae0e7",
            "#b5a1d1",
            "#b5c1d1",
            "#b5e1d1",
            "#dfc6d0",
            "#ffe6d0",
            "#bfa6d0",
            "#ecbcd7",
            "#d8d3e4",
//            "#232467",
//            "#644783",
//            "#0f54ad",
//            "#000033",
//            "#282531",
//            "#383640",
//            "#282236",
//            "#2a233c",
//            "#2e4f70",
//            "#44344e",
//            "#6c619e",
//            "#7070ff"
    };
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


    /**
     * Creates some fake data. <br/>
     */
    private List<Project> initializeFakeData() {

        List<Project> fakeData = new ArrayList<>();

        for (int i = 0; i < dummyData.length; i++) {
            Project projectData = new Project();
            projectData.setName(dummyData[i]);
            fakeData.add(projectData);
        }

        return fakeData;
    }
}
