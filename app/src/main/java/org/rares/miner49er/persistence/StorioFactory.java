package org.rares.miner49er.persistence;

import android.content.Context;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite;
import org.rares.miner49er.persistence.entity.Issue;
import org.rares.miner49er.persistence.entity.IssueSQLiteTypeMapping;
import org.rares.miner49er.persistence.entity.Project;
import org.rares.miner49er.persistence.entity.ProjectSQLiteTypeMapping;
import org.rares.miner49er.persistence.entity.TimeEntry;
import org.rares.miner49er.persistence.entity.TimeEntrySQLiteTypeMapping;
import org.rares.miner49er.persistence.entity.User;
import org.rares.miner49er.persistence.entity.UserSQLiteTypeMapping;

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
