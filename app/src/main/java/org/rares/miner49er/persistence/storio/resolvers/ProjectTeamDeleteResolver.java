package org.rares.miner49er.persistence.storio.resolvers;

import android.util.Log;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.storio.tables.UserProjectTable;

public class ProjectTeamDeleteResolver extends ProjectStorIOSQLiteDeleteResolver {
    public static final String TAG = ProjectTeamDeleteResolver.class.getSimpleName();

    @NonNull
    @Override
    public DeleteResult performDelete(@NonNull StorIOSQLite storIOSQLite, @NonNull Project project) {
        DeleteResult deleteUpResult = storIOSQLite
                .delete()
                .byQuery(DeleteQuery.builder()
                        .table(UserProjectTable.NAME)
                        .where(UserProjectTable.PROJECT_ID_COLUMN + " = ? ")
                        .whereArgs(project.getId())
                        .build())
                .prepare()
                .executeAsBlocking();
        DeleteResult deleteResult = super.performDelete(storIOSQLite, project);
        Log.i(TAG, "performDelete: removed: " + deleteResult.numberOfRowsDeleted() + " projects and " + deleteUpResult.numberOfRowsDeleted() + " members.");
        return deleteResult;
    }
}
