package org.rares.miner49er.persistence.storio.resolvers;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.delete.DeleteResolver;
import com.pushtorefresh.storio3.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import org.rares.miner49er.persistence.storio.tables.UserProjectTable;

public class UserProjectDeleteResolver extends DeleteResolver<ContentValues> {

    @NonNull
    @Override
    public DeleteResult performDelete(@NonNull StorIOSQLite storIOSQLite, @NonNull ContentValues values) {

        StorIOSQLite.LowLevel ll = storIOSQLite.lowLevel();
        ll.beginTransaction();
        try {
            DeleteQuery query = DeleteQuery.builder()
                    .table(UserProjectTable.NAME)
                    .where(UserProjectTable.USER_ID_COLUMN + " = ? AND " + UserProjectTable.PROJECT_ID_COLUMN + " = ? ")
                    .whereArgs(values.get(UserProjectTable.USER_ID_COLUMN), values.get(UserProjectTable.PROJECT_ID_COLUMN))
                    .build();

            final int deletedRows = ll.delete(query);
            DeleteResult deleteResult = DeleteResult.newInstance(deletedRows, UserProjectTable.NAME, "");
            ll.setTransactionSuccessful();
            return deleteResult;
        } finally {
            ll.endTransaction();
        }
    }
}
