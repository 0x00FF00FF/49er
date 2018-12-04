package org.rares.miner49er.persistence.resolvers;

import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import org.rares.miner49er.persistence.entities.User;

/**
 * Generated resolver for Delete Operation.
 */
public class UserStorIOSQLiteDeleteResolver extends DefaultDeleteResolver<User> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public DeleteQuery mapToDeleteQuery(@NonNull User object) {
        return DeleteQuery.builder()
                                        .table("users")
                                        .where("_id = ?")
                                        .whereArgs(object.getId())
                                        .build();}
}
