package org.rares.miner49er.cache.optimizer;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lombok.Builder;
import org.rares.miner49er.cache.optimizer.EntityOptimizer.DbUpdateFinishedListener;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.network.NetworkingService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

  private final GenericEntityDao<Project> projectDao;
  private final GenericEntityDao<Issue> issueDao;
  private final GenericEntityDao<TimeEntry> timeEntryDao;
  private final GenericEntityDao<User> userDao;

  private final UserConverter userConverter = UserConverter.builder().build();
  private final ProjectConverter projectConverter = ProjectConverter.builder().userConverter(userConverter).build();
  private final IssueConverter issueConverter;
  private final TimeEntryConverter timeEntryConverter;

  private final NetworkingService ns = NetworkingService.INSTANCE;
  private final List<DbUpdateFinishedListener> listenerList = new ArrayList<>();
  private final List<DataUpdatedListener> updateListeners = new ArrayList<>();
  private final CompositeDisposable disposables = new CompositeDisposable();

  private final Set<User> users = new HashSet<>();
  private final static User defaultUser = new User();
  private final static Project defaultProject = new Project();
  private final static Issue defaultIssue = new Issue();
  private final static TimeEntry defaultTimeEntry = new TimeEntry();

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

  public Flowable<ProjectDto> createProjectUpdateCall(int skip, int count, String... projectObjectIds) {
    UnsupportedOperationException notImplemented = new UnsupportedOperationException("Not implemented yet.");
    Flowable<ProjectDto> projectUpdates;
    if (skip == 0) {
      if (count == 0) {
        if (projectObjectIds.length == 0) {
          // update all projects
          projectUpdates = ns.getProjects();
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

  public Flowable<Project> updateProjects(Flowable<ProjectDto> projectUpdateCall) {
    users.clear();
    return projectUpdateCall
        .retry(1)
        .subscribeOn(Schedulers.io())
        .map(projectConverter::toModel)
        .map(p -> {
          users.addAll(p.getTeam());
          users.add(p.getOwner());
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
              notifyListeners(UserData.class, userDbToVmConverter.dmToVm(list));
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
//          notifyListeners(ProjectData.class, projectDataList);
//          return list;
//        })
        .flatMapPublisher(Flowable::fromIterable);
  }

  public Flowable<Issue> updateIssues(Project project) {
    return ns.issuesService
        .getIssuesForProjectAsSingleList(project.getObjectId())
        .retry(1)
        .subscribeOn(Schedulers.io())
        .flatMapPublisher(Flowable::fromIterable)
        .map(issueConverter::toModel) // get users from cache
        .map(issue -> {
          issue.setProjectId(project.getId());
          return issue;
        })
        .buffer(maxDbParams)
        .flatMap(iList -> saveEntityList(iList, issueDao, defaultIssue), maxThreads);
  }

  public Flowable<TimeEntry> updateTimeEntries(Issue issue) {
    return ns.timeEntriesService
        .getTimeEntriesForIssueAsSingleList(issue.getObjectId())
        .retry(1)
        .subscribeOn(Schedulers.io())
        .flatMapPublisher(Flowable::fromIterable)
//        .flatMapSingle(timeEntryConverter::toModelAsync)
        .map(timeEntryConverter::toModel)
        .map(te -> {
          te.setIssueId(issue.getId());
          return te;
        })
        .buffer(maxDbParams)
        .flatMap(teList -> saveEntityList(teList, timeEntryDao, defaultTimeEntry), maxThreads);
//        .window(maxDbParams)
//        .flatMap(flowable ->
//            flowable.collect((Callable<List<TimeEntry>>) ArrayList::new, List::add)
//                .flatMapPublisher(teList -> saveEntityList(teList, timeEntryDao, defaultTimeEntry)));
  }

  public Flowable<User> updateUsers() {
    return ns.userService.getUsers()
        .retry(1)
        .subscribeOn(Schedulers.io())
        .flatMapPublisher(Flowable::fromIterable)
        .map(UserConverter::toModelBlocking)
        .buffer(maxDbParams)
        .flatMap(userList -> saveEntityList(userList, userDao, defaultUser), maxThreads)
        ;
  }

  public void fullyUpdateProjects(String... projectObjectIds) {
    Disposable d = updateProjects(createProjectUpdateCall(0, 0, projectObjectIds))
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
          if (throwable instanceof ConnectException) {
            Log.w(TAG, "fullyUpdateProjects: connection problems?");
          }
          Log.w(TAG, "fullyUpdateProjects: an error occurred, returning empty list.");
          return Collections.emptyList();
        })
        .subscribe(projects -> {
          if (projects.size() > 0) {
            notifyListeners(ProjectData.class, projects);
          }
        });
    disposables.add(d);
  }

  public void fullyUpdateIssue(String issueId, long projectId) {
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
          if (throwable instanceof ConnectException) {
            Log.i(TAG, "fullyUpdateIssue: connection problems?");
          }
          return defaultIssue;
        })
        .map(issueDbToVmConverter::dmToVm)
        .subscribe(issue -> {
          if (!issue.getId().equals(defaultIssue.getId())) {
            notifyListeners(IssueData.class, Collections.singletonList(issue));
          }
        });
    disposables.add(d);
  }

  public void updateTimeEntry(String timeEntryObjectId, long issueId) {
    Disposable d = ns.timeEntriesService.getTimeEntry(timeEntryObjectId)
        .retry(1)
        .subscribeOn(Schedulers.io())
        .map(teDto -> {
          TimeEntry te = timeEntryConverter.toModel(teDto);
          te.setIssueId(issueId);
          return te;
        })
        .onErrorReturn(throwable -> {
          if (throwable instanceof ConnectException) {
            Log.i(TAG, "fullyUpdateIssue: connection problems?");
          }
          return defaultTimeEntry;
        })
        .flatMap(timeEntryDao::insertWithResult)
        .map(timeEntryDbToVmConverter::dmToVm)
        .subscribe(teData -> {
          if (!teData.getId().equals(defaultTimeEntry.getId())) {
            notifyListeners(TimeEntryData.class, Collections.singletonList(teData));
          }
        });
    disposables.add(d);
  }


  public void lightUpdate() {
    users.clear();
    List<ProjectDto> projects = new ArrayList<>();
    // saved initial projects to have a reference
    // to the projects' issues because i thought
    // that after saving, the issues are gone (but
    // in fact they are not)
    Disposable d = createProjectUpdateCall(0, 0)
        .retry(1)
        .toList()
        .subscribeOn(Schedulers.io())
        .subscribe(list -> {
          projects.addAll(list);
          Disposable ud = updateProjects(Flowable.fromIterable(list))
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
              // threaded save to database
              // - perhaps a better idea would
              // be to collect the issues from all
              // the projects and them buffered
              // save them? (if there are too
              // many requests?)
              .flatMap(prj -> insertEntityList(prj.getIssues(), issueDao, defaultIssue), maxThreads)
              .toList()
//              .map(issueDbToVmConverter::dmToVm)
              .subscribe(/*savedIssues -> notifyListeners(IssueData.class, savedIssues)*/l -> pingListeners());
          disposables.add(ud);
        });
    disposables.add(d);
  }

  @Deprecated
  public void lightUpdate_() {
    users.clear();
    Disposable d = createProjectUpdateCall(0, 0)
        .subscribeOn(Schedulers.io())
        .map(projectConverter::toModel)
        .map(p -> {
          users.addAll(p.getTeam());
          users.add(p.getOwner());
          return p;
        })
        .buffer(maxDbParams)
        .flatMap(projects ->
            this.saveEntityList(new ArrayList<>(users), userDao, defaultUser)
                .map(newUser -> {
                  Log.i(TAG, "lightUpdate_: new user id: " + newUser.getId());
                  populateUserIds(projects, newUser);
                  return newUser;
                })
                .toList()
                .flatMapPublisher(userList -> Flowable.fromIterable(projects)))
        .flatMapSingle(p -> projectDao
            .insertWithResult(p)
            .flatMap(sp -> {
                  sp.setIssues(p.getIssues());
                  Log.d(TAG, "lightUpdate: sp.id " + sp.getId());
                  return Single.just(sp);
                }
            ))
        .map(p -> {
          for (Issue i : p.getIssues()) {
            i.setOwnerId(p.getOwnerId()); //set a default issue owner
            i.setProjectId(p.getId());
            i.setName("New Issue");
          }
          return p;
        })
        .flatMap(p -> insertEntityList(p.getIssues(), issueDao, defaultIssue), maxThreads)
        .count()
        .subscribe(x -> pingListeners());
  }

  public void updateAll() {
// also update users because someone needs to add a [new] user to a project.
    Disposable d = updateUsers()
        .count()
        .flatMapPublisher(n -> {
          int bufferProjects = (n.intValue() / maxThreads); // limit the threads // todo: better!
          return updateProjects(createProjectUpdateCall(0, 0))
              .buffer(bufferProjects)
              .flatMap(Flowable::fromIterable, 2)
              .flatMapSingle(project -> updateIssues(project)
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
                  })
              );
        })
        .map(projectDbToVmConverter::dmToVm)
        .count()
        .onTerminateDetach()
        .doOnError(Throwable::printStackTrace)
        .onErrorResumeNext(x -> {
          Log.i(TAG, "getProjects: returning 0.");
          return Single.just(0L)
              ;
        })
        .subscribe(x -> pingListeners());
    disposables.add(d);
  }

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

  //todo:
  // make sure that related entities are saved correctly into their own caches
  // e.g. time entries when an issue is updated
  private void notifyListeners(Class cls, List<? extends AbstractViewModel> data) {
    Log.d(TAG, "notifyListeners() called with: cls = [" + cls + "], data = [" + data.size() + "]" + updateListeners.size());
    for (DataUpdatedListener updateListener : updateListeners) {
      updateListener.dataUpdated(cls, data);
    }
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
          Log.i(TAG, "saveEntityList: " + entityList.get(0).getClass().getSimpleName() + " " + entityList.size());
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
        .map(t -> {
          Log.i(TAG, "saveEntityList: " + t);
          return t;
        })
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
          Log.i(TAG, "insertEntityList: " + entityList.get(0).getClass().getSimpleName() + " " + entityList.size());
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
                    if (existingE.getObjectId().equals(newE.getObjectId())) {
                      notToUpdate.add(newE);
                      found = true;
                    }
                  }
                  if (!found) {
                    toDelete.add(existingE);
                  }
                }
                newEntities.removeAll(notToUpdate);
                Log.d(TAG, "insertEntityList: to:delete: " + toDelete.size());
                disposables.add(dao.delete(toDelete)
                    .subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace).subscribe(b -> {
                      if (b != null && b) {
                        Log.i(TAG, String.format("insertEntityList: delete succeeded.(%s)", toDelete.size()));
                      } else {
                        Log.w(TAG, String.format("insertEntityList: delete failed.(%s)", toDelete.size()));
                      }
                    }));
                Log.i(TAG, "insertEntityList: " + newEntities.size() + " " + notToUpdate.size());
                return Flowable.fromIterable(newEntities);
              });
        }, maxThreads)
        .map(p -> {
          Log.i(TAG, "insertEntityList: " + p);
          return p;
        })
        .toList()
        .flatMapPublisher(dao::insertWithResult)
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

  public interface DataUpdatedListener {
    void dataUpdated(Class cls, List<? extends AbstractViewModel> data);
  }

  public void addUpdateListener(DataUpdatedListener listener) {
    updateListeners.add(listener);
  }

  public void removeUpdateListener(DataUpdatedListener listener) {
    updateListeners.remove(listener);
  }
}
