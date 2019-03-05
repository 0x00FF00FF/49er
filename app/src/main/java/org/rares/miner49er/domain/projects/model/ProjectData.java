package org.rares.miner49er.domain.projects.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AbstractViewModel;

import java.util.Collections;
import java.util.List;

/**
 * @author rares
 * @since 23.02.2018
 */

@ToString
@Getter
@Setter
public class ProjectData extends AbstractViewModel {

    public final String TAG = ProjectData.class.getSimpleName();

//    private long id;

    private String name;
    private String description;
    private String icon;
    private String picture;
    private int color;
    private long dateAdded;

    UserData owner;
    List<UserData> team;
    List<IssueData> issues;

    public boolean compareContents(ProjectData otherData) {
//        Log.i(TAG, "compareContents: " + this.toString() + " | " + otherData.toString());

        return (lastUpdated == otherData.lastUpdated) &&
                (color == otherData.color) &&
                (dateAdded == otherData.dateAdded) &&
                (description == null ? "" : description).equals(otherData.getDescription() == null ? "" : otherData.getDescription()) &&
                (icon == null ? "" : icon).equals(otherData.getIcon() == null ? "" : otherData.getIcon()) &&
                (name == null ? "" : name).equals(otherData.getName() == null ? "" : otherData.getName()) &&
                (issues == null ? Collections.emptyList() : issues).equals(otherData.issues == null ? Collections.emptyList() : otherData.issues) &&
                (team == null ? Collections.emptyList() : team).equals(otherData.team == null ? Collections.emptyList() : otherData.team) &&
                (picture == null ? "" : picture).equals(otherData.getPicture() == null ? "" : otherData.getPicture());
    }

    public void updateData(ProjectData projectData) {
        name = projectData.name;
        description = projectData.description;
        icon = projectData.icon;
        picture = projectData.picture;
        color = projectData.color;
        dateAdded = projectData.dateAdded;

        owner = projectData.owner;
        team = projectData.team;
        issues = projectData.issues;
    }
}
