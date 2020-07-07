package org.rares.miner49er.network;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;
import lombok.Builder;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.network.dto.ProjectDto;
import org.rares.miner49er.network.dto.converter.IssueConverter;
import org.rares.miner49er.network.dto.converter.ProjectConverter;
import org.rares.miner49er.network.dto.converter.TimeEntryConverter;
import org.rares.miner49er.network.dto.converter.UserConverter;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.persistence.dao.GenericEntityDao;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.ObjectIdHolder;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.rares.miner49er.network.ObservableNetworkProgress.ID_PROJECTS_LIGHT;

/**
 * This class contains methods that
 * update entities. It makes use of
 * the rest clients network calls,
 * different converters (dto, vm, dm)
 * and daos to persist the data.
 */
@Builder
public class DataUpdater {

  private static final String TAG = DataUpdater.class.getSimpleName();

  private final GenericEntityDao<Project> projectDao = GenericEntityDao.Factory.of(Project.class);
  private final GenericEntityDao<Issue> issueDao = GenericEntityDao.Factory.of(Issue.class);
  private final GenericEntityDao<TimeEntry> timeEntryDao = GenericEntityDao.Factory.of(TimeEntry.class);
  private final GenericEntityDao<User> userDao = GenericEntityDao.Factory.of(User.class);

  //  private final UserConverter userConverter = UserConverter.builder().build();
//  private final ProjectConverter projectConverter = ProjectConverter.builder().build();
  private final IssueConverter issueConverter = IssueConverter.builder().userDao(userDao).build();
  private final TimeEntryConverter timeEntryConverter = TimeEntryConverter.builder().userDao(userDao).build();

  private final NetworkingService ns = NetworkingService.INSTANCE;
  //  private final List<DbUpdateFinishedListener> listenerList = new ArrayList<>();
//  private final List<DataUpdatedListener> updateListeners = new ArrayList<>();
  private final CompositeDisposable disposables = new CompositeDisposable();

  private final Set<User> users = new HashSet<>();
  private final static User defaultUser = new User();
  private final static Project defaultProject = new Project();
  private final static Issue defaultIssue = new Issue();
  private final static TimeEntry defaultTimeEntry = new TimeEntry();

//  private final NetworkProgress np = ns.networkProgress;

  private final ObservableNetworkProgress observableNetworkProgress;

  private final ViewModelCache cache;

  private final org.rares.miner49er.domain.users.persistence.UserConverter userDbToVmConverter = new org.rares.miner49er.domain.users.persistence.UserConverter();
  private final org.rares.miner49er.domain.issues.persistence.IssueConverter issueDbToVmConverter = new org.rares.miner49er.domain.issues.persistence.IssueConverter();
  private final org.rares.miner49er.domain.projects.persistence.ProjectConverter projectDbToVmConverter = new org.rares.miner49er.domain.projects.persistence.ProjectConverter();
  private final org.rares.miner49er.domain.entries.persistence.TimeEntryConverter timeEntryDbToVmConverter = new org.rares.miner49er.domain.entries.persistence.TimeEntryConverter();

  public static final int maxDbParams = 100;
  private final int maxThreads = 2;

  static {
    defaultProject.setId(-1L);
    defaultUser.setId(-1L);
    defaultIssue.setId(-1L);
    defaultTimeEntry.setId(-1L);
  }

  private Flowable<ProjectDto> createProjectUpdateCall(int skip, int count, String... projectObjectIds) {
    UnsupportedOperationException notImplemented = new UnsupportedOperationException("Not implemented yet.");
    Flowable<ProjectDto> projectUpdates;
    if (skip == 0) {
      if (count == 0) {
        if (projectObjectIds.length == 0) {
          // update all projects
          projectUpdates = getProjects();
        } else {
          if (projectObjectIds.length < 16) {
            // update projects in projectObjectsIds
            // TODO: 11.09.2019 add optimized service call
            projectUpdates = Flowable.fromArray(projectObjectIds)
                .concatMapSingle(ns.projectsService::getProjectById);
          } else {
            // to update more than 15 projects,
            // we definitely need an optimized
            // service call
            throw notImplemented;
          }
        }
      } else {
        // update projects starting from 0 to <count>
        projectUpdates = ns.projectsService
            .getProjectsAsSingleList(skip, count)
            .flatMapPublisher(Flowable::fromIterable);
      }
    } else {
      if (count == 0) {
        // no projects will be updated
        throw notImplemented;
      } else {
        // update projects starting from <skip> to <count>
        projectUpdates = ns.projectsService
            .getProjectsAsSingleList(skip, count)
            .flatMapPublisher(Flowable::fromIterable);
      }
    }
    return projectUpdates;
  }

  private Flowable<ProjectDto> getProjects() {
    return
        ns.projectsService
            .getProjectsAsSingleList()
            .retry(1, (t) -> {
              Log.w(TAG, "getProjects: retry: " + t.getClass().getSimpleName());
              return !(t instanceof SocketTimeoutException);
            })
            .flatMapPublisher(Flowable::fromIterable)
            .onTerminateDetach()
            .doOnError(e -> Log.e(TAG, "[doOnError] getProjects: ERROR " + e))
//            .onErrorResumeNext(x -> {
//              Log.i(TAG, "getProjects: returning empty list.");
//              return Flowable.fromIterable(Collections.emptyList());
//            })
            .subscribeOn(Schedulers.io());
  }

  private Flowable<Project> updateProjects(Flowable<ProjectDto> projectUpdateCall) {
    users.clear();  // side effect
    return projectUpdateCall
        .retry(1, (t) -> {
          Log.w(TAG, "updateProjects: retry: " + t.getClass().getSimpleName());
          return !(t instanceof SocketTimeoutException);
        })
        .subscribeOn(Schedulers.io())
        .map(ProjectConverter::toModel)
        .map(p -> {
          users.addAll(p.getTeam());
          users.add(p.getOwner());
          p.setLastUpdated(System.currentTimeMillis());
          return p;
        })
        .toList()
        .flatMap(projects -> saveEntityList(new ArrayList<>(users), userDao, defaultUser)
            .map(newUser -> {
              populateUserIds(projects, newUser);
              return newUser;
            })
            .toList()
            .map(list -> {
              updateCache(UserData.class, userDbToVmConverter.dmToVm(list));
              return projects;
            }))
        .flatMapPublisher(pList -> saveEntityList(pList, projectDao, defaultProject))
        .toList()
//        .map(list -> {
//          List<ProjectData> projectDataList = new ArrayList<>();
//          // only add projects to cache. at this time,
//          // the projects' issues have no local id.
//          for (Project project : list) {
//            project.setIssues(new ArrayList<>());
//            projectDataList.add(projectDbToVmConverter.dmToVm(project));
//          }
//          updateCache(ProjectData.class, projectDataList);
//          return list;
//        })
        .flatMapPublisher(Flowable::fromIterable);
  }

  private Flowable<Issue> updateIssues(Project project) {
    return ns.issuesService
        .getIssuesForProjectAsSingleList(project.getObjectId())
        .retry(1)
        .subscribeOn(Schedulers.io())
        .flatMapPublisher(Flowable::fromIterable)
        .map(issueConverter::toModel) // get users from cache
        .map(issue -> {
          issue.setProjectId(project.getId());
          issue.setLastUpdated(System.currentTimeMillis());
          return issue;
        })
        .buffer(maxDbParams)
        .flatMap(iList -> saveEntityList(iList, issueDao, defaultIssue), maxThreads);
  }

  private Flowable<TimeEntry> updateTimeEntries(Issue issue) {
    return ns.timeEntriesService
        .getTimeEntriesForIssueAsSingleList(issue.getObjectId())
        .retry(1)
        .subscribeOn(Schedulers.io())
        .flatMapPublisher(Flowable::fromIterable)
//        .flatMapSingle(timeEntryConverter::toModelAsync)
        .map(timeEntryConverter::toModel)
        .map(te -> {
          te.setIssueId(issue.getId());
          te.setLastUpdated(System.currentTimeMillis());
          return te;
        })
        .buffer(maxDbParams)
        .flatMap(teList -> saveEntityList(teList, timeEntryDao, defaultTimeEntry), maxThreads);
//        .window(maxDbParams)
//        .flatMap(flowable ->
//            flowable.collect((Callable<List<TimeEntry>>) ArrayList::new, List::add)
//                .flatMapPublisher(teList -> saveEntityList(teList, timeEntryDao, defaultTimeEntry)));
  }

  private Flowable<User> updateUsers() {
    return ns.userService.getUsers()
        .retry(1)
        // do not implement onError* so that whoever subscribes
        // can know that something went wrong here.
//        .onErrorReturn(throwable -> {
//          if (throwable instanceof ConnectException) {
//            Log.i(TAG, "updateUsers: connection problems?");
//          }
//          return Collections.emptyList();
//        })
        .subscribeOn(Schedulers.io())
        .flatMapPublisher(Flowable::fromIterable)
        .map(UserConverter::toModelBlocking)
        .map(user -> {
          user.setLastUpdated(System.currentTimeMillis());
          return user;
        })
        .buffer(maxDbParams)
        .flatMap(userList -> saveEntityList(userList, userDao, defaultUser), maxThreads)
        ;
  }

  public void fullyUpdateProjects(String... projectObjectIds) {
    if (projectObjectIds == null || projectObjectIds.length == 0) {
      return;
    }
    CompositeDisposable disposable = new CompositeDisposable();
    for (String projectObjectId : projectObjectIds) {
      UUID uuid = UUID.randomUUID();
      observableNetworkProgress.addNetworkEvent(projectObjectId, uuid, disposable);
    }
    Disposable updateProjectsDisposable =
        updateProjects(createProjectUpdateCall(0, 0, projectObjectIds))
            .concatMapSingle(project -> updateIssues(project)
                .concatMapSingle(issue -> updateTimeEntries(issue)
                    .toList()
                    .map(timeEntryList -> {
                      issue.setTimeEntries(timeEntryList);
                      return issue;
                    }))
                .toList()
                .map(issueList -> {
                  project.setIssues(issueList);
                  return project;
                }))
            .map(projectDbToVmConverter::dmToVm)
            .buffer(maxDbParams)
            .onErrorReturn((throwable) -> {
              for (String oid : projectObjectIds) {
                CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByObjectId(oid));
                if (cs != null) {
                  cs.onError(throwable);
                  observableNetworkProgress.removeNetworkEvent(oid, null);
                }
              }
//          np.cancelNetworkCall(callId, callId);
              if (throwable instanceof ConnectException) {
                Log.w(TAG, "fullyUpdateProjects: connection problems?");
              }
              Log.w(TAG, "fullyUpdateProjects: an error occurred, returning empty list.");
              return Collections.emptyList();
            })
            .subscribe(projects -> {
              if (projects.size() > 0) {
                List<TimeEntryData> tEntries = new ArrayList<>();
                List<IssueData> issues = new ArrayList<>();
                for (ProjectData project : projects) {
                  issues.addAll(project.getIssues());
                  for (IssueData i : project.getIssues()) {
                    tEntries.addAll(i.getTimeEntries());
                  }
                }
                for (String oid : projectObjectIds) {
                  CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByObjectId(oid));
                  if (cs != null) {
                    observableNetworkProgress.removeNetworkEvent(oid, null);
                    cs.onComplete();
                  }
                }
//            np.completeNetworkCall(callId, callId);
                updateCache(ProjectData.class, projects);
                updateCache(IssueData.class, issues);
                updateCache(TimeEntryData.class, tEntries);
              }
            });
    disposable.add(updateProjectsDisposable);
    disposables.add(disposable);
  }

  public void fullyUpdateIssue(String issueId, long projectId) {
    UUID uuid = UUID.randomUUID();
    CompositeDisposable disposable = new CompositeDisposable();
    observableNetworkProgress.addNetworkEvent(issueId, uuid, disposable);
    Disposable d = ns.issuesService.getIssue(issueId)
        .retry(1)
        .subscribeOn(Schedulers.io())
        .map(i -> {
          Issue iss = issueConverter.toModel(i);
          iss.setProjectId(projectId);
          return iss;
        })
        .flatMap(issue ->
            saveEntityList(Collections.singletonList(issue), issueDao, defaultIssue)
                .flatMap(this::updateTimeEntries, maxThreads)
                .toList()
                .map(entryList -> {
                  issue.setTimeEntries(entryList);
                  return issue;
                }))
        .onTerminateDetach()
        .onErrorReturn(throwable -> {
//          np.cancelNetworkCall(callId, callId);
          CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByUuid(uuid));
          if (cs != null) {
            cs.onError(throwable);
            observableNetworkProgress.removeNetworkEvent(issueId, uuid);
          }
          if (throwable instanceof ConnectException) {
            Log.i(TAG, "fullyUpdateIssue: connection problems?");
          }
          return defaultIssue;
        })
        .map(issueDbToVmConverter::dmToVm)
        .subscribe(issue -> {
          if (!issue.getId().equals(defaultIssue.getId())) {
            CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByUuid(uuid));
            if (cs != null) {
              cs.onComplete();
              observableNetworkProgress.removeNetworkEvent(issueId, uuid);
            }
//            np.completeNetworkCall(callId, callId);
            updateCache(IssueData.class, Collections.singletonList(issue));
            updateCache(TimeEntryData.class, new ArrayList<>(issue.getTimeEntries()));
          }
        });
    disposable.add(d);
    disposables.add(disposable);
  }

  public void updateTimeEntry(String timeEntryObjectId, long issueId) {
    UUID uuid = UUID.randomUUID();
    CompositeDisposable disposable = new CompositeDisposable();
    observableNetworkProgress.addNetworkEvent(timeEntryObjectId, uuid, disposable);
    Disposable d = ns.timeEntriesService.getTimeEntry(timeEntryObjectId)
        .retry(1)
        .subscribeOn(Schedulers.io())
        .map(teDto -> {
          TimeEntry te = timeEntryConverter.toModel(teDto);
          te.setIssueId(issueId);
          te.setLastUpdated(System.currentTimeMillis());
          return te;
        })
        .onErrorReturn(throwable -> {
          CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByObjectId(timeEntryObjectId));
          if (cs != null) {
            cs.onError(throwable);
            observableNetworkProgress.removeNetworkEvent(timeEntryObjectId, uuid);
          }
//          np.cancelNetworkCall(callId, callId);
          if (throwable instanceof ConnectException) {
            Log.i(TAG, "fullyUpdateIssue: connection problems?");
          }
          return defaultTimeEntry;
        })
        .flatMap(timeEntryDao::insertWithResult)
        .map(timeEntryDbToVmConverter::dmToVm)
        .subscribe(teData -> {
          if (!teData.getId().equals(defaultTimeEntry.getId())) {
            updateCache(TimeEntryData.class, Collections.singletonList(teData));
            CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByObjectId(timeEntryObjectId));
            if (cs != null) {
              cs.onComplete();
              observableNetworkProgress.removeNetworkEvent(timeEntryObjectId, uuid);
            }
//            np.completeNetworkCall(callId, callId);
          }
        });
    disposable.add(d);
    disposables.add(disposable);
  }

  public void updateTimeEntries(String issueObjectId, long issueId) {
    UUID uuid = UUID.randomUUID();
    CompositeDisposable disposable = new CompositeDisposable();
    observableNetworkProgress.addNetworkEvent(issueObjectId, uuid, disposable);
    Issue tempIssue = new Issue();
    tempIssue.setId(issueId);
    tempIssue.setObjectId(issueObjectId);
    Disposable d = updateTimeEntries(tempIssue)
        .map(timeEntryDbToVmConverter::dmToVm)
        .toList()
        .onErrorReturn(throwable -> {
          CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByUuid(uuid));
          if (cs != null) {
            cs.onError(throwable);
            observableNetworkProgress.removeNetworkEvent(issueObjectId, uuid);
          }
//          np.cancelNetworkCall(callId, callId);
          if (throwable instanceof ConnectException) {
            Log.i(TAG, "fullyUpdateIssue: connection problems?");
          }
          return Collections.emptyList();
        })
        .subscribe(data -> {
          updateCache(TimeEntryData.class, data);
          if (data.size() > 0) {
            CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByUuid(uuid));
            if (cs != null) {
              cs.onComplete();
              observableNetworkProgress.removeNetworkEvent(issueObjectId, uuid);
            }
          }
//          np.completeNetworkCall(callId, callId);
        });
    disposable.add(d);
    disposables.add(disposable);
  }


  public void lightProjectsUpdate() {
//    Log.e(TAG, "lightProjectsUpdate called.");
    UUID uuid = UUID.randomUUID();
    users.clear();
    List<ProjectDto> projects = new ArrayList<>();
    // saved initial projects to have a reference
    // to the projects' issues because i thought
    // that after saving, the issues are gone (but
    // in fact they are not)
    CompositeDisposable disposable = new CompositeDisposable();
//    Log.v(TAG, "lightProjectsUpdate() addNetworkEvent");
    observableNetworkProgress.addNetworkEvent(ID_PROJECTS_LIGHT, uuid, disposable);
    Disposable userDisposable = updateUsers().count().subscribe((count, thr) -> {
      if (thr != null) {
        Log.e(TAG, "lightProjectsUpdate: error while getting users (for projects) " + thr.getMessage());
        return;
      }
      Disposable pucDisposable = createProjectUpdateCall(0, 0)
          .retry(1, (t) -> {
            Log.w(TAG, "lightProjectsUpdate: retry: " + t.getClass().getSimpleName());
            return !(t instanceof SocketTimeoutException);
          })
//              .doOnSubscribe((s) -> observableNetworkProgress.addNetworkEvent(ID_PROJECTS_LIGHT, uuid, disposable))
          .toList()
          .onErrorReturn(throwable -> {
//            Log.i(TAG, "lightProjectsUpdate: error " + uuid);
            CompletableSubject subject = ((CompletableSubject) observableNetworkProgress.getByUuid(uuid));
            if (subject != null) {
              subject.onError(throwable);
              observableNetworkProgress.removeNetworkEvent(ID_PROJECTS_LIGHT, uuid);
            }
            if (throwable instanceof ConnectException) {
              Log.i(TAG, "lightProjectsUpdate: connection problems?");
            }
            return Collections.emptyList();
          })
          .subscribeOn(Schedulers.io())
          .subscribe(list -> {
            projects.addAll(list);
            Disposable updateProjectsDisposable = updateProjects(Flowable.fromIterable(list))
                .map(prj -> {
                  List<Issue> newIssues = new ArrayList<>();
                  for (ProjectDto pd : projects) {
                    if (pd.getId().equals(prj.getObjectId())) {
                      List<String> ids = pd.getIssues();
                      if (ids != null && ids.size() > 0) {
                        for (String oid : ids) {
                          Issue issue = new Issue();
                          issue.setObjectId(oid);
                          issue.setProjectId(prj.getId());
                          issue.setOwnerId(prj.getOwnerId());
                          issue.setName("New issue");
                          newIssues.add(issue);
                        }
                      }
                    }
                  }
                  prj.setIssues(newIssues);
                  return prj;
                })
                .concatMapSingle(prj -> insertEntityList(prj.getIssues(), issueDao, defaultIssue)
                    .toList()
                    .map(il -> {
                      prj.setIssues(il);
//                    System.out.println("project issues: " + prj.getIssues());
                      return projectDbToVmConverter.dmToVm(prj);
                    }))
                .toList()
//                .onErrorReturn(throwable -> {
//                  Log.i(TAG, "lightProjectsUpdate: error " + uuid);
//                  CompletableSubject subject = ((CompletableSubject) observableNetworkProgress.getByUuid(uuid));
//                  if (subject != null) {
//                    subject.onError(throwable);
//                    observableNetworkProgress.removeNetworkEvent(ID_PROJECTS, uuid);
//                  }
//                  if (throwable instanceof ConnectException) {
//                    Log.i(TAG, "lightProjectsUpdate: connection problems?");
//                  }
//                  return Collections.emptyList();
//                })
                .subscribe(savedProjects -> {
                  List<IssueData> issList = new ArrayList<>();
                  disposables.add(
                      Flowable.fromIterable(savedProjects)
                          .map(ProjectData::getIssues)
                          .reduce(issList, (aggregate, newItems) -> {
                            aggregate.addAll(newItems);
                            return aggregate;
                          })
                          .subscribe(issues -> {
                            if (issues.size() > 0) {
//                              Log.i(TAG, "lightProjectsUpdate: completed " + uuid);
                              CompletableSubject subject = ((CompletableSubject) observableNetworkProgress.getByUuid(uuid));
                              if (subject != null) {
                                subject.onComplete();
                                observableNetworkProgress.removeNetworkEvent(ID_PROJECTS_LIGHT, uuid);
                              }
                            }
                            updateCache(IssueData.class, issues);
                            updateCache(ProjectData.class, savedProjects);
//                            np.completeNetworkCall(callId, callId);
                          }));
                });
            disposable.add(updateProjectsDisposable);
          });
      disposable.add(pucDisposable);
    });
    disposable.add(userDisposable);
    disposables.add(disposable);
//    return disposable;
  }

  // TODO: 05.03.2020 : after inserting new issues in the database, try to see if a more complex sync works /!\
  public void lightIssuesUpdate(Long projectId, String projectObjectId) {
//    Log.v(TAG, "lightIssuesUpdate() called with: projectId = [" + projectId + "], projectObjectId = [" + projectObjectId + "], resultListener = [" + resultListener + "]");
    UUID uuid = UUID.randomUUID();
    List<TimeEntry> timeEntries = new ArrayList<>();
    CompositeDisposable disposable = new CompositeDisposable();
    observableNetworkProgress.addNetworkEvent(projectObjectId, uuid, disposable);
    Disposable networkCallDisposable = ns.projectsService.getProjectIssuesAsSingleList(projectObjectId)
        .retry(1)
        .subscribeOn(Schedulers.io())
        .subscribe((issueDtos, throwable) -> {
          if (throwable != null) {
//            Log.w(TAG, "lightIssuesUpdate: ERROR WHEN LIGHT GETTING ISSUES " + throwable.getMessage());
            CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByUuid(uuid));
            if (cs != null) {
              cs.onError(throwable);
              observableNetworkProgress.removeNetworkEvent(projectObjectId, uuid);
            }
            if (throwable instanceof ConnectException) {
              Log.w(TAG, "lightProjectsUpdate: connection problems?");
            }
          } else if (issueDtos != null && issueDtos.size() > 0) { // todo: if size>0 is only temporary here. size CAN be 0
            Disposable convertAndSaveDisposable = Flowable.fromIterable(issueDtos)
//                .doOnComplete(() -> Log.d(TAG, "lightIssuesUpdate: convertAndSaveDisposable complete"))
                .map(issueConverter::toModel)
                .map(issue -> {
                  issue.setProjectId(projectId);
                  issue.setLastUpdated(System.currentTimeMillis());
                  return issue;
                })
                .toList()
                .flatMapPublisher(list -> saveEntityList(list, issueDao, defaultIssue))
/*
// fastest>
        .map(issue -> {
          Disposable teDisposable = Flowable.fromIterable(issue.getTimeEntries())
              .map(timeEntry -> {
                timeEntry.setIssueId(issue.getId());
                timeEntry.setUserId(issue.getOwnerId());
                timeEntry.setHours(-1); // <---
                timeEntry.setComments("To Be Updated...");
                return timeEntry;
              })
              .toList()
              .map(teList -> insertEntityList(teList, timeEntryDao, defaultTimeEntry))
              .subscribe();
          disposables.add(teDisposable);
          return issue;
        })
*/
// blocking, but with progress>
                .map(issue -> {
                  List<TimeEntry> entries = issue.getTimeEntries();
                  for (TimeEntry timeEntry : entries) {
                    timeEntry.setIssueId(issue.getId());
                    timeEntry.setUserId(issue.getOwnerId());
                    timeEntry.setHours(0);
                    timeEntry.setComments("To Be Updated...");
                    timeEntries.add(timeEntry);
                  }
                  issue.setTimeEntries(new ArrayList<>());
                  return issue;
                })
                .map(issueDbToVmConverter::dmToVm)
                .toList()
                .subscribe(issueDataList -> {
                  Disposable teDisposable = insertEntityList(timeEntries, timeEntryDao, defaultTimeEntry)
//                      .doOnComplete(() -> Log.d(TAG, "lightIssuesUpdate: teDisposable complete"))
                      .map(timeEntryDbToVmConverter::dmToVm)
                      .map(entry -> {
                        for (IssueData issueData : issueDataList) {
                          if (issueData.id.equals(entry.getParentId())) {
                            issueData.getTimeEntries().add(entry);
                          }
                        }
                        return entry;
                      })
                      .toList()
                      .subscribe(teList -> {
                        if (teList.size() > 0) {
                          CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByUuid(uuid));
                          if (cs != null) {
                            cs.onComplete();
                            observableNetworkProgress.removeNetworkEvent(projectObjectId, uuid);
                          }
                        }
                        updateCache(TimeEntryData.class, teList);
                        updateCache(IssueData.class, issueDataList);
//                        np.completeNetworkCall(callId, callId);
                      });
                  disposable.add(teDisposable);
                });
            disposable.add(convertAndSaveDisposable);
          } else {
//            np.completeNetworkCall(callId, callId); // this should also be deleted when issueDtoS.size()>0 is removed.
            CompletableSubject cs = ((CompletableSubject) observableNetworkProgress.getByUuid(uuid));
            if (cs != null) {
              cs.onComplete();
              observableNetworkProgress.removeNetworkEvent(projectObjectId, uuid);
            }
//            Log.d(TAG, "lightIssuesUpdate: onComplete");
          }
        });
    disposable.add(networkCallDisposable);
    disposables.add(disposable);
  }

  public void close() {
    disposables.clear();
  }

//  @Deprecated
//  private void pingListeners() {
//    for (DbUpdateFinishedListener listener : listenerList) {
//      listener.onDbUpdateFinished(true, 1);
//    }
//  }

  private void updateCache(Class cls, List<? extends AbstractViewModel> data) {
//    Log.i(TAG, "updateCache: >>>>>>>> NOTIFY LISTENERS >> " + updateListeners.size() + " " + cls.getSimpleName());
    cache.getCache(cls).putData(data, true);
//    for (DataUpdatedListener updateListener : updateListeners) {
//      updateListener.dataUpdated(cls, data);
//    }
  }

  /**
   * Maps oids from classes that implement
   * {@link ObjectIdHolder}.
   *
   * @param objectIdHolders a list of
   *                        objectIdHolders
   *                        to extract their
   *                        objectIds from
   * @return a list of string object ids.
   */
  private List<String> getObjectIds(List<ObjectIdHolder> objectIdHolders) {
    List<String> objectIds = new ArrayList<>();
    for (ObjectIdHolder h : objectIdHolders) {
      objectIds.add(h.getObjectId());
    }
    return objectIds;
  }

  /**
   * Generic method that can accept any type that
   * extends {@link ObjectIdHolder} and any
   * {@link GenericEntityDao} [of the same type],
   * that is used to save the entity list.
   * If items already exist in the database,
   * they are updated accordingly.
   *
   * @param entities      A list of objects that extend
   *                      {@link ObjectIdHolder}.
   * @param dao           A {@link GenericEntityDao} that
   *                      is used to save the objects.
   * @param defaultEntity A default entity of
   *                      the same type as the list
   *                      of objects. This is only
   *                      used internally, to
   *                      determine what to do when
   *                      no items already exist
   *                      [in the database].
   *                      Can not be null.
   * @param <T>           any type that extends {@link
   *                      ObjectIdHolder}
   * @return A list of objects that have been saved
   * [updated or inserted].
   */
  private <T extends ObjectIdHolder> Flowable<T>
  saveEntityList(
      final List<T> entities,
      final GenericEntityDao<T> dao,
      final T defaultEntity) {
    Objects.requireNonNull(defaultEntity);
    return Flowable.fromIterable(entities)
        .buffer(maxDbParams)
        .flatMap(entityList -> {
//          Log.i(TAG, "saveEntityList: " + entityList.get(0).getClass().getSimpleName() + " " + entityList.size());
          List<ObjectIdHolder> teOih = new ArrayList<>(entityList);
          return dao.getByObjectIdIn(getObjectIds(teOih))
              .defaultIfEmpty(defaultEntity)
              .toList()
              .flatMapPublisher(existingEntities -> {
                List<T> newEntities = new ArrayList<T>(entityList);
                if (existingEntities.size() == 1) {
                  if (existingEntities.get(0).equals(defaultEntity)) {
                    return Flowable.fromIterable(entityList);
                  }
                }
                for (T existingE : existingEntities) {
                  for (T newE : newEntities) {
                    if (existingE.getObjectId().equals(newE.getObjectId())) {
                      newE.setId(existingE.getId());
                    }
                  }
                }
                return Flowable.fromIterable(newEntities);
              });
        }, maxThreads)
//        .map(t -> {
//          Log.i(TAG, "saveEntityList: " + t);
//          return t;
//        })
        .toList()
        .flatMapPublisher(dao::insertWithResult)
        ;
  }

  /**
   * Used for quickly updating issues:
   * only inserts/deletes new/obsolete entries.
   *
   * @param entities      fresh entities from the sync server
   * @param dao           a way to get local entities
   * @param defaultEntity an empty entity. it's dead inside :(
   * @param <T>           the type of entities
   * @return a list of saved entities.
   * In this case, the method
   * adds new entities and deletes
   * local entities that are not
   * present in the fresh entities
   * list.
   */
  private <T extends ObjectIdHolder> Flowable<T>
  insertEntityList(
      final List<T> entities,
      final GenericEntityDao<T> dao,
      final T defaultEntity) {
    Objects.requireNonNull(defaultEntity);
    return Flowable.fromIterable(entities)
        .buffer(maxDbParams)
        .flatMap(entityList -> {
//          Log.i(TAG, "insertEntityList: " + entityList.get(0).getClass().getSimpleName() + " " + entityList.size());
          List<ObjectIdHolder> teOih = new ArrayList<>(entityList);
          return dao.getByObjectIdIn(getObjectIds(teOih))
              .defaultIfEmpty(defaultEntity)
              .toList()
              .flatMapPublisher(existingEntities -> {
                List<T> newEntities = new ArrayList<T>(entityList);
                if (existingEntities.size() == 1) {
                  if (existingEntities.get(0).equals(defaultEntity)) {
                    return Flowable.fromIterable(entityList);
                  }
                }
                List<T> notToUpdate = new ArrayList<>();
                List<T> toDelete = new ArrayList<>();
                for (T existingE : existingEntities) {
                  boolean found = false;
                  for (T newE : newEntities) {
                    if (existingE.getObjectId().equals(newE.getObjectId())) { // && ee.getUpdateTime < ne.getUpdateTime
                      notToUpdate.add(newE);
                      found = true;
                    }
                  }
                  if (!found) {
                    toDelete.add(existingE);
                  } else {
                    newEntities.add(existingE);
                  }
                }
                newEntities.removeAll(notToUpdate);
//                Log.d(TAG, "insertEntityList: to:delete: " + toDelete.size());
                disposables.add(dao.delete(toDelete)
                    .subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace).subscribe(b -> {
                      if (b != null && b) {
//                        Log.i(TAG, String.format("insertEntityList: delete succeeded.(%s)", toDelete.size()));
                      } else {
//                        Log.w(TAG, String.format("insertEntityList: delete failed.(%s)", toDelete.size()));
                      }
                    }));
//                Log.i(TAG, "insertEntityList: " + newEntities.size() + " " + notToUpdate.size());
                return Flowable.fromIterable(newEntities);
              });
        }, maxThreads)
//        .map(p -> {
//          Log.i(TAG, "insertEntityList: " + p);
//          return p;
//        })
        .toList()
        .flatMapPublisher(ts -> {
          List<T> toInsert = new ArrayList<>();
          List<T> notToUpdate = new ArrayList<>();
          for (T t : ts) {
            if (t.getId() == null || t.getId() <= 0) {
              toInsert.add(t);
            } else {
              notToUpdate.add(t);
            }
          }
          return dao.insertWithResult(toInsert).concatWith(Flowable.fromIterable(notToUpdate));
        })
        ;
  }

  /**
   * Method that populates a newly inserted
   * user id into its related projects,
   * helping in creating object relations.
   *
   * @param projects list of projects
   *                 that may relate to the
   *                 user (the user can be
   *                 a team member or owner);
   * @param newUser  a user entity that was
   *                 newly inserted into a
   *                 database.
   */
  private void populateUserIds(List<Project> projects, User newUser) {
    for (Project newProject : projects) {
      if (newProject.getOwner().getObjectId().equals(newUser.getObjectId())) {
        newProject.setOwnerId(newUser.getId());
      }
      if (newProject.getOwner() != null && newProject.getOwner().getObjectId().equals(newUser.getObjectId())) {
//        newProject.getOwner().setId(newUser.getId());
        newProject.setOwner(newUser);
      }
      if (newProject.getTeam() != null) {
        for (User projectUser : newProject.getTeam()) {
          if (projectUser.getObjectId().equals(newUser.getObjectId())) {
            projectUser.setId(newUser.getId());
          }
        }
      }
    }
  }

//  public interface DataUpdatedListener {
//    void dataUpdated(Class cls, List<? extends AbstractViewModel> data);
//  }
//
//  public void addUpdateListener(DataUpdatedListener listener) {
//    updateListeners.add(listener);
//  }
//
//  public void removeUpdateListener(DataUpdatedListener listener) {
//    updateListeners.remove(listener);
//  }
}
