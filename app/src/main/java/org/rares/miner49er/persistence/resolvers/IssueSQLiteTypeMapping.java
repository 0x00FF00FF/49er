package org.rares.miner49er.persistence.resolvers;

import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import org.rares.miner49er.persistence.entities.Issue;

/**
 * Generated mapping with collection of resolvers.
 */
public class IssueSQLiteTypeMapping extends SQLiteTypeMapping<Issue> {
    public IssueSQLiteTypeMapping() {
        super(new IssueStorIOSQLitePutResolver(),
                new IssueStorIOSQLiteGetResolver(),
                new IssueStorIOSQLiteDeleteResolver());
    }
}
