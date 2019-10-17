package org.rares.miner49er.persistence.dao;

import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.LazyProjectGetResolver;

import java.util.List;
import java.util.Map;

public class ProjectsDao implements GenericEntityDao<Project> {
    private StorIOSQLite storio = StorioFactory.INSTANCE.get();
    private LazyProjectGetResolver getResolver = StorioFactory.INSTANCE.getLazyProjectGetResolver();

    @Override
    public Single<List<Project>> getAll() {
        return getResolver.getAllAsync(storio).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<Project>> getAll(long parentId) {
        return getResolver.getAllAsync(storio, parentId).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<Project>> getMatching(String term) {
        return null;
    }

    @Override
    public Single<Optional<Project>> get(long id) {
        return getResolver.getByIdAsync(storio, id).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> insert(Project toInsert) {
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
    public Single<Boolean> insert(List<Project> toInsert) {
        return storio.put()
                .objects(toInsert)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.numberOfInserts() == toInsert.size());
    }

    @Override
    public Single<Boolean> update(Project toUpdate) {
        return storio.put()
                .object(toUpdate)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::wasUpdated);
    }

    @Override
    public Single<Boolean> update(List<Project> toUpdate) {
        return storio.put()
                .objects(toUpdate)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.numberOfInserts() + res.numberOfUpdates() == toUpdate.size());
    }

    @Override
    public Single<Boolean> delete(Project toDelete) {
        return storio.delete()
                .object(toDelete)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(dr -> dr.numberOfRowsDeleted() != 0);
    }

    @Override
    public Single<Boolean> delete(List<Project> toDelete) {
        return storio.delete()
                .objects(toDelete)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.results().size() == toDelete.size());
    }

    @Override
    public Flowable<Project> insertWithResult(List<Project> toInsert) {
        return storio.put()
                .objects(toInsert)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .flatMapPublisher(putResult -> {
                    Map<Project, PutResult> resultMap = putResult.results();
                    return Flowable
                            .fromArray(resultMap.keySet().toArray(new Project[0]))
                            .map(p -> {
                                PutResult result = resultMap.get(p);
                                if (result != null && result.insertedId() != null) {
                                    p.setId(result.insertedId());
                                }
                                return p;
                            });
                })
                .map(project -> {
                    System.out.println("project after saving: " + project);
                    return project;
                })
                .doOnError(Throwable::printStackTrace);
    }

    @Override
    public Single<Project> insertWithResult(Project toInsert) {
        return storio.put()
            .object(toInsert)
            .prepare()
            .asRxSingle()
            .subscribeOn(Schedulers.io())
            .map(putResult -> {
                if (putResult != null && putResult.insertedId() != null) {
                    toInsert.setId(putResult.insertedId());
                }
                System.out.println("project after saving: " + toInsert);
                return toInsert;
            });
    }

    @Override
    public Flowable<Project> getByObjectIdIn(List<String> objectIds) {
        return getResolver.getByObjectIdAsync(storio, objectIds);
    }
/*    @Override
    public Single<Boolean> wipe() {
        return storio.delete()
                .byQuery(DeleteQuery.builder().table(ProjectsTable.TABLE_NAME).build())
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(res -> res.affectedTables().size() > 0);
    }*/

    @Getter
    private final static ProjectsDao instance = new ProjectsDao(); // lombok will create getINSTANCE method if the name is not lowercase

    private ProjectsDao() {
    }
}
