package org.rares.miner49er.persistence.storio.mappings;

import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.storio.StorioFactory;

/**
 * Generated mapping with collection of resolvers.
 */
public class TimeEntrySQLiteTypeMapping extends SQLiteTypeMapping<TimeEntry> {
    public TimeEntrySQLiteTypeMapping() {
        super(StorioFactory.INSTANCE.getTimeEntryStorIOSQLitePutResolver(),
                StorioFactory.INSTANCE.getTimeEntryStorIOSQLiteGetResolver(),
                StorioFactory.INSTANCE.getTimeEntryStorIOSQLiteDeleteResolver());
    }
}
