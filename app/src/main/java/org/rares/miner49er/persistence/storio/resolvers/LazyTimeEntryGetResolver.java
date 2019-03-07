package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import android.util.Log;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetListOfObjects;
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetObject;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import io.reactivex.Single;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.storio.tables.TimeEntryTable;

import java.util.List;

/**
 * Generated resolver for Get Operation.
 */
public class LazyTimeEntryGetResolver extends DefaultGetResolver<TimeEntry> {

    protected LazyTimeEntryGetResolver getInstance() {
        Log.i("LazyProjectGetResolver", "getInstance: ");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public TimeEntry mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {

        TimeEntry timeEntry = new TimeEntry();

        timeEntry.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        timeEntry.setIssueId(cursor.getLong(cursor.getColumnIndex("_issue_id")));
        timeEntry.setUserId(cursor.getLong(cursor.getColumnIndex("_user_id")));
        timeEntry.setWorkDate(cursor.getLong(cursor.getColumnIndex("work_date")));
        timeEntry.setDateAdded(cursor.getLong(cursor.getColumnIndex("date_added")));
        timeEntry.setLastUpdated(cursor.getLong(cursor.getColumnIndex("last_updated")));
        timeEntry.setHours(cursor.getInt(cursor.getColumnIndex("hours")));
        timeEntry.setComments(cursor.getString(cursor.getColumnIndex("comments")));

        return timeEntry;
    }


    public TimeEntry getById(StorIOSQLite storIOSQLite, long id) {
        return _getById(storIOSQLite, id)
                .executeAsBlocking();
    }

    public List<TimeEntry> getByUser(StorIOSQLite storIOSQLite, long userId) {
        return _getByUser(storIOSQLite, userId)
                .executeAsBlocking();
    }

    public List<TimeEntry> getAll(StorIOSQLite storIOSQLite) {
        return _getAll(storIOSQLite)
                .executeAsBlocking();
    }

    public List<TimeEntry> getAll(StorIOSQLite storIOSQLite, long issueId) {
        return _getAll(storIOSQLite, issueId)
                .executeAsBlocking();
    }

    ////

    public Single<Optional<TimeEntry>> getByIdAsync(StorIOSQLite storIOSQLite, long id) {
        return _getById(storIOSQLite, id)
                .asRxSingle();
    }

    public Single<List<TimeEntry>> getByUserAsync(StorIOSQLite storIOSQLite, long userId) {
        return _getByUser(storIOSQLite, userId)
                .asRxSingle();
    }

    public Single<List<TimeEntry>> getAllAsync(StorIOSQLite storIOSQLite) {
        return _getAll(storIOSQLite)
                .asRxSingle();
    }

    public Single<List<TimeEntry>> getAllAsync(StorIOSQLite storIOSQLite, long issueId) {
        return _getAll(storIOSQLite, issueId)
                .asRxSingle();
    }

    ////

    private PreparedGetObject<TimeEntry> _getById(StorIOSQLite storIOSQLite, long id) {
        return storIOSQLite
                .get()
                .object(TimeEntry.class)
                .withQuery(
                        Query.builder()
                                .table(TimeEntryTable.NAME)
                                .where(TimeEntryTable.ID_COLUMN + " = ?")
                                .whereArgs(id)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }

    private PreparedGetListOfObjects<TimeEntry> _getByUser(StorIOSQLite storIOSQLite, long userId) {
        return storIOSQLite
                .get()
                .listOfObjects(TimeEntry.class)
                .withQuery(
                        Query.builder()
                                .table(TimeEntryTable.NAME)
                                .where(TimeEntryTable.USER_ID_COLUMN + " = ? ")
                                .whereArgs(userId)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }

    private PreparedGetListOfObjects<TimeEntry> _getAll(StorIOSQLite storIOSQLite) {
        return storIOSQLite
                .get()
                .listOfObjects(TimeEntry.class)
                .withQuery(
                        Query.builder()
                                .table(TimeEntryTable.NAME)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }

    private PreparedGetListOfObjects<TimeEntry> _getAll(StorIOSQLite storIOSQLite, long issueId) {
        return storIOSQLite
                .get()
                .listOfObjects(TimeEntry.class)
                .withQuery(
                        Query.builder()
                                .table(TimeEntryTable.NAME)
                                .where(TimeEntryTable.ISSUE_ID_COLUMN + " = ? ")
                                .whereArgs(issueId)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }
}
