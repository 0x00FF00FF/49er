package org.rares.miner49er.cache.optimizer;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import lombok.Builder;
import org.rares.miner49er.cache.optimizer.EntityOptimizer.DbUpdateFinishedListener;
import org.rares.miner49er.network.NetworkingService;
import org.rares.miner49er.network.dto.converter.IssueConverter;
import org.rares.miner49er.network.dto.converter.ProjectConverter;
import org.rares.miner49er.network.dto.converter.TimeEntryConverter;
import org.rares.miner49er.network.dto.converter.UserConverter;
import org.rares.miner49er.persistence.dao.GenericEntityDao;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.ObjectIdHolder;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@Builder
public class DtoToModelConverter {

    private static final String TAG = DtoToModelConverter.class.getSimpleName();

    private final GenericEntityDao<Project> projectDao;
    private final GenericEntityDao<Issue> issueDao;
    private final GenericEntityDao<TimeEntry> timeEntryDao;
    private final GenericEntityDao<User> userDao;

    private final ProjectConverter projectConverter = ProjectConverter.builder().userConverter(new UserConverter()).build();
    private final IssueConverter issueConverter;
    private final TimeEntryConverter timeEntryConverter;

    private final NetworkingService ns = NetworkingService.INSTANCE;
    private final List<DbUpdateFinishedListener> listenerList = new ArrayList<>();
    private final CompositeDisposable disposables = new CompositeDisposable();

//    private final List<TimeEntry> timeEntries = new ArrayList<>();
//    private


    public void updateProjects() {
//        final List<Issue> issues = new ArrayList<>();
        final Set<User> users = new HashSet<>();
        final User defaultUser = new User();
        defaultUser.setId(-1L);
        final Project defaultProject = new Project();
        defaultProject.setId(-1L);
        final Issue defaultIssue = new Issue();
        defaultIssue.setId(-1L);
        final TimeEntry defaultTimeEntry = new TimeEntry();
        defaultTimeEntry.setId(-1L);

        Disposable d = ns.getProjects()
                .map(projectConverter::toModel)
                .map(p -> {
                    users.addAll(p.getTeam());
                    users.add(p.getOwner());
//                    issues.addAll(p.getIssues());
                    return p;
                })
                .toList()
                .flatMapPublisher(projects -> {
                    List<ObjectIdHolder> userOihList = new ArrayList<>(users);
                    return userDao.getByObjectIdIn(getObjectIds(userOihList))// get them from cache?
                            .defaultIfEmpty(defaultUser)
                            .toList()
                            .map(list -> {
                                List<User> newUsers = new ArrayList<>(users);
                                if (list.size() == 1) {
                                    if (list.get(0).equals(defaultUser)) {
                                        return newUsers;
                                    }
                                }
                                for (User existingUser : list) {
                                    for (User newUser : newUsers) {
                                        if (existingUser.getObjectId().equals(newUser.getObjectId())) {
                                            newUser.setId(existingUser.getId());
                                            break;
                                        }
                                    }
                                }
                                return newUsers;
                            })
                            .flatMapPublisher(userDao::insertWithResult)
                            .map(newUser -> {
                                for (Project newProject : projects) {
                                    if (newProject.getOwner().getObjectId().equals(newUser.getObjectId())) {
                                        newProject.setOwnerId(newUser.getId());
                                    }
                                    if (newProject.getTeam() != null) {
                                        for (User projectUser : newProject.getTeam()) {
                                            if (projectUser.getObjectId().equals(newUser.getObjectId())) {
                                                projectUser.setId(newUser.getId());
                                            }
                                        }
                                    }
                                }
                                return newUser;
                            })
                            .toList()
                            .flatMapPublisher(userList -> {
                                List<ObjectIdHolder> projectOihList = new ArrayList<>(projects);
                                return projectDao.getByObjectIdIn(getObjectIds(projectOihList))
                                        .defaultIfEmpty(defaultProject)
                                        .toList()
                                        .flatMapPublisher(list -> {
                                            List<Project> newProjects = new ArrayList<>(projects);
                                            if (list.size() == 1) {
                                                if (list.get(0).equals(defaultProject)) {
                                                    return Flowable.fromIterable(newProjects);
                                                }
                                            }
                                            for (Project existingProject : list) {
                                                for (Project newProject : newProjects) {
                                                    if (existingProject.getObjectId().equals(newProject.getObjectId())) {
                                                        newProject.setId(existingProject.getId());
                                                    }
                                                }
                                            }

                                            return Flowable.fromIterable(newProjects);
                                        });
                            });
                })
                .toList()
                .flatMapPublisher(projectDao::insertWithResult)
                .flatMap(project -> ns.issuesService
                        .getIssuesForProjectAsSingleList(project.getObjectId())
                        .flatMapPublisher(Flowable::fromIterable)
                        .flatMapSingle(issueConverter::toModelAsync) // get users from cache
                        .map(issue -> {
                            issue.setProjectId(project.getId());
                            return issue;
                        }))
                .collect((Callable<List<Issue>>) ArrayList::new, List::add)
                .flatMapPublisher(issues -> {
                    List<ObjectIdHolder> issuesOih = new ArrayList<>(issues);
                    return issueDao.getByObjectIdIn(getObjectIds(issuesOih))
                            .defaultIfEmpty(defaultIssue)
                            .toList()
                            .flatMapPublisher(list -> {
                                List<Issue> newIssues = new ArrayList<>(issues);
                                if (list.size() == 1) {
                                    if (list.get(0).equals(defaultIssue)) {
                                        return Flowable.fromIterable(newIssues);
                                    }
                                }
                                for (Issue existingIssue : list) {
                                    for (Issue newIssue : newIssues) {
                                        if (existingIssue.getObjectId().equals(newIssue.getObjectId())) {
                                            newIssue.setId(existingIssue.getId());
                                        }
                                    }
                                }

                                return Flowable.fromIterable(newIssues);
                            });
                })
                .toList()
                .flatMapPublisher(issueDao::insertWithResult)
                .flatMap(insertedIssue -> ns.timeEntriesService
                        .getTimeEntriesForIssueAsSingleList(insertedIssue.getObjectId())
                        .flatMapPublisher(Flowable::fromIterable)
                        .flatMapSingle(timeEntryConverter::toModelAsync)
                        .flatMapSingle(te -> {
                            te.setIssueId(insertedIssue.getId());
                            return Single.just(te);
                        }))
                .window(333)
                .flatMap(flowable ->
                        flowable.collect((Callable<List<TimeEntry>>) ArrayList::new, List::add)
                                .flatMapPublisher(teList -> {
                                    List<ObjectIdHolder> teOih = new ArrayList<>(teList);
                                    return timeEntryDao.getByObjectIdIn(getObjectIds(teOih))
                                            .defaultIfEmpty(defaultTimeEntry)
                                            .toList()
                                            .flatMapPublisher(list -> {
                                                List<TimeEntry> newTimeEntries = new ArrayList<>(teList);
                                                if (list.size() == 1) {
                                                    if (list.get(0).equals(defaultTimeEntry)) {
                                                        return Flowable.fromIterable(newTimeEntries);
                                                    }
                                                }
                                                for (TimeEntry existingTe : list) {
                                                    for (TimeEntry newTe : newTimeEntries) {
                                                        if (existingTe.getObjectId().equals(newTe.getObjectId())) {
                                                            newTe.setId(existingTe.getId());
                                                        }
                                                    }
                                                }

                                                return Flowable.fromIterable(newTimeEntries);
                                            });
                                }))
                .toList()
                .map(list -> {
                    Log.d(TAG, "updateProjects: [][][][][][] " + list);
                    return list;
                })
                .flatMapPublisher(timeEntryDao::insertWithResult)
                .doOnError(Throwable::printStackTrace)
                .toList()
                .subscribe(x -> pingListeners());
        disposables.add(d);
    }

//    public void updateIssues(String projectId) {
//        disposables.add(
//                ns.issuesService.getIssuesForProject(projectId)
//                        .flatMap(iDao -> Flowable.just(issueConverter.toModel(iDao)))
//                        .flatMap(i -> {
//                            updateTimeEntries(i.getObjectId());
//                            return Flowable.just(i);
//                        })
//                        .collectInto(issues, List::add)
//                        .subscribe());
//    }
//
//    public void updateTimeEntries(String issueId) {
//        disposables.add(ns.timeEntriesService.getTimeEntriesForIssue(issueId)
//                .flatMap(te -> Flowable.just(timeEntryConverter.toModel(te)))
//                .collectInto(timeEntries, List::add)
//                .subscribe());
//    }

    public void addDbUpdateFinishedListener(DbUpdateFinishedListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    public void close() {
        disposables.clear();
    }

    private void pingListeners() {
        for (DbUpdateFinishedListener listener : listenerList) {
            listener.onDbUpdateFinished(true, 1);
        }
    }

    /**
     * Maps oids from classes that implement {@link ObjectIdHolder}.
     * Ideally the objects are distinct.
     *
     * @param objectIdHolders a list of objectIdHolders to extract their objectIds from
     * @return a list of object ids.
     */
    private List<String> getObjectIds(List<ObjectIdHolder> objectIdHolders) {
        List<String> objectIds = new ArrayList<>();
        for (ObjectIdHolder h : objectIdHolders) {
            objectIds.add(h.getObjectId());
        }
        return objectIds;
    }
}
