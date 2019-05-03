package org.rares.miner49er.cache.optimizer;

import android.annotation.SuppressLint;
import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.persistence.dao.GenericEntityDao;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("UseSparseArrays")
public class EntityOptimizer implements Consumer<List<Project>> {

    public static final String TAG = EntityOptimizer.class.getSimpleName();

    private final Map<Long, User> usersToAdd = new HashMap<>();
    private final Map<Long, Issue> issuesToAdd = new HashMap<>();
    private final Map<Long, Project> projectsToAdd = new HashMap<>();
    private final Map<Long, TimeEntry> timeEntriesToAdd = new HashMap<>();

    private GenericEntityDao<Project> projectDao = GenericEntityDao.Factory.of(Project.class);
    private GenericEntityDao<Issue> issueDao = GenericEntityDao.Factory.of(Issue.class);
    private GenericEntityDao<TimeEntry> timeEntryDao = GenericEntityDao.Factory.of(TimeEntry.class);
    private GenericEntityDao<User> userDao = GenericEntityDao.Factory.of(User.class);

    private List<DbUpdateFinishedListener> listenerList = new ArrayList<>();

    public EntityOptimizer() {
    }

    public void addDbUpdateFinishedListener(DbUpdateFinishedListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    // TODO: 3/12/19 Consumers for lists of other entities as well as consumers for <entity>

    @Override
    public void accept(List<Project> projects) {
        boolean successful = false;
        if (prepareEntities(projects)) {
            userDao.wipe();     //// user dao should wipe just the user db (+ related tables = all tables :) )
            successful =
                    userDao.insert(new ArrayList<>(usersToAdd.values())).blockingGet() &&
                            projectDao.insert(new ArrayList<>(projectsToAdd.values())).blockingGet() &&
                            issueDao.insert(new ArrayList<>(issuesToAdd.values())).blockingGet() &&
                            timeEntryDao.insert(new ArrayList<>(timeEntriesToAdd.values())).blockingGet();
        }
        if (!successful) {
            Log.w(TAG, "Problems with inserting entities to database.");
        } else {
            Log.d(TAG, "Finished inserting data.");
//            for (DbUpdateFinishedListener listener : listenerList) {
//                listener.onDbUpdateFinished();
//            }
            usersToAdd.clear();
            issuesToAdd.clear();
            timeEntriesToAdd.clear();
            projectsToAdd.clear();
        }
    }

    private boolean prepareEntities(List<Project> projects) {

        if (Collections.emptyList().equals(projects)) {
            Log.e(TAG, "RECEIVED EMPTY LIST. stopping here.");
            return false;
        }
        // for badly written/interpreted JSON, perhaps
        // a more refined solution would be a JsonAdapter.Factory
        // https://github.com/square/moshi/issues/295
        if (projects.size() == 1 && projects.get(0).getName() == null) {
            Log.e(TAG, "persistProjects: EMPTY LIST FROM SERVER");
            return false;
        }

        Log.i(TAG, "prepareEntities: ..." + projects.size());
        return Flowable.fromIterable(projects)
                .subscribeOn(Schedulers.computation())
                .map(p -> {
                    Log.i(TAG, "prepareEntities: " + p.getName());
                    List<User> users = Collections.emptyList();
                    if (p.getTeam() != null) {
                        users = p.getTeam();
                    }

                    if (p.getOwner() != null) {
                        if (p.getOwnerId() == null || p.getOwnerId() == 0) {
                            p.setOwnerId(p.getOwner().getId());
                        }
                        if (!users.contains(p.getOwner())) {
                            users.add(p.getOwner());
                        }
                    }

                    prepareUsers(users);
                    users.remove(p.getOwner());

                    List<Issue> issues = Collections.emptyList();
                    if (p.getIssues() != null) {
                        issues = p.getIssues();
                    }
                    prepareIssues(issues);

                    p.setPicture(p.getPicture().replaceAll("148", "114"));

                    projectsToAdd.put(p.getId(), p);


                    return p;
                })
                .doOnComplete(() -> Log.i(TAG, "prepareEntities: done."))
                .toList()
                .map(list -> true)
                .blockingGet();
    }

    private void prepareUsers(List<User> users) {
        for (User user : users) {
            if (!usersToAdd.keySet().contains(user.getId())) {
                usersToAdd.put(user.getId(), user);
            }
        }
    }

    private void prepareIssues(List<Issue> issues) {
        for (Issue i : issues) {

            List<TimeEntry> timeEntries = Collections.emptyList();
            if (i.getTimeEntries() != null) {
                timeEntries = i.getTimeEntries();
            }
            prepareTimeEntries(timeEntries);

            if (i.getOwner() != null) {
                if (i.getOwnerId() == null || i.getOwnerId() == 0) {
                    i.setOwnerId(i.getOwner().getId());
                }
                if (!usersToAdd.keySet().contains(i.getOwnerId())) {
                    usersToAdd.put(i.getOwner().getId(), i.getOwner());
                }
            }

            if (i.getProject() != null && (i.getProjectId() == null || i.getProjectId() == 0)) {
                i.setProjectId(i.getProject().getId());
            }

            issuesToAdd.put(i.getId(), i);
        }
    }

    private void prepareTimeEntries(List<TimeEntry> timeEntries) {
        for (TimeEntry te : timeEntries) {
            if (te.getUser() != null) {
                if ((te.getUserId() == null || te.getUserId() == 0)) {
                    te.setUserId(te.getUser().getId());
                }
                if (!usersToAdd.keySet().contains(te.getUserId())) {
                    usersToAdd.put(te.getUser().getId(), te.getUser());
                }
            }
            if (te.getIssue() != null && (te.getIssueId() == null || te.getIssueId() == 0)) {
                te.setIssueId(te.getIssue().getId());
            }
            timeEntriesToAdd.put(te.getId(), te);
        }
    }

    public interface DbUpdateFinishedListener {
        void onDbUpdateFinished();
    }
}