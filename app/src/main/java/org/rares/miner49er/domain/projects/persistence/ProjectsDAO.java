package org.rares.miner49er.domain.projects.persistence;

import android.graphics.Color;
import android.util.Log;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.domain.issues.repository.IssuesDAO;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.persistence.UsersDAO;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.ProjectTeamGetResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectsDAO implements GenericDAO<ProjectData> {

    private static final String TAG = ProjectsDAO.class.getSimpleName();
    private StorIOSQLite storio;

    public static GenericDAO<ProjectData> newInstance() {
        return new ProjectsDAO();
    }

    private ProjectsDAO() {
        storio = StorioFactory.INSTANCE.get();
    }

    @Override
    public List<ProjectData> getAll() {
        List<Project> list = ProjectTeamGetResolver.getAll(storio);
        return convertDbModelList(list);
    }

    @Override
    public List<ProjectData> getAll(long id) {
        return Collections.emptyList();
    }

    @Override
    public List<ProjectData> getMatching(String term) {
        List<Project> list = ProjectTeamGetResolver.getMatchingName(storio, term);
        return convertDbModelList(list);
    }

    @Override
    public ProjectData get(long id) {
        Project project = ProjectTeamGetResolver.getById(storio, id);
        return convertDbModel(project);
    }

    @Override
    public long insert(ProjectData toInsert) {
        Log.d(TAG, "insert() called with: toInsert = [" + toInsert + "]");
        assertInsertReady(toInsert);
        return storio.put()
                .object(convertViewModel(toInsert))
                .prepare()
                .executeAsBlocking()
                .insertedId();
    }

    @Override
    public void update(ProjectData toUpdate) {
        assertUpdateReady(toUpdate);
        boolean updateSuccess = storio.put()
                .object(convertViewModel(toUpdate))
                .prepare()
                .executeAsBlocking()
                .wasUpdated();
        Log.d(TAG, "updated: " + toUpdate.getId() + ": " + updateSuccess);
    }

    @Override
    public void delete(ProjectData toDelete) {
        assertDeleteReady(toDelete);

        Project project = convertViewModel(toDelete);
        project.setTeam(Collections.emptyList());

        int deletedRows = storio.delete()
                .object(project)
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted();
        Log.d(TAG, "delete: deleted rows: " + deletedRows);
    }

    public static List<ProjectData> convertDbModelList(List<Project> entities) {
        if (entities == null) {
            return null;
        }

        ArrayList<ProjectData> viewModels = new ArrayList<>();
//        for (Project p : entities) {
//            viewModels.add(convertDbModel(p));
//        }

        for (int i = 0; i < entities.size(); i++) {
            Project p = entities.get(i);
            ProjectData pd = convertDbModel(p);
            pd.setColor(Color.parseColor(localColors[i % 2]));
            viewModels.add(pd);
        }

        return viewModels;
    }

    public static ProjectData convertDbModel(Project entity) {
        if (entity == null) {
            return null;
        }

        ProjectData converted = new ProjectData();
        converted.setName(entity.getName());
        converted.setIcon(entity.getIcon());
        converted.setId(entity.getId());
        converted.setDescription(entity.getDescription());
        converted.setDateAdded(entity.getDateAdded());
        converted.setPicture(entity.getPicture());
        converted.setIcon(entity.getIcon());
        converted.setLastUpdated(entity.getLastUpdated());
        converted.setOwner(UsersDAO.convertDbModel(entity.getOwner()));
        converted.setIssues(IssuesDAO.convertDbModelList(entity.getIssues()));
        converted.setTeam(UsersDAO.convertDbModelList(entity.getTeam()));

        return converted;
    }

    public static Project convertViewModel(ProjectData viewData) {
        if (viewData == null) {
            return null;
        }

        Project project = new Project();
        project.setId(viewData.getId());
        project.setName(viewData.getName());
        project.setIcon(viewData.getIcon());
        project.setDescription(viewData.getDescription());
        project.setDateAdded(viewData.getDateAdded());
        project.setPicture(viewData.getPicture());
        project.setIcon(viewData.getIcon());
        project.setLastUpdated(viewData.getLastUpdated());
        project.setOwnerId(viewData.getOwner().getId());
        project.setTeam(UsersDAO.convertViewModelList(viewData.getTeam()));

        return project;
    }

    private final static String[] localColors = {"#AA7986CB", "#AA5C6BC0"};
}
