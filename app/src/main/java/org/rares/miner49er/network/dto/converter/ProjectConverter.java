package org.rares.miner49er.network.dto.converter;

import android.util.Log;
import lombok.Builder;
import org.rares.miner49er.network.dto.ProjectDto;
import org.rares.miner49er.network.dto.UserDto;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.User;

import java.util.ArrayList;
import java.util.List;

@Builder
public class ProjectConverter {

    private static final String TAG = ProjectConverter.class.getSimpleName();

    private final IssueConverter issueConverter;
    private final UserConverter userConverter;

    public Project toModel(ProjectDto dtoProject) {
        Log.i(TAG, "toModel: >>> " + dtoProject.getName());
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
        project.setOwner(userConverter.toModel(dtoProject.getOwner()));
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
            team.add(userConverter.toModel(userDto));
        }
        project.setTeam(team);
        return project;
    }
}
