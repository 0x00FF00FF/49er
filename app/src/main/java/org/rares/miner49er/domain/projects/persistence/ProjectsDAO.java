package org.rares.miner49er.domain.projects.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.LazyProjectGetResolver;
import org.rares.miner49er.persistence.storio.resolvers.ProjectTeamGetResolver;

import java.util.Collections;
import java.util.List;

@Deprecated
public class ProjectsDAO implements GenericDAO<ProjectData> {

    private static final String TAG = ProjectsDAO.class.getSimpleName();
    private StorIOSQLite storio;

    private LazyProjectGetResolver lazyGetResolver = StorioFactory.INSTANCE.getLazyProjectGetResolver();
    private ProjectTeamGetResolver getResolver = StorioFactory.INSTANCE.getProjectTeamGetResolver();
    private DaoConverter<Project, ProjectData> projectConverter = DaoConverterFactory.of(Project.class, ProjectData.class);

    public static GenericDAO<ProjectData> newInstance() {
        return new ProjectsDAO();
    }

    private ProjectsDAO() {
        storio = StorioFactory.INSTANCE.get();
    }

    @Override
    public List<ProjectData> getAll(boolean lazy) {
        List<Project> list = lazy ?
                lazyGetResolver.getAll(storio) :
                getResolver.getAll(storio);

        return projectConverter.dmToVm(list);
    }

    @Override
    public List<ProjectData> getAll(long parentId, boolean lazy) {
        return Collections.emptyList();
    }

    @Override
    public List<ProjectData> getMatching(String term, boolean lazy) {
        List<Project> list = lazy ?
                lazyGetResolver.getMatchingName(storio, term) :
                getResolver.getMatchingName(storio, term);
        return projectConverter.dmToVm(list);
    }

    @Override
    public ProjectData get(long id, boolean lazy) {     // todo: get projects by user id.
        Project project = lazy ?
                lazyGetResolver.getById(storio, id) :
                getResolver.getById(storio, id);
        return projectConverter.dmToVm(project);
    }

    @Override
    public long insert(ProjectData toInsert) {
        Log.d(TAG, "insert() called with: toInsert = [" + toInsert + "]");
        assertInsertReady(toInsert);
        return storio.put()
                .object(projectConverter.vmToDm(toInsert))
                .prepare()
                .executeAsBlocking()
                .insertedId();
    }

    @Override
    public void update(ProjectData toUpdate) {
        assertUpdateReady(toUpdate);
        boolean updateSuccess = storio.put()
                .object(projectConverter.vmToDm(toUpdate))
                .prepare()
                .executeAsBlocking()
                .wasUpdated();
        Log.d(TAG, "updated: " + toUpdate.getId() + ": " + updateSuccess);
    }

    @Override
    public void delete(ProjectData toDelete) {
        assertDeleteReady(toDelete);

        Project project = projectConverter.vmToDm(toDelete);
        project.setTeam(Collections.emptyList());

        int deletedRows = storio.delete()
                .object(project)
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted();
        Log.d(TAG, "delete: deleted rows: " + deletedRows);
    }

}
