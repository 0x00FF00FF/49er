package org.rares.miner49er.persistence.storio.resolvers;

import android.content.ContentValues;
import android.util.Log;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResults;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.tables.UserProjectTable;

import java.util.ArrayList;
import java.util.List;

public class ProjectTeamPutResolver extends ProjectStorIOSQLitePutResolver {

    private UserProjectPutResolver userProjectPutResolver;

    ProjectTeamPutResolver(UserProjectPutResolver userProjectPutResolver) {
        this.userProjectPutResolver = userProjectPutResolver;
    }

    @NonNull
    @Override
    public PutResult performPut(@NonNull StorIOSQLite storIOSQLite, @NonNull Project project) {

        StorIOSQLite.LowLevel ll = storIOSQLite.lowLevel();



        ll.beginTransaction();

        try {
            final PutResult putResult = super.performPut(storIOSQLite, project);
            final boolean projectWasInserted = putResult.wasInserted();
            final long projectId = projectWasInserted ? putResult.insertedId() : project.getId();

            Log.i(TAG, "performPut: inserted? " + projectWasInserted);

            List<User> team = project.getTeam();
            List<ContentValues> userProjectList = new ArrayList<>(team.size());

            for (User i : team) { // "there is no i in team". well i showed them!
                final ContentValues cv = new ContentValues(2);
                cv.put(UserProjectTable.USER_ID_COLUMN, i.getId());
                cv.put(UserProjectTable.PROJECT_ID_COLUMN, projectId);
                userProjectList.add(cv);
            }

            if (!projectWasInserted) {
                final DeleteResult deleteResult = storIOSQLite
                        .delete()
                        .byQuery(DeleteQuery.builder()
                                .table(UserProjectTable.NAME)
                                .where(UserProjectTable.PROJECT_ID_COLUMN + " = ? ")
                                .whereArgs(projectId).build())
                        .prepare()
                        .executeAsBlocking();
                Log.i(TAG, "performPut: removed all previous u-t-p (" + deleteResult.numberOfRowsDeleted() + ")");
            }

            if (userProjectList.size() > 0) {
                final PutResults<ContentValues> upResult = storIOSQLite
                        .put()
                        .contentValues(userProjectList)
                        .withPutResolver(userProjectPutResolver)
                        .prepare()
                        .executeAsBlocking();
                Log.i(TAG, "performPut: added new u-t-p (" + upResult.numberOfInserts() + "/" + upResult.numberOfUpdates() + ")");
            }else {
                Log.i(TAG, "performPut users size: " + userProjectList.size());
            }

            ll.setTransactionSuccessful();
            return putResult;
        } catch (Exception ex) {
            Log.e(TAG, "performPut: exception: ", ex);
        } finally {
            ll.endTransaction();
        }

        return null;
    }
}
