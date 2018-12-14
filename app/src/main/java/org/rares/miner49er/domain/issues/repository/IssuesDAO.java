package org.rares.miner49er.domain.issues.repository;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.IssueStorIOSQLiteGetResolver;

import java.util.ArrayList;
import java.util.List;

public class IssuesDAO implements GenericDAO<IssueData> {
    private StorIOSQLite storIOSQLite;

    public static IssuesDAO newInstance() {
        return new IssuesDAO();
    }

    private IssuesDAO() {
        storIOSQLite = StorioFactory.INSTANCE.get();
    }

    @Override
    public List<IssueData> getAll() {
        List<Issue> Issues = IssueStorIOSQLiteGetResolver.getAll(storIOSQLite);
        return convertDbModelList(Issues);
    }

    @Override
    public List<IssueData> getMatching(String term) {
        List<Issue> Issues = IssueStorIOSQLiteGetResolver.getMatchingName(storIOSQLite, term);
        return convertDbModelList(Issues);
    }

    @Override
    public IssueData get(long id) {
        Issue Issue = IssueStorIOSQLiteGetResolver.getById(storIOSQLite, id);
        return convertDbModel(Issue);
    }

    @Override
    public long insert(IssueData toInsert) {
        assertInsertReady(toInsert);
        return storIOSQLite
                .put()
                .object(toInsert)
                .prepare()
                .executeAsBlocking()
                .insertedId();
    }

    @Override
    public void update(IssueData toUpdate) {
        assertUpdateReady(toUpdate);
        boolean updateSuccess = storIOSQLite.put()
                .object(convertViewModel(toUpdate))
                .prepare()
                .executeAsBlocking()
                .wasUpdated();
        Log.d(TAG, "updated: " + toUpdate.getId() + ": " + updateSuccess);
    }

    @Override
    public void delete(IssueData toDelete) {
        assertDeleteReady(toDelete);

        int deletedRows = storIOSQLite.delete()
                .object(convertViewModel(toDelete))
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted();
        Log.d(TAG, "delete: deleted rows: " + deletedRows);
    }

    public static List<IssueData> convertDbModelList(List<Issue> entities) {
        if (entities == null) {
            return null;
        }

        ArrayList<IssueData> viewModels = new ArrayList<>();
        for (Issue p : entities) {
            viewModels.add(convertDbModel(p));
        }
        return viewModels;
    }

    public static IssueData convertDbModel(Issue entity) {
        if (entity == null) {
            return null;
        }

        IssueData converted = new IssueData();
        converted.setId(entity.getId());
        converted.setName(entity.getName());
        converted.setLastUpdated(entity.getLastUpdated());
        converted.setDateAdded(entity.getDateAdded());
        converted.setProjectId(entity.getProjectId());
//        converted.setTimeEntries(TimeEntriesDAO.convertDbModelList(entity.getTimeEntries()));
        return converted;
    }

    public static Issue convertViewModel(IssueData viewData) {
        if (viewData == null) {
            return null;
        }

        Issue issue = new Issue();
        issue.setId(viewData.getId());
        issue.setLastUpdated(viewData.getLastUpdated());
        issue.setName(viewData.getName());
        issue.setProjectId(viewData.getProjectId());
        issue.setDateAdded(viewData.getDateAdded());
        issue.setDateDue(viewData.getDateDue());

        return issue;
    }
}
