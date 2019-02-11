package org.rares.miner49er.cache.adapter;

import org.rares.miner49er.cache.VMCache;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.dao.GenericDaoFactory;

import java.util.List;

public class TimeEntryDataCacheAdapter implements GenericDAO<TimeEntryData> {

    private GenericDAO<TimeEntryData> dao = GenericDaoFactory.ofType(TimeEntryData.class);
    private VMCache cache = VMCache.INSTANCE;

    @Override
    public List<TimeEntryData> getAll() {
        return dao.getAll();
    }

    @Override
    public List<TimeEntryData> getAll(long id) {
        List<TimeEntryData> data = cache.getIssueTimeEntriesData(id);
        if (data != null) {
            return data;
        } else {
            data = dao.getAll(id);
            cache.updateCachedTimeEntries(id, data);
            return data;
        }
    }

    @Override
    public List<TimeEntryData> getMatching(String term) {
        return dao.getMatching(term);
    }

    @Override
    public TimeEntryData get(long id) {
        TimeEntryData data = cache.getLruTimeEntryData(id);
        if (data != null) {
            return data;
        } else {
            data = dao.get(id);
            cache.updateLruTimeEntryData(data);
            return data;
        }
    }

    @Override
    public long insert(TimeEntryData toInsert) {
        return dao.insert(toInsert);
    }

    @Override
    public void update(TimeEntryData toUpdate) {
        dao.update(toUpdate);
        cache.updateTimeEntryData(toUpdate);
    }

    @Override
    public void delete(TimeEntryData toDelete) {
        dao.delete(toDelete);
        cache.removeTimeEntryData(toDelete, false);
    }
}
