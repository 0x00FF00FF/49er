package org.rares.miner49er.persistence.dao;

import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.queries.RawQuery;
import io.reactivex.Single;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.tables.IssueTable;
import org.rares.miner49er.persistence.storio.tables.ProjectsTable;
import org.rares.miner49er.persistence.storio.tables.TimeEntryTable;
import org.rares.miner49er.persistence.storio.tables.UserProjectTable;
import org.rares.miner49er.persistence.storio.tables.UserTable;

import java.util.List;

public interface GenericEntityDao<EntityType> {
    /* everything is lazy */

    // MyGetResolver<EntityType> getResolver(); + default methods? | + create interface for get resolvers

    Single<List<EntityType>> getAll();

    Single<List<EntityType>> getAll(long parentId);

    Single<List<EntityType>> getMatching(String term);

    Single<Optional<EntityType>> get(long id);

    Single<Boolean> insert(EntityType toInsert);

    Single<Boolean> insert(List<EntityType> insert);

    Single<Boolean> update(EntityType toInsert);

    Single<Boolean> update(List<EntityType> update);

    Single<Boolean> delete(EntityType toDelete);

    Single<Boolean> delete(List<EntityType> toDelete);

    default Single<Boolean> wipe() {
        return StorioFactory.INSTANCE.get()
                .executeSQL()
                .withQuery(RawQuery.builder()
                        .query(String.format("DELETE FROM %s; DELETE FROM %s; DELETE FROM %s; DELETE FROM %s; DELETE FROM %s;",
                                TimeEntryTable.NAME, IssueTable.NAME, ProjectsTable.TABLE_NAME, UserProjectTable.NAME, UserTable.NAME))
                        .build())
                .prepare()
                .asRxSingle()
                .map((x) -> Boolean.TRUE);
    }

    class Factory {

        @SuppressWarnings("unchecked")
        public static <T> GenericEntityDao<T> of(Class<T> cls) {
            GenericEntityDao dao = null;

            if (Project.class.equals(cls)) {
                dao = ProjectsDao.getInstance();
            }
            if (Issue.class.equals(cls)) {
                dao = IssuesDao.getInstance();
            }
            if (TimeEntry.class.equals(cls)) {
                dao = TimeEntriesDao.getInstance();
            }
            if (User.class.equals(cls)) {
                dao = UsersDao.getInstance();
            }

            if (dao != null) {
                return (GenericEntityDao<T>) dao;
            }

            throw new UnsupportedOperationException("No existing DAO was found for " + cls.getSimpleName() + ".");
        }
    }
}
