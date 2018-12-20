package org.rares.miner49er.persistence.storio.resolvers;

import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import org.rares.miner49er.persistence.entities.User;

/**
 * Generated mapping with collection of resolvers.
 */
public class UserSQLiteTypeMapping extends SQLiteTypeMapping<User> {
    public UserSQLiteTypeMapping() {
        super(new UserStorIOSQLitePutResolver(),
                new UserStorIOSQLiteGetResolver(),
                new UserStorIOSQLiteDeleteResolver());
    }
}
