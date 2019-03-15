package org.rares.miner49er.domain.projects.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.LazyProjectGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.ProjectTeamGetResolver;
import org.rares.miner49er.persistence.storio.tables.ProjectsTable;

import java.util.List;

public class AsyncProjectsDao implements AsyncGenericDao<ProjectData> {


    public static AsyncGenericDao<ProjectData> getInstance() {
        return INSTANCE;
    }

    @Override
    public Single<List<ProjectData>> getAll(boolean lazy) {
        return (lazy ?
                lazyGetResolver.getAllAsync(storio) :
                eagerResolver.getAllAsync(storio))
                .subscribeOn(Schedulers.io())
                .map(projectDataList -> projectConverter.dmToVm(projectDataList));
    }

    @Override
    public Single<List<ProjectData>> getAll(long userId, boolean lazy) {
        return (lazy ?
                lazyGetResolver.getAllAsync(storio, userId) :
                eagerResolver.getAllAsync(storio, userId))
                .subscribeOn(Schedulers.io())
                .map(projects -> projectConverter.dmToVm(projects));
    }

    @Override
    public Single<List<ProjectData>> getMatching(String term, boolean lazy) {
        return (lazy ?
                lazyGetResolver.getMatchingNameAsync(storio, term) :
                eagerResolver.getMatchingNameAsync(storio, term))
                .subscribeOn(Schedulers.io())
                .map(projectDataList -> projectConverter.dmToVm(projectDataList));
    }

    @Override
    public Single<Optional<ProjectData>> get(long id, boolean lazy) {
        return (lazy ?
                lazyGetResolver.getByIdAsync(storio, id) :
                eagerResolver.getByIdAsync(storio, id))
                .subscribeOn(Schedulers.io())
                .map(projectOptional ->
                        projectOptional.isPresent() ?
                                Optional.of(projectConverter.dmToVm(projectOptional.get())) :
                                Optional.of(null));
    }

    @Override
    public Single<Long> insert(ProjectData toInsert) {
        Log.d(TAG, "insert() called with: toInsert = [" + toInsert + "]");
        assertInsertReady(toInsert);
        return storio.put()
                .object(projectConverter.vmToDm(toInsert))
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::insertedId);
    }

    @Override
    public Single<Boolean> update(ProjectData toUpdate) {
        assertUpdateReady(toUpdate);
        return storio.put()
                .object(projectConverter.vmToDm(toUpdate))
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(PutResult::wasUpdated);
    }

    @Override
    public Single<Boolean> delete(ProjectData toDelete) {
        assertDeleteReady(toDelete);

        Project project = projectConverter.vmToDm(toDelete);

        return storio.delete()
                .object(project)
                .prepare()
                .asRxSingle()
                .subscribeOn(Schedulers.io())
                .map(dr -> dr.numberOfRowsDeleted() == 0);
    }

    private AsyncProjectsDao() {
    }

    private static final String TAG = AsyncProjectsDao.class.getSimpleName();
    private static final AsyncProjectsDao INSTANCE = new AsyncProjectsDao();

    private StorIOSQLite storio = StorioFactory.INSTANCE.get();
    private LazyProjectGetResolver lazyGetResolver = StorioFactory.INSTANCE.getLazyProjectGetResolver();
    private ProjectTeamGetResolver eagerResolver = StorioFactory.INSTANCE.getProjectTeamGetResolver();

    private DaoConverter<Project, ProjectData> projectConverter = DaoConverterFactory.of(Project.class, ProjectData.class);
    @Getter
    private final Flowable<Changes> dbChangesFlowable = storio.observeChangesInTable(ProjectsTable.TABLE_NAME, BackpressureStrategy.LATEST);
}
