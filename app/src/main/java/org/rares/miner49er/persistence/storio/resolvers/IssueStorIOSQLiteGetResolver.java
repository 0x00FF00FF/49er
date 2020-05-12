package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.storio.StorioFactory;

/**
 * Generated resolver for Get Operation.
 */
public class IssueStorIOSQLiteGetResolver extends LazyIssueGetResolver {

    public final String TAG = IssueStorIOSQLiteGetResolver.class.getSimpleName();

    protected LazyIssueGetResolver getInstance() {
//        Log.d(TAG, "getInstance() called");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Issue mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        Issue issue = super.mapFromCursor(storIOSQLite, cursor);

        issue.setTimeEntries(StorioFactory.INSTANCE.getTimeEntryStorIOSQLiteGetResolver().getAll(storIOSQLite, issue.getId()));

        return issue;
    }
}
