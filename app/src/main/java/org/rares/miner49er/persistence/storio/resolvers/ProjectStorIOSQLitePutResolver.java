package org.rares.miner49er.persistence.storio.resolvers;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery;
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery;
import org.rares.miner49er.persistence.entities.Project;

/**
 * Generated resolver for Put Operation.
 */
public class ProjectStorIOSQLitePutResolver extends DefaultPutResolver<Project> {

    public static final String TAG = ProjectStorIOSQLitePutResolver.class.getSimpleName();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public InsertQuery mapToInsertQuery(@NonNull Project object) {
        return InsertQuery.builder()
                                        .table("projects")
                                        .build();}

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public UpdateQuery mapToUpdateQuery(@NonNull Project object) {
        return UpdateQuery.builder()
                                        .table("projects")
                                        .where("_id = ?")
                                        .whereArgs(object.getId())
                                        .build();}

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public ContentValues mapToContentValues(@NonNull Project object) {
        ContentValues contentValues = new ContentValues(8);

        long ownerId = object.getOwnerId();
//
//        if (object.getOwner() != null) {
//            ownerId = object.getOwner().getId();
//        }

        contentValues.put("_id", object.getId());
        contentValues.put("_user_id", ownerId);
        contentValues.put("date_added", object.getDateAdded());
        contentValues.put("last_updated", object.getLastUpdated());
        contentValues.put("project_name", object.getName());
        contentValues.put("project_description", object.getDescription());
        contentValues.put("icon_path", object.getIcon());
        contentValues.put("picture_path", object.getPicture());

        return contentValues;
    }

//    @NonNull
//    @Override
//    public PutResult performPut(@NonNull StorIOSQLite storIOSQLite, @NonNull Project object) {
//
////        List<Object> updateList = new ArrayList<>();
////        updateList.add(object.getOwner());
////        updateList.add(object);
//
//        storIOSQLite.put()
//                .object(object)
//                .prepare()
//                .asRxCompletable()
//                .doOnError(x-> Log.e(TAG, "performPut: oioioioio", x))
//                .subscribe();
//
//        final Set<String> affectedTables = new HashSet<String>(2);
//
////        affectedTables.add(UserTable.NAME);
//        affectedTables.add(ProjectTable.NAME);
//
//        return PutResult.newUpdateResult(1, affectedTables);
//    }
}
