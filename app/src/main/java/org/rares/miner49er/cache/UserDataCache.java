package org.rares.miner49er.cache;

import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UserDataCache implements Cache<UserData>, Closeable {

    private final ViewModelCache cache;
    private final LruCache<Long, ProjectData> projectCache;
    private final LruCache<Long, UserData> usersCache;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public UserDataCache(ViewModelCache cache) {
        this.cache = cache;
        projectCache = cache.getProjectsLruCache();
        usersCache = cache.getUsersLruCache();
    }

    @Override
    public void putData(List<UserData> list, Predicate<UserData> ptCondition, boolean link) {

    }

    @Override
    public void putData(List<UserData> list, boolean link) {
        for (UserData ud : list) {
            putData(ud, link);
        }
    }

    @Override
    public void putData(UserData userData, boolean link) {
        usersCache.put(userData.id, userData);
        if (link) {
            Collection<ProjectData> projects = projectCache.snapshot().values();
            disposables.add(Flowable.fromIterable(projects)
                    .parallel(4)
                    .runOn(Schedulers.computation())
                    .map(projectData -> {
                        List<UserData> team = projectData.getTeam();
                        List<IssueData> issues = projectData.getIssues();
                        if (projectData.getOwner() != null && projectData.getOwner().id.equals(userData.id)) {
                            projectData.setOwner(userData);
                        }
                        if (team != null) {
                            UserData toRemove = null;
                            synchronized (projectCache.get(projectData.id).getTeam()) {
                                for (UserData member : team) {
                                    if (member.id.equals(userData.id)) {
                                        toRemove = member;
//                                    member.updateData(userData);
                                        break;
                                    }
                                }
                            }
                            if (toRemove != null) {
                                synchronized (projectCache.get(projectData.id).getTeam()) {
                                    team.remove(toRemove);
                                    team.add(userData);
                                }
                                if (issues != null) {
                                    synchronized (projectCache.get(projectData.id).getIssues()) {
                                        for (IssueData i : issues) {
                                            if (i.getOwnerId().equals(userData.id) && i.getOwner() != null) {
                                                // TODO: should the owner be added regardless?
                                                i.setOwner(userData);
                                            }
                                            if (i.getTimeEntries() != null) {
                                                List<TimeEntryData> entries = new ArrayList<>(i.getTimeEntries());
                                                for (TimeEntryData ted : entries) {
                                                    if (ted.getUserId().equals(userData.id)) {
                                                        ted.setUserPhoto(userData.getPicture());
                                                        ted.setUserName(userData.getName());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return projectData;
                    })
                    .sequential()
                    .count()
                    .doOnError(x-> System.err.println("ERROR IN USER DATA CACHE PUT USER."))
                    .subscribe(number -> cache.sendEvent(CACHE_EVENT_UPDATE_USER))); //
        } else {
            cache.sendEvent(CACHE_EVENT_UPDATE_USER);
        }
    }

    @Override
    public void removeData(UserData userData) {     ////// one does not simply 'remove' a user
        List<ProjectData> projects = new ArrayList<>(projectCache.snapshot().values());
        disposables.add(checkDelete(projects, userData)
                .subscribe(result -> {
                    remove(result, userData);
                }));
    }

    @Override
    public UserData getData(Long id) {
        return usersCache.get(id);
    }

    @Override
    public List<UserData> getData(Optional<Long> parentId) {
        if (parentId.isPresent()) {
            ProjectData projectData = projectCache.get(parentId.get());
            if (projectData != null) {
                List<UserData> team = projectData.getTeam();
                if (team == null) {
//                    return Collections.emptyList();
                    return null;
                }
//                Collections.sort(team, (u1, u2) -> u1.id.compareTo(u2.id));
                Collections.sort(team, (u1, u2) -> u1.getName().compareTo(u2.getName()));
                Collections.reverse(team);
                return team;
            }
        }
        return new ArrayList<>(usersCache.snapshot().values());
    }

    @Override
    public int getSize() {
        return usersCache.size();
    }

    private void remove(UserDeleteResult result, UserData userData) {
        if (result.notSafeToDelete.size() > 0) {
            return;
        }
        for (ProjectData projectData : result.safeToDelete) {
            synchronized (projectCache.get(projectData.id).getTeam()) {
                List<UserData> team = projectData.getTeam();
                UserData toRemove = null;
                for (UserData ud : team) {
                    if (ud.id.equals(userData.id)) {
                        toRemove = ud;
                        break;
                    }
                }
                if (toRemove != null) {
                    team.remove(toRemove);
                }
            }
        }
        usersCache.remove(userData.id);
        cache.sendEvent(CACHE_EVENT_REMOVE_USER);
    }

    /**
     * Checks if the user is safe to delete. If the user is
     * present in any of the projects, then the user is not safe to delete.
     *
     * @param projects The list of projects that need to be parsed.
     * @param userData The user data to be deleted.
     * @return a list of projects that are linked to the user
     * (the user is project owner or issue owner or time entry owner)
     */
    public Single<UserDeleteResult> checkDelete(final List<ProjectData> projects, final UserData userData) {
        final SingleSubject<UserDeleteResult> resultSingle = SingleSubject.create();
        final List<ProjectData> safeToDelete = new ArrayList<>();
        disposables.add(Flowable.fromIterable(projects)
                .parallel(4)
                .runOn(Schedulers.computation())
                .filter(projectData -> {
                    boolean contained = false;
                    if (projectData.getOwner() != null && projectData.getOwner().id.equals(userData.id)) {
                        return true;
                    }
                    if (projectData.getTeam() != null) {
                        List<UserData> team = new ArrayList<>(projectData.getTeam());

                        for (UserData member : team) {
                            if (member.id.equals(userData.id)) {
                                contained = true;
                                break;
                            }
                        }
                    }
                    if (projectData.getIssues() != null) {
                        List<IssueData> issues = new ArrayList<>(projectData.getIssues());
                        if (contained) {
                            for (IssueData i : issues) {
                                if (i.getOwnerId() != null && i.getOwnerId().equals(userData.id)) {
                                    return true;
                                }
                                List<TimeEntryData> entries = i.getTimeEntries();
                                if (entries != null) {
                                    for (TimeEntryData ted : entries) {
                                        if (ted.getUserId().equals(userData.id)) {
                                            return true;
                                        }
                                    }
                                }
                            }
                            synchronized (safeToDelete) {
                                safeToDelete.add(projectData);
                            }
                        }
                    }
                    return false;
                })
                .sequential()
                .toList()
                .subscribe(notSafe -> {
                    UserDeleteResult udr = new UserDeleteResult(safeToDelete, notSafe);
                    resultSingle.onSuccess(udr);
                })
        );
        return resultSingle;
    }

    @Override
    public void close() {
        disposables.clear();
    }

    public class UserDeleteResult {
        private List<ProjectData> safeToDelete;
        private List<ProjectData> notSafeToDelete;

        UserDeleteResult(
                List<ProjectData> safeToDelete,
                List<ProjectData> notSafeToDelete) {
            this.safeToDelete = safeToDelete;
            this.notSafeToDelete = notSafeToDelete;
        }
    }
}
