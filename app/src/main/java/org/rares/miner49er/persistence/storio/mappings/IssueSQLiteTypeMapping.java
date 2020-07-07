package org.rares.miner49er.persistence.storio.mappings;

import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.storio.StorioFactory;

/**
 * Generated mapping with collection of resolvers.
 */
public class IssueSQLiteTypeMapping extends SQLiteTypeMapping<Issue> {
    public IssueSQLiteTypeMapping() {
        super(StorioFactory.INSTANCE.getIssueStorIOSQLitePutResolver(),
                StorioFactory.INSTANCE.getIssueStorIOSQLiteGetResolver(),
                StorioFactory.INSTANCE.getIssueStorIOSQLiteDeleteResolver());
    }
}
