package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.RawQuery;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.tables.UserProjectTable;
import org.rares.miner49er.persistence.storio.tables.UserTable;

import java.util.List;

public class ProjectTeamGetResolver extends ProjectStorIOSQLiteGetResolver {

    @NonNull
    @Override
    public Project mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        Project project = super.mapFromCursor(storIOSQLite, cursor);

        List<User> team = getProjectTeam(storIOSQLite, project.getId());

        project.setTeam(team);
        return project;
    }

    public static List<User> getProjectTeam(StorIOSQLite storIOSQLite, long projectId) {
        return storIOSQLite
                .get()
                .listOfObjects(User.class)
                .withQuery(RawQuery.builder()
                        .query("SELECT * FROM " + UserTable.NAME +
                                " JOIN " + UserProjectTable.NAME +
                                " ON " + UserProjectTable.USER_ID_COLUMN + " = " + UserTable.NAME + "." + UserTable.ID_COLUMN +
                                " AND " + UserProjectTable.PROJECT_ID_COLUMN + " = ?")
                        .args(projectId)
                        .build())
                .prepare()
                .executeAsBlocking();
    }
}
