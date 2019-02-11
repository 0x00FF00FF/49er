package org.rares.miner49er.cache.adapter;

import org.rares.miner49er.cache.VMCache;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.dao.GenericDaoFactory;

import java.util.List;

public class ProjectDataCacheAdapter implements GenericDAO<ProjectData> {

    private GenericDAO<ProjectData> dao = GenericDaoFactory.ofType(ProjectData.class);
    private VMCache cache = VMCache.INSTANCE;

    @Override
    public List<ProjectData> getAll() {
        List<ProjectData> cached = cache.getCachedProjects();
        if (cached != null && cached.size() > 0) {
            return cached;
        } else {
            cached = dao.getAll();
            cache.updateCachedProjects(cached);
            return cached;
        }
    }

    @Override
    public List<ProjectData> getAll(long id) {
        return dao.getAll(id);
    }

    @Override
    public List<ProjectData> getMatching(String term) {
        return dao.getMatching(term);
    }

    @Override
    public ProjectData get(long id) {
        ProjectData data = cache.getProjectData(id);
        if (data != null) {
            return data;
        } else {
            data = dao.get(id);
            cache.updateProjectData(data);
            return data;
        }
    }

    @Override
    public long insert(ProjectData toInsert) {
        return dao.insert(toInsert);
    }

    @Override
    public void update(ProjectData toUpdate) {
        cache.updateProjectData(toUpdate);
        dao.update(toUpdate);
    }

    @Override
    public void delete(ProjectData toDelete) {
        cache.removeProjectData(toDelete);
        dao.delete(toDelete);
    }
}
