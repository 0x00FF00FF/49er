package org.rares.miner49er.persistence.storio.resolvers;

import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.storio.tables.ProjectsTable;

/**
 * Generated resolver for Delete Operation.
 */
public class ProjectStorIOSQLiteDeleteResolver extends DefaultDeleteResolver<Project> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public DeleteQuery mapToDeleteQuery(@NonNull Project project) {
        return DeleteQuery.builder()
                                        .table(ProjectsTable.TABLE_NAME)
                                        .where(ProjectsTable.COLUMN_ID + " = ?")
                                        .whereArgs(project.getId())
                                        .build();}
}
