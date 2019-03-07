package org.rares.miner49er.persistence.dao;

import java.util.List;

@Deprecated
public interface GenericDAO<EntityType extends AbstractViewModel> {

    List<EntityType> getAll(boolean lazy);

//    List<EntityType> getAll(boolean lazy, int from, int to);

    List<EntityType> getAll(long parentId, boolean lazy);

    List<EntityType> getMatching(String term, boolean lazy);

    EntityType get(long id, boolean lazy);

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
