package org.rares.miner49er.persistence.storio.mappings;

import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.storio.StorioFactory;

/**
 * Generated mapping with collection of resolvers.
 */
public class ProjectSQLiteTypeMapping extends SQLiteTypeMapping<Project> {

    public ProjectSQLiteTypeMapping() {
        super(StorioFactory.INSTANCE.getProjectTeamPutResolver(),
                StorioFactory.INSTANCE.getProjectTeamGetResolver(),
                StorioFactory.INSTANCE.getProjectStorIOSQLiteDeleteResolver());
    }
}
