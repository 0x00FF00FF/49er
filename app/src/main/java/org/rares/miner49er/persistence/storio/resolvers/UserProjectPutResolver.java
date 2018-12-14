package org.rares.miner49er.persistence.storio.resolvers;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResolver;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery;
import org.rares.miner49er.persistence.storio.tables.UserProjectTable;

public class UserProjectPutResolver extends PutResolver<ContentValues> {

    @NonNull
    @Override
    public PutResult performPut(@NonNull StorIOSQLite storIOSQLite, @NonNull ContentValues values) {
        StorIOSQLite.LowLevel ll = storIOSQLite.lowLevel();

        ll.beginTransaction();

        try {
            InsertQuery insertQuery = InsertQuery.builder().table(UserProjectTable.NAME).build();

            final long insertedId = ll.insert(insertQuery, values);
            final PutResult result = PutResult.newInsertResult(insertedId, insertQuery.table());

            ll.setTransactionSuccessful();

            return result;
        } finally {
            ll.endTransaction();
        }
    }
}
