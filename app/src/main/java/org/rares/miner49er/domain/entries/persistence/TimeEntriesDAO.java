package org.rares.miner49er.domain.entries.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.LazyTimeEntryGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.TimeEntryStorIOSQLiteGetResolver;

import java.util.List;

@Deprecated
public class TimeEntriesDAO implements GenericDAO<TimeEntryData> {
    private StorIOSQLite storIOSQLite;

    public static final String TAG = TimeEntriesDAO.class.getSimpleName();
    public static TimeEntriesDAO newInstance() {
        return new TimeEntriesDAO();
    }

    private TimeEntryStorIOSQLiteGetResolver timeEntryGetResolver = StorioFactory.INSTANCE.getTimeEntryStorIOSQLiteGetResolver();
    private LazyTimeEntryGetResolver lazyGetResolver = StorioFactory.INSTANCE.getLazyTimeEntryGetResolver();
    private DaoConverter<TimeEntry, TimeEntryData> daoConverter = DaoConverterFactory.of(TimeEntry.class, TimeEntryData.class);

    private TimeEntriesDAO() {
        storIOSQLite = StorioFactory.INSTANCE.get();
    }

    @Override
    public List<TimeEntryData> getAll(boolean lazy) {
        List<TimeEntry> timeEntries =
                lazy ?
                        lazyGetResolver.getAll(storIOSQLite) :
                        timeEntryGetResolver.getAll(storIOSQLite);
        return daoConverter.dmToVm(timeEntries);
    }

    @Override
    public List<TimeEntryData> getAll(long parentId, boolean lazy) {
        List<TimeEntry> timeEntries =
                lazy ?
                        lazyGetResolver.getAll(storIOSQLite, parentId) :
                        timeEntryGetResolver.getAll(storIOSQLite, parentId);
        return daoConverter.dmToVm(timeEntries);
    }

    @Override
    public List<TimeEntryData> getMatching(String term, boolean lazy) {
        return null;
    }

    @Override
    public TimeEntryData get(long id, boolean lazy) {
        TimeEntry timeEntry =
                lazy ?
                        lazyGetResolver.getById(storIOSQLite, id) :
                        timeEntryGetResolver.getById(storIOSQLite, id);
        return daoConverter.dmToVm(timeEntry);
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
                .object(daoConverter.vmToDm(toUpdate))
                .prepare()
                .executeAsBlocking()
                .wasUpdated();
        Log.d(TAG, "updated: " + toUpdate.getId() + ": " + updateSuccess);
    }

    @Override
    public void delete(TimeEntryData toDelete) {
        assertDeleteReady(toDelete);

        int deletedRows = storIOSQLite.delete()
                .object(daoConverter.vmToDm(toDelete))
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted();
        Log.d(TAG, "delete: deleted rows: " + deletedRows);
    }

}
