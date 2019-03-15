package org.rares.miner49er.persistence.dao;

import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.LazyIssueGetResolver;

import java.util.List;

public class IssuesDao implements GenericEntityDao<Issue> {
    private StorIOSQLite storio = StorioFactory.INSTANCE.get();
    private LazyIssueGetResolver getResolver = StorioFactory.INSTANCE.getLazyIssueGetResolver();

    @Override
    public Single<List<Issue>> getAll() {
        return getResolver.getAllAsync(storio).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<Issue>> getAll(long parentId) {
        return getResolver.getAllAsync(storio, parentId).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<Issue>> getMatching(String term) {
        return null;
    }

    @Override
    public Single<Optional<Issue>> get(long id) {
        return getResolver.getByIdAsync(storio, id).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> insert(Issue toInsert) {
//        assertInsertReady(toInsert);
        return storio.put()
                .object(toInsert)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> {
                    toInsert.setId(res.insertedId());
                    return res.wasInserted();
                });
    }

    @Override
    public Single<Boolean> insert(List<Issue> toInsert) {
        return storio.put()
                .objects(toInsert)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.numberOfInserts() == toInsert.size());
    }

    @Override
    public Single<Boolean> update(Issue toUpdate) {
        return storio.put()
                .object(toUpdate)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::wasUpdated);
    }

    @Override
    public Single<Boolean> update(List<Issue> toUpdate) {
        return storio.put()
                .objects(toUpdate)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.numberOfInserts() + res.numberOfUpdates() == toUpdate.size());
    }

    @Override
    public Single<Boolean> delete(Issue toDelete) {
        return storio.delete()
                .object(toDelete)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(dr -> dr.numberOfRowsDeleted() != 0);
    }

    @Override
    public Single<Boolean> delete(List<Issue> toDelete) {
        return storio.delete()
                .objects(toDelete)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.results().size() == toDelete.size());
    }

/*    @Override
    public Single<Boolean> wipe() {
        return storio.delete()
                .byQuery(DeleteQuery.builder().table(IssueTable.NAME).build())  // TODO: 3/8/19 WHERE lastModified NOT 0
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.affectedTables().size() > 0);
    }*/

    @Getter
    private final static IssuesDao instance = new IssuesDao();
    private IssuesDao() {}
}
