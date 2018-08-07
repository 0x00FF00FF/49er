package org.rares.miner49er.persistence.resolvers;

import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import org.rares.miner49er.persistence.entities.Project;

/**
 * Generated mapping with collection of resolvers.
 */
public class ProjectSQLiteTypeMapping extends SQLiteTypeMapping<Project> {
    public ProjectSQLiteTypeMapping() {
        super(new ProjectStorIOSQLitePutResolver(),
                new ProjectStorIOSQLiteGetResolver(),
                new ProjectStorIOSQLiteDeleteResolver());
    }
}
