package org.rares.miner49er.persistence.storio.resolvers;

import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import org.rares.miner49er.persistence.entities.Project;

/**
 * Generated mapping with collection of resolvers.
 */
public class ProjectSQLiteTypeMapping extends SQLiteTypeMapping<Project> {

    public ProjectSQLiteTypeMapping(UserProjectPutResolver userProjectPutResolver) {
        super(new ProjectTeamPutResolver(userProjectPutResolver),
                new ProjectTeamGetResolver(),
                new ProjectTeamDeleteResolver());
    }
}
