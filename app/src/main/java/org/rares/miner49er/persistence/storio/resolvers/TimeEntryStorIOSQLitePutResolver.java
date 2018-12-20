package org.rares.miner49er.persistence.storio.resolvers;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery;
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery;
import org.rares.miner49er.persistence.entities.TimeEntry;

/**
 * Generated resolver for Put Operation.
 */
public class TimeEntryStorIOSQLitePutResolver extends DefaultPutResolver<TimeEntry> {

    public static final String TAG = TimeEntryStorIOSQLitePutResolver.class.getSimpleName();

    @Override
    @NonNull
    public InsertQuery mapToInsertQuery(@NonNull TimeEntry object) {
        return InsertQuery.builder()
                .table("time_entries")
                .build();
    }

    @Override
    @NonNull
    public UpdateQuery mapToUpdateQuery(@NonNull TimeEntry object) {
        return UpdateQuery.builder()
                .table("time_entries")
                .where("_id = ?")
                .whereArgs(object.getId())
                .build();
    }

    @Override
    @NonNull
    public ContentValues mapToContentValues(@NonNull TimeEntry object) {
        ContentValues contentValues = new ContentValues(8);

        long issueId = object.getIssueId(), userId = object.getUserId();
//        if (object.getIssue() != null) {
//            issueId = object.getIssue().getId();
//        }
//
//        if (object.getUser() != null) {
//            userId = object.getUser().getId();
//        }

        contentValues.put("_id", object.getId());
        contentValues.put("_issue_id", issueId);
        contentValues.put("_user_id", userId);
        contentValues.put("work_date", object.getWorkDate());
        contentValues.put("date_added", object.getDateAdded());
        contentValues.put("last_updated", object.getLastUpdated());
        contentValues.put("hours", object.getHours());
        contentValues.put("comments", object.getComments());

        return contentValues;
    }


//    @NonNull
//    @Override
//    public PutResult performPut(@NonNull StorIOSQLite storIOSQLite, @NonNull TimeEntry object) {
//
////        List<Object> updateList = new ArrayList<>();
////        updateList.add(object.getIssue());
////        updateList.add(object.getUser());
////        updateList.add(object);
//
//        storIOSQLite.put()
//                .object(object)
//                .prepare()
//                .asRxCompletable()
//                .doOnError((x)-> Log.e(TAG, "performPut: oooooo oOOOO", x))
//                .subscribe();
//
//        final Set<String> affectedTables = new HashSet<String>(1);
//
////        affectedTables.add(IssueTable.NAME);
////        affectedTables.add(UserTable.NAME);
//        affectedTables.add(TimeEntryTable.NAME);
//
//        return PutResult.newUpdateResult(1, affectedTables);
//    }


}
