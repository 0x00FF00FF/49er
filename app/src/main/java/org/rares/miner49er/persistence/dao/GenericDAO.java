package org.rares.miner49er.persistence.dao;

import java.util.List;

public interface GenericDAO<EntityType extends AbstractViewModel> {

    String TAG = GenericDAO.class.getSimpleName();

    List<EntityType> getAll();

    List<EntityType> getAll(long id);

    List<EntityType> getMatching(String term);

    EntityType get(long id);

    long insert(EntityType toInsert);

    void update(EntityType toUpdate);

    void delete(EntityType toDelete);

    default void assertInsertReady(EntityType toInsert) {
        if (toInsert.getId() != null) {
            throw new IllegalStateException("New entities need to have null id! " + toInsert.getId());
        }
    }

    default void assertUpdateReady(EntityType toUpdate) {
        if (toUpdate.getId() <= 0) {
            throw new IllegalStateException("Existing entities need to have positive id! " + toUpdate.getId());
        }
    }

    default void assertDeleteReady(EntityType toDelete) {
        if (toDelete.getId() <= 0) {
            throw new IllegalStateException("Entities to be deleted need to have positive id! " + toDelete.getId());
        }
    }
}
