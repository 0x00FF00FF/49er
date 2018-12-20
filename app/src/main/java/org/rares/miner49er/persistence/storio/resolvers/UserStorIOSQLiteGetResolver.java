package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.tables.UserTable;

import java.util.List;

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
        User user = new User();

        user.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        user.setLastUpdated(cursor.getLong(cursor.getColumnIndex("last_updated")));
        user.setName(cursor.getString(cursor.getColumnIndex("user_name")));
        user.setPwd(cursor.getString(cursor.getColumnIndex("password")));
        user.setEmail(cursor.getString(cursor.getColumnIndex("email")));
        user.setPhoto(cursor.getString(cursor.getColumnIndex("photo_path")));
        user.setApiKey(cursor.getString(cursor.getColumnIndex("api_key")));
        user.setRole(cursor.getInt(cursor.getColumnIndex("role")));

        return user;
    }

    public static User getById(StorIOSQLite storIOSQLite, long id) {
        return storIOSQLite
                .get()
                .object(User.class)
                .withQuery(
                        Query.builder()
                                .table(UserTable.NAME)
                                .where(UserTable.ID_COLUMN + " = ?")
                                .whereArgs(id)
                                .build())
                .prepare()
                .executeAsBlocking();
    }

    public static List<User> getMatchingName(StorIOSQLite storIOSQLite, String term) {
        return storIOSQLite
                .get()
                .listOfObjects(User.class)
                .withQuery(
                        Query.builder()
                                .table(UserTable.NAME)
                                .where(UserTable.NAME_COLUMN + " = ? ")
                                .whereArgs(term)
                                .build())
                .prepare()
                .executeAsBlocking();
    }

    public static List<User> getMatchingRole(StorIOSQLite storIOSQLite, int role) {
        return storIOSQLite
                .get()
                .listOfObjects(User.class)
                .withQuery(
                        Query.builder()
                                .table(UserTable.NAME)
                                .where(UserTable.ROLE_COLUMN + " = ? ")
                                .whereArgs(role)
                                .build())
                .prepare()
                .executeAsBlocking();
    }

    public static List<User> getAll(StorIOSQLite storIOSQLite) {
        return storIOSQLite
                .get()
                .listOfObjects(User.class)
                .withQuery(Query.builder()
                        .table(UserTable.NAME)
                        .build())
                .prepare()
                .executeAsBlocking();
    }
}
