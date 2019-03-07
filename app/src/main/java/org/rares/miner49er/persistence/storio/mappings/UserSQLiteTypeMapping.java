package org.rares.miner49er.persistence.storio.mappings;

import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.StorioFactory;

/**
 * Generated mapping with collection of resolvers.
 */
public class UserSQLiteTypeMapping extends SQLiteTypeMapping<User> {
    public UserSQLiteTypeMapping() {
        super(StorioFactory.INSTANCE.getUserStorIOSQLitePutResolver(),
                StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver(),
                StorioFactory.INSTANCE.getUserStorIOSQLiteDeleteResolver());
    }
}
