package org.rares.miner49er.persistence.resolvers;

import android.support.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import org.rares.miner49er.persistence.entities.Project;

/**
 * Generated resolver for Delete Operation.
 */
public class ProjectStorIOSQLiteDeleteResolver extends DefaultDeleteResolver<Project> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public DeleteQuery mapToDeleteQuery(@NonNull Project object) {
        return DeleteQuery.builder()
                                        .table("projects")
                                        .where("_id = ?")
                                        .whereArgs(object.getId())
                                        .build();}
}
