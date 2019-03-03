package org.rares.miner49er.domain.projects.persistence;

import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.functions.Predicate;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.SimpleCache;
import org.rares.miner49er.domain.projects.model.ProjectData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectDataCache implements Cache<ProjectData> {

    private final SimpleCache cache = SimpleCache.getInstance();
    private LruCache<Long, ProjectData> projectsCache = cache.getProjectsCache();

    @Override
    public void putData(List<ProjectData> list, Predicate<ProjectData> ptCondition, boolean link) {

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
        cache.sendEvent();
    }

    @Override
    public void removeData(ProjectData projectData) {
        projectsCache.remove(projectData.id);
        cache.sendEvent();
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
}
