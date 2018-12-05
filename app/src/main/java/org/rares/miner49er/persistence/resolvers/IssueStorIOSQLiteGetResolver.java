package org.rares.miner49er.persistence.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;
import org.rares.miner49er.persistence.entities.Issue;

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
        Issue object = new Issue();

        object.setId(cursor.getInt(cursor.getColumnIndex("_id")));
        object.setProjectId(cursor.getInt(cursor.getColumnIndex("_project_id")));
        object.setOwnerId(cursor.getInt(cursor.getColumnIndex("_user_id")));
        object.setDateAdded(cursor.getLong(cursor.getColumnIndex("date_added")));
        object.setDateDue(cursor.getLong(cursor.getColumnIndex("date_due")));
        object.setLastUpdated(cursor.getLong(cursor.getColumnIndex("last_updated")));
        object.setName(cursor.getString(cursor.getColumnIndex("issue_name")));

//        object.setProject(new ProjectStorIOSQLiteGetResolver().getById(storIOSQLite, object.getProjectId()));
//        object.setOwner(new UserStorIOSQLiteGetResolver().getById(storIOSQLite, object.getOwnerId()));

        return object;
    }
}
