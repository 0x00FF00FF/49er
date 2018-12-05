package org.rares.miner49er.persistence.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;

/**
 * Generated resolver for Get Operation.
 */
public class TimeEntryStorIOSQLiteGetResolver extends DefaultGetResolver<TimeEntry> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public TimeEntry mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {

        TimeEntry timeEntry = new TimeEntry();

        timeEntry.setId(cursor.getInt(cursor.getColumnIndex("_id")));
        timeEntry.setIssueId(cursor.getInt(cursor.getColumnIndex("_issue_id")));
        timeEntry.setUserId(cursor.getInt(cursor.getColumnIndex("_user_id")));
        timeEntry.setWorkDate(cursor.getLong(cursor.getColumnIndex("work_date")));
        timeEntry.setDateAdded(cursor.getLong(cursor.getColumnIndex("date_added")));
        timeEntry.setLastUpdated(cursor.getLong(cursor.getColumnIndex("last_updated")));
        timeEntry.setHours(cursor.getInt(cursor.getColumnIndex("hours")));
        timeEntry.setComments(cursor.getString(cursor.getColumnIndex("comments")));

        User user = new UserStorIOSQLiteGetResolver()
                .getById(storIOSQLite, timeEntry.getUserId());

        timeEntry.setUser(user);

//        Issue issue = storIOSQLite
//                .get()
//                .object(Issue.class)
//                .withQuery(Query.builder()
//                        .table(IssueTable.NAME)
//                        .where(IssueTable.ID_COLUMN + " = ?")
//                        .whereArgs(timeEntry.getIssueId())
//                        .build())
//                .prepare()
//                .executeAsBlocking();
//
//        timeEntry.setIssue(issue);

        return timeEntry;
    }

}
