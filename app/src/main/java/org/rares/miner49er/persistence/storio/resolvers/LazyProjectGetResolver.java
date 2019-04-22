package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetListOfObjects;
import com.pushtorefresh.storio3.sqlite.operations.get.PreparedGetObject;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import io.reactivex.Single;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.storio.tables.ProjectsTable;

import java.util.List;

/**
 * Generated resolver for Get Operation.
 */
public class LazyProjectGetResolver extends DefaultGetResolver<Project> {

    protected LazyProjectGetResolver getInstance() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Project mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        Project project = new Project();

        project.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        project.setOwnerId(cursor.getLong(cursor.getColumnIndex("_user_id")));
        project.setDateAdded(cursor.getLong(cursor.getColumnIndex("date_added")));
        project.setLastUpdated(cursor.getLong(cursor.getColumnIndex("last_updated")));
        project.setName(cursor.getString(cursor.getColumnIndex("project_name")));
        project.setDescription(cursor.getString(cursor.getColumnIndex("project_description")));
        project.setIcon(cursor.getString(cursor.getColumnIndex("icon_path")));
        project.setPicture(cursor.getString(cursor.getColumnIndex("picture_path")));

        return project;
    }

    public Single<List<Project>> getAllAsync(StorIOSQLite storIOSQLite) {
        return _getAll(storIOSQLite)
                .asRxSingle();
    }

    public Single<List<Project>> getAllAsync(StorIOSQLite storIOSQLite, long userId) {
        return _getAll(storIOSQLite, userId)
                .asRxSingle();
    }


    public Single<List<Project>> getMatchingNameAsync(StorIOSQLite storIOSQLite, String term) {
        return _getMatchingName(storIOSQLite, term)
                .asRxSingle();
    }


    public Single<Optional<Project>> getByIdAsync(StorIOSQLite storIOSQLite, long id) {
        return _getById(storIOSQLite, id)
                .asRxSingle();
    }

    ////

    public List<Project> getAll(StorIOSQLite storIOSQLite) {
        return _getAll(storIOSQLite)
                .executeAsBlocking();
    }


    public List<Project> getAll(StorIOSQLite storIOSQLite, long userId) {
        return _getAll(storIOSQLite, userId)
                .executeAsBlocking();
    }

    public List<Project> getMatchingName(StorIOSQLite storIOSQLite, String term) {
        return _getMatchingName(storIOSQLite, term)
                .executeAsBlocking();
    }

    public Project getById(StorIOSQLite storIOSQLite, long id) {
        return _getById(storIOSQLite, id)
                .executeAsBlocking();
    }

    ////

    private PreparedGetObject<Project> _getById(StorIOSQLite storIOSQLite, long id) {
        return storIOSQLite.get()
                .object(Project.class)
                .withQuery(Query.builder()
                        .table(ProjectsTable.TABLE_NAME)
                        .where(ProjectsTable.COLUMN_ID + " = ?")
                        .whereArgs(id)
                        .build())
                .withGetResolver(getInstance())
                .prepare();
    }

    private PreparedGetListOfObjects<Project> _getAll(StorIOSQLite storIOSQLite) {
        return storIOSQLite
                .get()
                .listOfObjects(Project.class)
                .withQuery(
                        Query.builder()
                                .table(ProjectsTable.TABLE_NAME)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }

    private PreparedGetListOfObjects<Project> _getAll(StorIOSQLite storIOSQLite, long ownerId) {
        return storIOSQLite
                .get()
                .listOfObjects(Project.class)
                .withQuery(
                        Query.builder()
                                .table(ProjectsTable.TABLE_NAME)
                                .where(ProjectsTable.COLUMN_USER_ID + " = ?")
                                .whereArgs(ownerId)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }

    private PreparedGetListOfObjects<Project> _getMatchingName(StorIOSQLite storIOSQLite, String term) {
        return storIOSQLite.get()
                .listOfObjects(Project.class)
                .withQuery(
                        Query.builder()
                                .table(ProjectsTable.TABLE_NAME)
                                .where(ProjectsTable.COLUMN_PROJECT_NAME + " = ? ")
                                .whereArgs(term)
                                .build())
                .withGetResolver(getInstance())
                .prepare();
    }
}
