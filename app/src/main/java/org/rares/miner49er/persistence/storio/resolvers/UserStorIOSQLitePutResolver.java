package org.rares.miner49er.persistence.storio.resolvers;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery;
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.tables.UserTable;

import java.io.UnsupportedEncodingException;

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
        .table(UserTable.NAME)
        .where(UserTable.OBJECT_ID_COLUMN + " = ? ")
        .whereArgs(object.getObjectId())
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
    try {
      if (object.getPwd() != null) {
        //noinspection CharsetObjectCanBeUsed
        contentValues.put("password", object.getPwd().getBytes("UTF-16"));
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    contentValues.put("email", object.getEmail());
    contentValues.put("photo_path", object.getPhoto());
    contentValues.put("api_key", object.getApiKey());
    contentValues.put("role", object.getRole());
    contentValues.put("objectid", object.getObjectId());

//    if (object.getApiKey() == null || object.getApiKey().equals("")) {
//      Log.e(TAG, "mapToContentValues: ----------------------------------");
//      Log.e(TAG, "mapToContentValues: >>>> PUT USER WITH NO API KEY <<<<");
//      Log.i(TAG, "mapToContentValues: " + object.getName());
//      Log.e(TAG, "mapToContentValues: ----------------------------------");
//    }

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
