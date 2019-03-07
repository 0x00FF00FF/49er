package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import android.util.Log;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.StorioFactory;

import java.util.List;

public class ProjectTeamGetResolver extends ProjectStorIOSQLiteGetResolver {

    private static final String TAG = ProjectTeamGetResolver.class.getSimpleName();

    @NonNull
    @Override
    public Project mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        Log.d(TAG, String.format("mapFromCursor() called from: %s", Thread.currentThread().getName()));
        Project project = super.mapFromCursor(storIOSQLite, cursor);

        List<User> team = StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver().getAll(storIOSQLite, project.getId());
        project.setTeam(team);

        return project;
    }



    protected LazyProjectGetResolver getInstance() {
        return this;
    }
}
