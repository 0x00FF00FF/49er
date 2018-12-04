package org.rares.miner49er.persistence.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.tables.ProjectsTable;

/**
 * Generated resolver for Get Operation.
 */
public class ProjectStorIOSQLiteGetResolver extends DefaultGetResolver<Project> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Project mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        Project object = new Project();

        object.setId(cursor.getInt(cursor.getColumnIndex("_id")));
        object.setOwnerId(cursor.getInt(cursor.getColumnIndex("_user_id")));
        object.setDateAdded(cursor.getLong(cursor.getColumnIndex("date_added")));
        object.setLastUpdated(cursor.getLong(cursor.getColumnIndex("last_updated")));
        object.setName(cursor.getString(cursor.getColumnIndex("project_name")));
        object.setDescription(cursor.getString(cursor.getColumnIndex("project_description")));
        object.setIcon(cursor.getString(cursor.getColumnIndex("icon_path")));
        object.setPicture(cursor.getString(cursor.getColumnIndex("picture_path")));

//        List<Issue> issues = storIOSQLite.get()
//                .listOfObjects(Issue.class)
//                .withQuery(
//                        Query.builder()
//                                .table(IssueTable.NAME)
//                                .whereArgs(ProjectsTable.COLUMN_ID + " = ?")
//                                .whereArgs(object.getId())
//                                .build())
//                .prepare()
//                .executeAsBlocking();
//
//        object.setIssues(issues);

        return object;
    }

    public Project getById(StorIOSQLite storIOSQLite, int id) {
        return storIOSQLite.get()
                .object(Project.class)
                .withQuery(Query.builder()
                        .table(ProjectsTable.TABLE_NAME)
                        .where(ProjectsTable.COLUMN_ID + " = ?")
                        .whereArgs(id)
                        .build())
                .prepare()
                .executeAsBlocking();
    }
}
