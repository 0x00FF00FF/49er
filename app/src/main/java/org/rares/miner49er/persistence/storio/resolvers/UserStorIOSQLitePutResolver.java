package org.rares.miner49er.persistence.storio.resolvers;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery;
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery;
import org.rares.miner49er.persistence.entities.User;

/**
 * Generated resolver for Put Operation.
 */
public class UserStorIOSQLitePutResolver extends DefaultPutResolver<User> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public InsertQuery mapToInsertQuery(@NonNull User object) {
        return InsertQuery.builder()
                .table("users")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public UpdateQuery mapToUpdateQuery(@NonNull User object) {
        return UpdateQuery.builder()
                .table("users")
                .where("_id = ?")
                .whereArgs(object.getId())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public ContentValues mapToContentValues(@NonNull User object) {
        ContentValues contentValues = new ContentValues(8);

        contentValues.put("_id", object.getId());
        contentValues.put("last_updated", object.getLastUpdated());
        contentValues.put("user_name", object.getName());
        contentValues.put("password", object.getPwd());
        contentValues.put("email", object.getEmail());
        contentValues.put("photo_path", object.getPhoto());
        contentValues.put("api_key", object.getApiKey());
        contentValues.put("role", object.getRole());

        return contentValues;
    }

    public static final String TAG = UserStorIOSQLitePutResolver.class.getSimpleName();

//    @NonNull
//    @Override
//    public PutResult performPut(@NonNull StorIOSQLite storIOSQLite, @NonNull User object) {
//        Log.i(TAG, "performPut: >>>>>>>>>> " + object.getId());
//        return super.performPut(storIOSQLite, object);
//    }
}
