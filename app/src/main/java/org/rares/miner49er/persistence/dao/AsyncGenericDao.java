package org.rares.miner49er.persistence.dao;

import com.pushtorefresh.storio3.Optional;
import io.reactivex.Single;

import java.util.List;

public interface AsyncGenericDao<EntityType extends AbstractViewModel> {

    Single<List<EntityType>> getAll(boolean lazy);

    Single<List<EntityType>> getAll(long parentId, boolean lazy);

    Single<List<EntityType>> getMatching(String term, boolean lazy);

    Single<Optional<EntityType>> get(long id, boolean lazy);

    Single<Long> insert(EntityType toInsert);

    Single<Boolean> update(EntityType toUpdate);

    Single<Boolean> delete(EntityType toDelete);

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
