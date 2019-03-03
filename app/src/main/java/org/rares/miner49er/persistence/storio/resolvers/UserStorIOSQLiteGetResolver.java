package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetListOfObjects;
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetObject;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import com.pushtorefresh.storio3.sqlite.queries.RawQuery;
import io.reactivex.Single;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.tables.UserProjectTable;
import org.rares.miner49er.persistence.storio.tables.UserTable;

import java.util.ArrayList;
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

    public User getById(StorIOSQLite storIOSQLite, long id) {
        return _getById(storIOSQLite, id).executeAsBlocking();
    }

    public List<User> getMatchingName(StorIOSQLite storIOSQLite, String term) {
        return _getMatchingName(storIOSQLite, term).executeAsBlocking();
    }

    public List<User> getMatchingRole(StorIOSQLite storIOSQLite, int role) {
        return _getMatchingRole(storIOSQLite, role).executeAsBlocking();
    }

    public List<User> getAll(StorIOSQLite storIOSQLite) {
        return _getAll(storIOSQLite).executeAsBlocking();
    }

    public List<User> getAll(StorIOSQLite storIOSQLite, long projectId) {
        return _getAll(storIOSQLite, projectId).executeAsBlocking();
    }

    ////

    public Single<Optional<User>> getByIdAsync(StorIOSQLite storIOSQLite, long id) {
        return _getById(storIOSQLite, id).asRxSingle();
    }

    public Single<List<User>> getMatchingNameAsync(StorIOSQLite storIOSQLite, String term) {
        return _getMatchingName(storIOSQLite, term).asRxSingle();
    }

    public Single<List<User>> getMatchingRoleAsync(StorIOSQLite storIOSQLite, int role) {
        return _getMatchingRole(storIOSQLite, role).asRxSingle();
    }

    public Single<List<User>> getAllAsync(StorIOSQLite storIOSQLite) {
        return _getAll(storIOSQLite).asRxSingle();
    }


    public Single<List<User>> getAllAsync(StorIOSQLite storIOSQLite, long projectId) {
        return _getAll(storIOSQLite, projectId).asRxSingle();
    }


    private PreparedGetObject<User> _getById(StorIOSQLite storIOSQLite, long id) {
        return storIOSQLite
                .get()
                .object(User.class)
                .withQuery(
                        Query.builder()
                                .table(UserTable.NAME)
                                .where(UserTable.ID_COLUMN + " = ?")
                                .whereArgs(id)
                                .build())
                .prepare();
    }

    private PreparedGetListOfObjects<User> _getMatchingName(StorIOSQLite storIOSQLite, String term) {
        return storIOSQLite
                .get()
                .listOfObjects(User.class)
                .withQuery(
                        Query.builder()
                                .table(UserTable.NAME)
                                .where(UserTable.NAME_COLUMN + " = ? ")
                                .whereArgs(term)
                                .build())
                .prepare();
    }

    private PreparedGetListOfObjects<User> _getMatchingRole(StorIOSQLite storIOSQLite, int role) {
        return storIOSQLite
                .get()
                .listOfObjects(User.class)
                .withQuery(
                        Query.builder()
                                .table(UserTable.NAME)
                                .where(UserTable.ROLE_COLUMN + " = ? ")
                                .whereArgs(role)
                                .build())
                .prepare();
    }

    private PreparedGetListOfObjects<User> _getAll(StorIOSQLite storIOSQLite) {
        return storIOSQLite
                .get()
                .listOfObjects(User.class)
                .withQuery(Query.builder()
                        .table(UserTable.NAME)
                        .build())
                .prepare();
    }

    private PreparedGetListOfObjects<User> _getAll(StorIOSQLite storIOSQLite, long projectId) {
        return storIOSQLite
                .get()
                .listOfObjects(User.class)
                .withQuery(RawQuery.builder()
                        .query("SELECT " + UserTable.NAME + ".* FROM " + UserTable.NAME +
                                " JOIN " + UserProjectTable.NAME +
                                " ON " + UserTable.NAME + "." + UserTable.ID_COLUMN + " = " + UserProjectTable.USER_ID_COLUMN +
                                " AND " + UserProjectTable.PROJECT_ID_COLUMN + " = ?")
                        .args(projectId)
                        .build())
                .prepare();
    }

    private List<Long> _getAllIds(StorIOSQLite storIOSQLite, long projectId) {
        List<Long> teamIds = new ArrayList<>();
        Query query = Query
                .builder()
                .table(UserProjectTable.NAME)
                .where(String.format("%s = ?", UserProjectTable.PROJECT_ID_COLUMN))
                .whereArgs(projectId)
                .build();


        try (Cursor c = storIOSQLite.lowLevel().query(query)) {
            while (c.moveToNext()) {
                teamIds.add(c.getLong(c.getColumnIndex(UserProjectTable.PROJECT_ID_COLUMN)));
            }
        }
        return teamIds;
    }
}
