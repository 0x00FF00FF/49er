package org.rares.miner49er.cache;

import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.functions.Predicate;
import org.rares.miner49er.domain.projects.model.ProjectData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectDataCache implements Cache<ProjectData> {

    private final ViewModelCache cache = ViewModelCache.getInstance();
    private LruCache<Long, ProjectData> projectsCache = cache.getProjectsLruCache();

    @Override
    public synchronized void putData(List<ProjectData> list, Predicate<ProjectData> ptCondition, boolean link) {

    }

    @Override
    public void putData(List<ProjectData> list, boolean link) {
        if (list != null) {
            if (projectsCache.maxSize() < list.size()) {
                projectsCache = new LruCache<>(list.size());
            }
            for (ProjectData pd : list) {
                putData(pd, link);
            }
        }
    }

    @Override
    public void putData(ProjectData projectData, boolean link) {
        projectsCache.put(projectData.id, projectData);
        cache.sendEvent(CACHE_EVENT_UPDATE_PROJECT);
    }

    @Override
    public void removeData(ProjectData projectData) {
        projectsCache.remove(projectData.id);
        cache.sendEvent(CACHE_EVENT_REMOVE_PROJECT);
    }

    @Override
    public ProjectData getData(Long id) {
        return projectsCache.get(id);
    }

    @Override
    public List<ProjectData> getData(Optional<Long> parentId) {
        List<ProjectData> projectDataList = new ArrayList<>(projectsCache.snapshot().values());
        Collections.sort(projectDataList, (pd1, pd2) -> pd1.id.compareTo(pd2.id));
        return projectDataList;
    }

    @Override
    public int getSize() {
        return projectsCache.size();
    }
}
