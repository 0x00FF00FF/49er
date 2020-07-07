package org.rares.miner49er.domain.projects.persistence;

import android.graphics.Color;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.User;

import java.util.ArrayList;
import java.util.List;

public class ProjectConverter implements DaoConverter<Project, ProjectData> {

    private DaoConverter<Issue, IssueData> issueConverter = DaoConverterFactory.of(Issue.class, IssueData.class);
    private DaoConverter<User, UserData> userConverter = DaoConverterFactory.of(User.class, UserData.class);

    private final static String[] localColors = {"#AA7986CB", "#AA5C6BC0"};

    private final String TAG = ProjectConverter.class.getSimpleName();

    @Override
    public Project vmToDm(ProjectData viewModel) {
        if (viewModel == null) {
            return null;
        }

        Project project = new Project();
        project.setId(viewModel.getId());
        project.setName(viewModel.getName());
        project.setIcon(viewModel.getIcon());
        project.setDescription(viewModel.getDescription());
        project.setDateAdded(viewModel.getDateAdded());
        project.setPicture(viewModel.getPicture());
        project.setIcon(viewModel.getIcon());
        project.setLastUpdated(viewModel.getLastUpdated());
        project.setOwnerId(viewModel.parentId);
        project.setTeam(userConverter.vmToDm(viewModel.getTeam()));
        project.setDeleted(viewModel.isDeleted() ? 1 : 0);
        project.setObjectId(viewModel.getObjectId());

        return project;
    }

    @Override
    public ProjectData dmToVm(Project databaseModel) {
        if (databaseModel == null) {
            return null;
        }

        ProjectData converted = new ProjectData();
        converted.setName(databaseModel.getName());
        converted.setIcon(databaseModel.getIcon());
        converted.setId(databaseModel.getId());
        converted.setDescription(databaseModel.getDescription());
        converted.setDateAdded(databaseModel.getDateAdded());
        converted.setPicture(databaseModel.getPicture());
        converted.setIcon(databaseModel.getIcon());
        converted.setLastUpdated(databaseModel.getLastUpdated());
        converted.parentId = databaseModel.getOwnerId();
        converted.setDeleted(databaseModel.getDeleted() != 0);
        converted.setObjectId(databaseModel.getObjectId());
        if (databaseModel.getOwner() != null) {
            converted.setOwner(userConverter.dmToVm(databaseModel.getOwner()));
        }
        if (databaseModel.getIssues() != null) {
            converted.setIssues(issueConverter.dmToVm(databaseModel.getIssues()));
        }
        if (databaseModel.getTeam() != null) {
            converted.setTeam(userConverter.dmToVm(databaseModel.getTeam()));
        }

        return converted;
    }

    @Override
    public List<Project> vmToDm(List<ProjectData> viewModelList) {
        return null;
    }

    @Override
    public List<ProjectData> dmToVm(List<Project> databaseModelList) {
        if (databaseModelList == null) {
            return null;
        }

        ArrayList<ProjectData> viewModels = new ArrayList<>();

        for (int i = 0; i < databaseModelList.size(); i++) {
            Project p = databaseModelList.get(i);
            ProjectData pd = dmToVm(p);
            pd.setColor(Color.parseColor(localColors[i % 2]));
            viewModels.add(pd);
        }

        return viewModels;
    }
}
