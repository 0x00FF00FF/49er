package org.rares.miner49er.persistence.storio.resolvers;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery;
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.storio.tables.IssueTable;

/**
 * Generated resolver for Put Operation.
 */
public class IssueStorIOSQLitePutResolver extends DefaultPutResolver<Issue> {

    public static final String TAG = IssueStorIOSQLitePutResolver.class.getSimpleName();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public InsertQuery mapToInsertQuery(@NonNull Issue object) {
        return InsertQuery.builder()
                .table("issues")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public UpdateQuery mapToUpdateQuery(@NonNull Issue object) {
        return UpdateQuery.builder()
                .table("issues")
                .where(IssueTable.OBJECT_ID_COLUMN + " = ?")
                .whereArgs(object.getObjectId())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public ContentValues mapToContentValues(@NonNull Issue object) {
        ContentValues contentValues = new ContentValues(7);

        long projectId = object.getProjectId(), userId = object.getOwnerId();

//        Log.i(TAG, "mapToContentValues: " + projectId + " " + userId);
//
//        if (object.getProject() != null) {
//            projectId = object.getProject().getId();
//        }
//
//        if (object.getOwner() != null) {
//            userId=object.getOwner().getId();
//        }

        contentValues.put("_id", object.getId());
        contentValues.put("_project_id", projectId);
        contentValues.put("_user_id", userId);
        contentValues.put("date_added", object.getDateAdded());
        contentValues.put("date_due", object.getDateDue());
        contentValues.put("last_updated", object.getLastUpdated());
        contentValues.put("issue_name", object.getName());
        contentValues.put("objectid", object.getObjectId());

        return contentValues;
    }

//    @NonNull
//    @Override
//    public PutResult performPut(@NonNull StorIOSQLite storIOSQLite, @NonNull Issue object) {
//
////        List<Object> updateList = new ArrayList<>();
////        updateList.add(object.getProject());
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
////        affectedTables.add(IssueTable.NAME);
////        affectedTables.add(UserTable.NAME);
//        affectedTables.add(ProjectTable.NAME);
//
//        return PutResult.newUpdateResult(1, affectedTables);
//    }
}
