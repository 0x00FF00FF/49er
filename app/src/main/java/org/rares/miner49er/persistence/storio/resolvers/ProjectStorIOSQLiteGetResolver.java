package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import android.util.Log;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.storio.StorioFactory;

/**
 * Generated resolver for Get Operation.
 */
public class ProjectStorIOSQLiteGetResolver extends LazyProjectGetResolver {

    private IssueStorIOSQLiteGetResolver issueGetResolver = new IssueStorIOSQLiteGetResolver();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Project mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        Project project = super.mapFromCursor(storIOSQLite, cursor);

        project.setOwner(StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver().getById(storIOSQLite, project.getOwnerId()));
        project.setIssues(issueGetResolver.getAll(storIOSQLite, project.getId()));

        return project;
    }

    protected LazyProjectGetResolver getInstance() {
        Log.i("ProjectGetResolver", "getInstance: ");
        return this;
    }
}
