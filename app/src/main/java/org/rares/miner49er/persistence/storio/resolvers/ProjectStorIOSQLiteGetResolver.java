package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.storio.tables.ProjectsTable;

import java.util.List;

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
        Project project = new Project();

        project.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        project.setOwnerId(cursor.getLong(cursor.getColumnIndex("_user_id")));
        project.setDateAdded(cursor.getLong(cursor.getColumnIndex("date_added")));
        project.setLastUpdated(cursor.getLong(cursor.getColumnIndex("last_updated")));
        project.setName(cursor.getString(cursor.getColumnIndex("project_name")));
        project.setDescription(cursor.getString(cursor.getColumnIndex("project_description")));
        project.setIcon(cursor.getString(cursor.getColumnIndex("icon_path")));
        project.setPicture(cursor.getString(cursor.getColumnIndex("picture_path")));

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
//        project.setIssues(issues);

        return project;
    }

    public static List<Project> getAll(StorIOSQLite storIOSQLite) {
        return storIOSQLite
                .get()
                .listOfObjects(Project.class)
                .withQuery(
                        Query.builder()
                                .table(ProjectsTable.TABLE_NAME)
                                .build())
                .prepare()
                .executeAsBlocking();
    }

    public static List<Project> getMatchingName(StorIOSQLite storIOSQLite, String term) {
        return storIOSQLite.get()
                .listOfObjects(Project.class)
                .withQuery(
                        Query.builder()
                                .table(ProjectsTable.TABLE_NAME)
                                .where(ProjectsTable.COLUMN_PROJECT_NAME + " = ? ")
                                .whereArgs(term)
                                .build())
                .prepare()
                .executeAsBlocking();
    }

    public static Project getById(StorIOSQLite storIOSQLite, long id) {
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
