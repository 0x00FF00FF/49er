package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import android.util.Log;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.StorioFactory;

/**
 * Generated resolver for Get Operation.
 */
public class TimeEntryStorIOSQLiteGetResolver extends LazyTimeEntryGetResolver {

    protected LazyTimeEntryGetResolver getInstance() {
        Log.i("TimeEntryGetResolver", "getInstance: ");
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public TimeEntry mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {

        TimeEntry timeEntry = super.mapFromCursor(storIOSQLite, cursor);

        User user = StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver().getById(storIOSQLite, timeEntry.getUserId());

        timeEntry.setUser(user);

        return timeEntry;
    }

}
