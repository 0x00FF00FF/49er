package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.storio.tables.IssueTable;

import java.util.List;

/**
 * Generated resolver for Get Operation.
 */
public class IssueStorIOSQLiteGetResolver extends DefaultGetResolver<Issue> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Issue mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        Issue issue = new Issue();

        issue.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        issue.setProjectId(cursor.getLong(cursor.getColumnIndex("_project_id")));
        issue.setOwnerId(cursor.getLong(cursor.getColumnIndex("_user_id")));
        issue.setDateAdded(cursor.getLong(cursor.getColumnIndex("date_added")));
        issue.setDateDue(cursor.getLong(cursor.getColumnIndex("date_due")));
        issue.setLastUpdated(cursor.getLong(cursor.getColumnIndex("last_updated")));
        issue.setName(cursor.getString(cursor.getColumnIndex("issue_name")));

        issue.setTimeEntries(TimeEntryStorIOSQLiteGetResolver.getAll(storIOSQLite, issue.getId()));

//        issue.setProject(new ProjectStorIOSQLiteGetResolver().getById(storIOSQLite, issue.getProjectId()));
//        issue.setOwner(new IssueStorIOSQLiteGetResolver().getById(storIOSQLite, issue.getOwnerId()));

        return issue;
    }

    public static Issue getById(StorIOSQLite storIOSQLite, long id) {
        return storIOSQLite
                .get()
                .object(Issue.class)
                .withQuery(
                        Query.builder()
                                .table(IssueTable.NAME)
                                .where(IssueTable.ID_COLUMN + " = ?")
                                .whereArgs(id)
                                .build())
                .prepare()
                .executeAsBlocking();
    }

    public static List<Issue> getMatchingName(StorIOSQLite storIOSQLite, String term) {
        return storIOSQLite
                .get()
                .listOfObjects(Issue.class)
                .withQuery(
                        Query.builder()
                                .table(IssueTable.NAME)
                                .where(IssueTable.NAME_COLUMN + " = ? ")
                                .whereArgs(term)
                                .build())
                .prepare()
                .executeAsBlocking();
    }

    public static List<Issue> getAll(StorIOSQLite storIOSQLite) {
        return storIOSQLite
                .get()
                .listOfObjects(Issue.class)
                .withQuery(
                        Query.builder()
                                .table(IssueTable.NAME)
                                .build())
                .prepare()
                .executeAsBlocking();
    }

    public static List<Issue> getAll(StorIOSQLite storIOSQLite, long projectId) {
        return storIOSQLite.get()
                .listOfObjects(Issue.class)
                .withQuery(
                        Query.builder()
                                .table(IssueTable.NAME)
                                .where(IssueTable.PROJECT_ID_COLUMN + " = ?")
                                .whereArgs(projectId)
                                .build())
                .prepare()
                .executeAsBlocking();
    }
}
