package org.rares.miner49er.cache.adapter;

import android.util.Log;
import org.rares.miner49er.cache.VMCache;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.dao.GenericDaoFactory;
import org.rares.miner49er.ui.custom.functions.Optional;

import java.util.List;

public class IssueDataCacheAdapter implements GenericDAO<IssueData> {

    private GenericDAO<IssueData> dao = GenericDaoFactory.ofType(IssueData.class);
    private VMCache cache = VMCache.INSTANCE;

    @Override
    public List<IssueData> getAll() {
        return dao.getAll();
    }

    @Override
    public List<IssueData> getAll(long id) {
//        return dao.getAll(id);
        Optional<List<IssueData>> cached = cache.getProjectIssuesData(id);
        if (cached.isPresent()) {
            Log.d(TAG, "getAll: CACHE HIT!");
            return cached.get();
        } else {
            List<IssueData> fromDb = dao.getAll(id);
            cache.updateCachedIssues(id, fromDb);
            return fromDb;
        }
    }

    @Override
    public List<IssueData> getMatching(String term) {
        return dao.getMatching(term);
    }

    @Override
    public IssueData get(long id) {
        Optional<IssueData> cachedIssueData = cache.getLruIssueData(id);
        if (cachedIssueData.isPresent()) {
            Log.d(TAG, "getAll [" + id + "]: CACHE HIT!");
            return cachedIssueData.get();
        } else {
            IssueData fromDb = dao.get(id);
            cache.updateIssueData(fromDb);
            return fromDb;
        }
    }

    @Override
    public long insert(IssueData toInsert) {
        return dao.insert(toInsert);
    }

    @Override
    public void update(IssueData toUpdate) {
        dao.update(toUpdate);
        cache.updateIssueData(toUpdate);
    }

    @Override
    public void delete(IssueData toDelete) {
        cache.removeIssueData(toDelete, false);
        dao.delete(toDelete);
    }
}
