package org.rares.miner49er.persistence.resolvers;

import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import org.rares.miner49er.persistence.entities.Issue;

/**
 * Generated resolver for Delete Operation.
 */
public class IssueStorIOSQLiteDeleteResolver extends DefaultDeleteResolver<Issue> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public DeleteQuery mapToDeleteQuery(@NonNull Issue object) {
        return DeleteQuery.builder()
                                        .table("issues")
                                        .where("_id = ?")
                                        .whereArgs(object.getId())
                                        .build();}
}
