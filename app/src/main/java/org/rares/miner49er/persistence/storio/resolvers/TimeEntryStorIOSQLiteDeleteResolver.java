package org.rares.miner49er.persistence.storio.resolvers;

import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import org.rares.miner49er.persistence.entities.TimeEntry;

/**
 * Generated resolver for Delete Operation.
 */
public class TimeEntryStorIOSQLiteDeleteResolver extends DefaultDeleteResolver<TimeEntry> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public DeleteQuery mapToDeleteQuery(@NonNull TimeEntry object) {
        return DeleteQuery.builder()
                                        .table("time_entries")
                                        .where("_id = ?")
                                        .whereArgs(object.getId())
                                        .build();}
}
