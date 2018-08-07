package org.rares.miner49er.persistence.resolvers;

import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import org.rares.miner49er.persistence.entities.TimeEntry;

/**
 * Generated mapping with collection of resolvers.
 */
public class TimeEntrySQLiteTypeMapping extends SQLiteTypeMapping<TimeEntry> {
    public TimeEntrySQLiteTypeMapping() {
        super(new TimeEntryStorIOSQLitePutResolver(),
                new TimeEntryStorIOSQLiteGetResolver(),
                new TimeEntryStorIOSQLiteDeleteResolver());
    }
}
