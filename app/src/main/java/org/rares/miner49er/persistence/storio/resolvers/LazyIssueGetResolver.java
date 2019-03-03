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
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.storio.tables.IssueTable;

import java.util.List;

/**
 * Generated resolver for Get Operation.
 */
public class LazyIssueGetResolver extends DefaultGetResolver<Issue> {

    public final String TAG = LazyIssueGetResolver.class.getSimpleName();

    protected LazyIssueGetResolver getInstance() {
        Log.d(TAG, "getInstance() called");
        return this;
    }

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

        return issue;
    }

    public Single<Optional<Issue>> getByIdAsync(StorIOSQLite storIOSQLite, long id) {
        return _getById(storIOSQLite, id)
                .asRxSingle();
    }

    public Single<List<Issue>> getMatchingNameAsync(StorIOSQLite storIOSQLite, String term) {
        return _getMatchingName(storIOSQLite, term)
                .asRxSingle();
    }

    public Single<List<Issue>> getAllAsync(StorIOSQLite storIOSQLite) {
        return _getAll(storIOSQLite)
                .asRxSingle();
    }

    public Single<List<Issue>> getAllAsync(StorIOSQLite storIOSQLite, long projectId) {
        return _getAll(storIOSQLite, projectId)
                .asRxSingle();       //// storio will get data from the database for each subscriber subscribed to this single.
//                .doOnSubscribe((x) -> Log.i(TAG, "[] getAllAsync: " + x.hashCode() + " " + Thread.currentThread().getName()));
    }


    //

    public Issue getById(StorIOSQLite storIOSQLite, long id) {
        return _getById(storIOSQLite, id)
                .executeAsBlocking();
    }

    public List<Issue> getMatchingName(StorIOSQLite storIOSQLite, String term) {
        return _getMatchingName(storIOSQLite, term)
                .executeAsBlocking();
    }

    public List<Issue> getAll(StorIOSQLite storIOSQLite) {
        return _getAll(storIOSQLite)
                .executeAsBlocking();
    }

    public List<Issue> getAll(StorIOSQLite storIOSQLite, long projectId) {
        return _getAll(storIOSQLite, projectId)
                .executeAsBlocking();
    }

    ///////

    private PreparedGetObject<Issue> _getById(StorIOSQLite storIOSQLite, long id) {
        return storIOSQLite
                .get()
                .object(Issue.class)
                .withQuery(
                        Query.builder()
                                .table(IssueTable.NAME)
                                .where(IssueTable.ID_COLUMN + " = ?")
                                .whereArgs(id)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }

    private PreparedGetListOfObjects<Issue> _getMatchingName(StorIOSQLite storIOSQLite, String term) {
        return storIOSQLite
                .get()
                .listOfObjects(Issue.class)
                .withQuery(
                        Query.builder()
                                .table(IssueTable.NAME)
                                .where(IssueTable.NAME_COLUMN + " = ? ")
                                .whereArgs(term)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }

    private PreparedGetListOfObjects<Issue> _getAll(StorIOSQLite storIOSQLite) {
        return storIOSQLite
                .get()
                .listOfObjects(Issue.class)
                .withQuery(
                        Query.builder()
                                .table(IssueTable.NAME)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }

    private PreparedGetListOfObjects<Issue> _getAll(StorIOSQLite storIOSQLite, long projectId) {
        Log.i(TAG, "_getAll() called with: projectId = [" + projectId + "] " + Thread.currentThread().getName());
        return storIOSQLite.get()
                .listOfObjects(Issue.class)
                .withQuery(
                        Query.builder()
                                .table(IssueTable.NAME)
                                .where(IssueTable.PROJECT_ID_COLUMN + " = ?")
                                .whereArgs(projectId)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }
}
