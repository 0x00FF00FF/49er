package org.rares.miner49er.persistence;

import android.content.Context;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.resolvers.IssueSQLiteTypeMapping;
import org.rares.miner49er.persistence.resolvers.ProjectSQLiteTypeMapping;
import org.rares.miner49er.persistence.resolvers.TimeEntrySQLiteTypeMapping;
import org.rares.miner49er.persistence.resolvers.UserSQLiteTypeMapping;

public enum StorioFactory {

    INSTANCE;

    public StorIOSQLite get() {
        return storio;
    }

    public StorIOSQLite get(Context c) {
        if (storio == null) {
            setup(c);
        }
        return storio;
    }

    public void setup(Context c) {
        storio = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new StorioDbHelper(c))

                .addTypeMapping(User.class,         new UserSQLiteTypeMapping()     )
                .addTypeMapping(TimeEntry.class,    new TimeEntrySQLiteTypeMapping())
                .addTypeMapping(Issue.class,        new IssueSQLiteTypeMapping()    )
                .addTypeMapping(Project.class,      new ProjectSQLiteTypeMapping()  )

                .build();
    }

    private StorIOSQLite storio = null;

}
