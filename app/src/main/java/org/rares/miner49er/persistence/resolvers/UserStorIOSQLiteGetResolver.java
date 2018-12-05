package org.rares.miner49er.persistence.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.tables.UserTable;

/**
 * Generated resolver for Get Operation.
 */
public class UserStorIOSQLiteGetResolver extends DefaultGetResolver<User> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public User mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        User object = new User();

        object.setId(cursor.getInt(cursor.getColumnIndex("_id")));
        object.setLastUpdated(cursor.getLong(cursor.getColumnIndex("last_updated")));
        object.setName(cursor.getString(cursor.getColumnIndex("user_name")));
        object.setPwd(cursor.getString(cursor.getColumnIndex("password")));
        object.setEmail(cursor.getString(cursor.getColumnIndex("email")));
        object.setPhoto(cursor.getString(cursor.getColumnIndex("photo_path")));
        object.setApiKey(cursor.getString(cursor.getColumnIndex("api_key")));
        object.setRole(cursor.getInt(cursor.getColumnIndex("role")));

        return object;
    }

    public User getById(StorIOSQLite storIOSQLite, int id) {
        return storIOSQLite
                .get()
                .object(User.class)
                .withQuery(Query.builder()
                        .table(UserTable.NAME)
                        .where(UserTable.ID_COLUMN + " = ?")
                        .whereArgs(id)
                        .build())
                .prepare()
                .executeAsBlocking();
    }
}
