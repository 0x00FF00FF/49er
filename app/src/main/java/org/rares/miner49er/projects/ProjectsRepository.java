package org.rares.miner49er.projects;

import android.annotation.SuppressLint;
import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio3.sqlite.queries.Query;
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
import org.rares.miner49er.persistence.resolvers.IssueStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.resolvers.ProjectStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.resolvers.TimeEntryStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.resolvers.UserStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.tables.IssueTable;
import org.rares.miner49er.persistence.tables.ProjectTable;
import org.rares.miner49er.persistence.tables.ProjectsTable;
import org.rares.miner49er.persistence.tables.TimeEntryTable;
import org.rares.miner49er.persistence.tables.UserTable;
import org.rares.miner49er.projects.model.ProjectData;
import org.rares.miner49er.projects.model.ProjectsSort;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: 8/7/18 add abstraction layer so we could easily switch to using any other persistence layer library
// TODO: 8/7/18 add data to the other screens/domains 
// TODO: 8/7/18 refactor this to be used either only for projects or for all domains 
// TODO: 8/7/18 ### this is where i left off ### 
// if no connectivity, no data is displayed. 
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

    @SuppressLint("UseSparseArrays")
    private void persistProjects(List<Project> projects) {
        Log.d(TAG, "persistProjects() called with: projects = [ ] + " + storio.hashCode());
        if (Collections.emptyList().equals(projects)) {
            Log.e(TAG, "RECEIVED EMPTY LIST. stopping here.");
            return;
        }
        // for badly written/interpreted JSON, perhaps
        // a more refined solution would be a JsonAdapter.Factory
        // https://github.com/square/moshi/issues/295
        if (projects.size() == 1 && projects.get(0).getName() == null) {
            Log.e(TAG, "persistProjects: EMPTY LIST FROM SERVER");
            return;
        }

        long s = System.currentTimeMillis();

        StorIOSQLite.LowLevel ll = storio.lowLevel();


        SQLiteTypeMapping<User> userTypeMapping = ll.typeMapping(User.class);
        SQLiteTypeMapping<Project> projectTypeMapping = ll.typeMapping(Project.class);
        SQLiteTypeMapping<Issue> issueTypeMapping = ll.typeMapping(Issue.class);
        SQLiteTypeMapping<TimeEntry> timeEntryTypeMapping = ll.typeMapping(TimeEntry.class);

        UserStorIOSQLitePutResolver userPutResolver = null;
        ProjectStorIOSQLitePutResolver projectPutResolver = null;
        IssueStorIOSQLitePutResolver issuePutResolver = null;
        TimeEntryStorIOSQLitePutResolver timeEntryPutResolver = null;

        if (userTypeMapping != null) {
            userPutResolver = (UserStorIOSQLitePutResolver) userTypeMapping.putResolver();
        }
        if (projectTypeMapping != null) {
            projectPutResolver = (ProjectStorIOSQLitePutResolver) projectTypeMapping.putResolver();
        }
        if (issueTypeMapping != null) {
            issuePutResolver = (IssueStorIOSQLitePutResolver) issueTypeMapping.putResolver();
        }
        if (timeEntryTypeMapping != null) {
            timeEntryPutResolver = (TimeEntryStorIOSQLitePutResolver) timeEntryTypeMapping.putResolver();
        }

// TODO: 8/14/18          perhaps only use usersToAdd, because all other entities do not repeat themselves

        Map<Integer, User> usersToAdd = new HashMap<>();
        Map<Integer, Issue> issuesToAdd = new HashMap<>();
        Map<Integer, Project> projectsToAdd = new HashMap<>();
        Map<Integer, TimeEntry> timeEntriesToAdd = new HashMap<>();

        try {
            ll.beginTransaction();

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

            Set<String> affectedTables = new HashSet<>();

            affectedTables.add(UserTable.NAME);
            affectedTables.add(ProjectsTable.TABLE_NAME);
            affectedTables.add(IssueTable.NAME);
            affectedTables.add(TimeEntryTable.NAME);


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

                List<Issue> issues = p.getIssues();

                for (Issue i : issues) {
                    for (TimeEntry te : i.getTimeEntries()) {
                        if (te.getUserId() == 0) {
                            te.setUserId(te.getUser().getId());
                        }
                        if (te.getIssueId() == 0) {
                            te.setIssueId(te.getIssue().getId());
                        }
                        timeEntriesToAdd.put(te.getId(), te);
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


//            List<Object> objectsToAdd = new ArrayList<>();
//            objectsToAdd.addAll(usersToAdd.values());
//            objectsToAdd.addAll(timeEntriesToAdd.values());
//            objectsToAdd.addAll(issuesToAdd.values());
//            objectsToAdd.addAll(projectsToAdd.values());
//
//            storio
//                    .put()
//                    .objects(objectsToAdd)
//                    .prepare()
//                    .asRxCompletable()
//                    .doOnError((e) -> {
//                        Log.e(TAG, "!!!!!!!!!!!!");
//                        throw new SQLException(e);
//                    })
//                    .subscribe();


            for (User user : usersToAdd.values()) {
                ll.insert(userPutResolver.mapToInsertQuery(user), userPutResolver.mapToContentValues(user));
            }

            for (TimeEntry timeEntry : timeEntriesToAdd.values()) {
                ll.insert(timeEntryPutResolver.mapToInsertQuery(timeEntry), timeEntryPutResolver.mapToContentValues(timeEntry));
            }

            for (Issue issue : issuesToAdd.values()) {
                ll.insert(issuePutResolver.mapToInsertQuery(issue), issuePutResolver.mapToContentValues(issue));
            }

            for (Project project : projectsToAdd.values()) {
                ll.insert(projectPutResolver.mapToInsertQuery(project), projectPutResolver.mapToContentValues(project));
            }


            ll.setTransactionSuccessful();
            ll.notifyAboutChanges(Changes.newInstance(affectedTables));
        } catch (Exception x) {
            Log.e(TAG, "persistProjects: ERRRORICAAAA", x);
        } finally {
            ll.endTransaction();

            usersToAdd.clear();
            issuesToAdd.clear();
            timeEntriesToAdd.clear();
            projectsToAdd.clear();
        }

        Log.w(TAG, "persistProjects: done insert/update _______________________________ " + (System.currentTimeMillis() - s));
    }

    @Override
    public void registerSubscriber(Consumer<List> consumer) {
        Log.d(TAG, "registerSubscriber() called with: consumer = [" + consumer + "]");

        disposables.add(
                projectTableObservable
                        .doOnNext(x -> Log.i(TAG, "registerSubscriber: change: " + x.affectedTables()))
                        .map(changes -> {

                            Log.i(TAG, "registerSubscriber: CHANGE >>> ADAPTER " + changes.affectedTables().toString());
//
                            boolean shouldConsume = false;
                            for (String s : changes.affectedTables()) {
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
