package org.rares.miner49er.domain.issues.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.IssueStorIOSQLiteGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.LazyIssueGetResolver;

import java.util.List;

@Deprecated
public class IssuesDAO implements GenericDAO<IssueData> {
    private StorIOSQLite storIOSQLite;

    public static final String TAG = IssuesDAO.class.getSimpleName();
    public static IssuesDAO newInstance() {
        return new IssuesDAO();
    }

    private IssuesDAO() {
        storIOSQLite = StorioFactory.INSTANCE.get();
    }

    private LazyIssueGetResolver lazyIssueGetResolver = StorioFactory.INSTANCE.getLazyIssueGetResolver();
    private IssueStorIOSQLiteGetResolver issueGetResolver = StorioFactory.INSTANCE.getIssueStorIOSQLiteGetResolver();
    private DaoConverter<Issue, IssueData> daoConverter = DaoConverterFactory.of(Issue.class, IssueData.class);

    @Override
    public List<IssueData> getAll(boolean lazy) {
        List<Issue> issues = lazy ?
                lazyIssueGetResolver.getAll(storIOSQLite) :
                issueGetResolver.getAll(storIOSQLite);
        return daoConverter.dmToVm(issues);
    }

    @Override
    public List<IssueData> getAll(long parentId, boolean lazy) {
        List<Issue> issues = lazy ?
                lazyIssueGetResolver.getAll(storIOSQLite, parentId) :
                issueGetResolver.getAll(storIOSQLite, parentId);
        return daoConverter.dmToVm(issues);
    }

    @Override
    public List<IssueData> getMatching(String term, boolean lazy) {
        List<Issue> issues = lazy ?
                lazyIssueGetResolver.getMatchingName(storIOSQLite, term) :
                issueGetResolver.getMatchingName(storIOSQLite, term);
        return daoConverter.dmToVm(issues);
    }

    @Override
    public IssueData get(long id, boolean lazy) {
        Issue issue = lazy ?
                lazyIssueGetResolver.getById(storIOSQLite, id) :
                issueGetResolver.getById(storIOSQLite, id);
        return daoConverter.dmToVm(issue);
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
                .object(daoConverter.vmToDm(toUpdate))
                .prepare()
                .executeAsBlocking()
                .wasUpdated();
        Log.d(TAG, "updated: " + toUpdate.getId() + ": " + updateSuccess);
    }

    @Override
    public void delete(IssueData toDelete) {
        assertDeleteReady(toDelete);

        int deletedRows = storIOSQLite.delete()
                .object(daoConverter.vmToDm(toDelete))
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted();
        Log.d(TAG, "delete: deleted rows: " + deletedRows);
    }


}
