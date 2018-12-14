package org.rares.miner49er.domain.entries.repository;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.TimeEntryStorIOSQLiteGetResolver;

import java.util.ArrayList;
import java.util.List;

public class TimeEntriesDAO implements GenericDAO<TimeEntryData> {
    private StorIOSQLite storIOSQLite;

    public static TimeEntriesDAO newInstance() {
        return new TimeEntriesDAO();
    }

    private TimeEntriesDAO() {
        storIOSQLite = StorioFactory.INSTANCE.get();
    }

    @Override
    public List<TimeEntryData> getAll() {
        List<TimeEntry> TimeEntries = TimeEntryStorIOSQLiteGetResolver.getAll(storIOSQLite);
        return convertDbModelList(TimeEntries);
    }

    @Override
    public List<TimeEntryData> getMatching(String term) {
        return null;
    }

    @Override
    public TimeEntryData get(long id) {
        TimeEntry TimeEntry = TimeEntryStorIOSQLiteGetResolver.getById(storIOSQLite, id);
        return convertDbModel(TimeEntry);
    }

    @Override
    public long insert(TimeEntryData toInsert) {
        assertInsertReady(toInsert);
        return storIOSQLite
                .put()
                .object(toInsert)
                .prepare()
                .executeAsBlocking()
                .insertedId();
    }

    @Override
    public void update(TimeEntryData toUpdate) {
        assertUpdateReady(toUpdate);
        boolean updateSuccess = storIOSQLite.put()
                .object(convertViewModel(toUpdate))
                .prepare()
                .executeAsBlocking()
                .wasUpdated();
        Log.d(TAG, "updated: " + toUpdate.getId() + ": " + updateSuccess);
    }

    @Override
    public void delete(TimeEntryData toDelete) {
        assertDeleteReady(toDelete);

        int deletedRows = storIOSQLite.delete()
                .object(convertViewModel(toDelete))
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted();
        Log.d(TAG, "delete: deleted rows: " + deletedRows);
    }

    public static List<TimeEntryData> convertDbModelList(List<TimeEntry> entities) {
        if (entities == null) {
            return null;
        }

        ArrayList<TimeEntryData> viewModels = new ArrayList<>();
        for (TimeEntry p : entities) {
            viewModels.add(convertDbModel(p));
        }
        return viewModels;
    }

    public static TimeEntryData convertDbModel(TimeEntry dbModel) {
        if (dbModel == null) {
            return null;
        }

        TimeEntryData viewModel = new TimeEntryData();
        viewModel.setId(dbModel.getId());
        viewModel.setLastUpdated(dbModel.getLastUpdated());
        viewModel.setDateAdded(dbModel.getDateAdded());
        viewModel.setWorkDate(dbModel.getWorkDate());
        viewModel.setComments(dbModel.getComments());
        viewModel.setIssueId(dbModel.getIssueId());
        viewModel.setHours(dbModel.getHours());
        viewModel.setUserId(dbModel.getUserId());
        viewModel.setUserPhoto(dbModel.getUser().getPhoto());
        viewModel.setUserName(dbModel.getUser().getName());
        return viewModel;
    }

    public static TimeEntry convertViewModel(TimeEntryData viewData) {
        if (viewData == null) {
            return null;
        }

        TimeEntry dbModel = new TimeEntry();
        dbModel.setId(viewData.getId());
        dbModel.setLastUpdated(viewData.getLastUpdated());
        dbModel.setDateAdded(viewData.getDateAdded());
        dbModel.setWorkDate(viewData.getWorkDate());
        dbModel.setComments(viewData.getComments());
        dbModel.setIssueId(viewData.getIssueId());
        dbModel.setHours(viewData.getHours());
        dbModel.setUserId(viewData.getUserId());

        return dbModel;
    }
}
