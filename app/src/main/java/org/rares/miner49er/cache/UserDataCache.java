package org.rares.miner49er.cache;

import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UserDataCache implements Cache<UserData> {

    private final ViewModelCache cache = ViewModelCache.getInstance();
    private final LruCache<Long, ProjectData> projectCache = cache.getProjectsLruCache();
    private final LruCache<Long, UserData> usersCache = cache.getUsersLruCache();

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
        cache.sendEvent(CACHE_EVENT_UPDATE_USER);
    }

    @Override
    public void removeData(UserData userData) {     //////
        usersCache.remove(userData.id);
        Collection<ProjectData> projects = projectCache.snapshot().values();
//        List<TimeEntryData> entriesToRemove = new ArrayList<>();
        Flowable.fromIterable(projects)
                .parallel(4)
                .runOn(Schedulers.computation())
                .map(projectData -> {
                    List<UserData> team = projectData.getTeam();
                    List<IssueData> issues = projectData.getIssues();
                    boolean canRemoveUser = true;
                    if (team != null) {
                        if (team.contains(userData)) {
                            for (IssueData i : issues) {
                                List<TimeEntryData> entries = i.getTimeEntries();
                                if (entries != null) {
                                    for (TimeEntryData ted : entries) {
                                        if (ted.getUserId().equals(userData.id)) {
                                            // entriesToRemove.add(ted);
                                            canRemoveUser = false;
                                        }
                                    }
                                }
                            }
                        }
                        if (canRemoveUser) {
                            synchronized (projectData.getTeam()) {
                                team.remove(userData);
                            }
                        }
                    }
                    return projectData;
                }).sequential()
                .subscribe();
        cache.sendEvent(CACHE_EVENT_REMOVE_USER);
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
                    return Collections.emptyList();
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
}
