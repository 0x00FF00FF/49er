package org.rares.miner49er.network.dto.converter;

import org.rares.miner49er.network.dto.ProjectDto;
import org.rares.miner49er.network.dto.UserDto;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.User;

import java.util.ArrayList;
import java.util.List;

//@Builder
public class ProjectConverter {

    private static final String TAG = ProjectConverter.class.getSimpleName();

    public static Project toModel(ProjectDto dtoProject) {
        Project project = new Project();
        project.setObjectId(dtoProject.getId());
//        project.setId();
//        project.setOwnerId();
        project.setDateAdded(dtoProject.getDateAdded());
        project.setLastUpdated(System.currentTimeMillis());
        project.setName(dtoProject.getName());
        project.setDescription(dtoProject.getDescription());
        String pic = dtoProject.getIcon() == null ? "default.jpg" : dtoProject.getIcon() ;
        project.setIcon(pic);
        project.setPicture(pic);
        project.setDeleted(0);
        project.setArchived(dtoProject.getArchived() != null ? dtoProject.getArchived() ? 1 : 0 : 0);
        project.setOwner(UserConverter.toModelBlocking(dtoProject.getOwner()));
        List<Issue> issueList = new ArrayList<>();
        for (String issueId : dtoProject.getIssues()) {
            Issue issue = new Issue();
            issue.setObjectId(issueId);
            issue.setOwnerId(-1L);
            issueList.add(issue);
        }
        project.setIssues(issueList);
        List<User> team = new ArrayList<>();
        for (UserDto userDto : dtoProject.getTeam()) {
            team.add(UserConverter.toModelBlocking(userDto));
        }
        project.setTeam(team);
        return project;
    }
}
