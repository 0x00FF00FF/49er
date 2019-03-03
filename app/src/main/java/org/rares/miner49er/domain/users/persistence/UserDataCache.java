package org.rares.miner49er.domain.users.persistence;

import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.functions.Predicate;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.SimpleCache;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserDataCache implements Cache<UserData> {

    private final SimpleCache cache = SimpleCache.getInstance();
    private final LruCache<Long, ProjectData> projectCache = cache.getProjectsCache();
    private LruCache<Long, UserData> usersCache = cache.getUsersCache();

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
        cache.sendEvent();
    }

    @Override
    public void removeData(UserData userData) {
        // users can not be removed from the app
        // they can only be removed from teams
        usersCache.remove(userData.id);
        cache.sendEvent();
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
                return team == null ? Collections.emptyList() : team;
            }
        }
        return new ArrayList<>(usersCache.snapshot().values());
    }
}
