package org.rares.miner49er.cache.adapter;

import org.rares.miner49er.cache.VMCache;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.dao.GenericDaoFactory;

import java.util.List;

public class UserDataCacheAdapter implements GenericDAO<UserData> {

    private GenericDAO<UserData> dao = GenericDaoFactory.ofType(UserData.class);
    private VMCache cache = VMCache.INSTANCE;

    @Override
    public List<UserData> getAll() {
        return dao.getAll();
    }

    @Override
    public List<UserData> getAll(long id) {
        return dao.getAll(id);
    }

    @Override
    public List<UserData> getMatching(String term) {
        return dao.getMatching(term);
    }

    @Override
    public UserData get(long id) {
        return dao.get(id);
    }

    @Override
    public long insert(UserData toInsert) {
        return dao.insert(toInsert);
    }

    @Override
    public void update(UserData toUpdate) {
        dao.update(toUpdate);
    }

    @Override
    public void delete(UserData toDelete) {
        dao.delete(toDelete);
    }
}
