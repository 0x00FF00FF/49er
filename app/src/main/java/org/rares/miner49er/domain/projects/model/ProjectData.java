package org.rares.miner49er.domain.projects.model;

import android.util.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AbstractViewModel;

import java.util.List;

/**
 * @author rares
 * @since 23.02.2018
 */

@ToString
@Getter
@Setter
public class ProjectData extends AbstractViewModel implements Cloneable {

    private final String TAG = ProjectData.class.getSimpleName();

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

        boolean teamsEqual = false;
        boolean issuesEqual = false;

        if (team == null) {
            teamsEqual = otherData.team == null;
        } else {
            teamsEqual = otherData.team != null;
        }

        if (issues == null) {
            issuesEqual = otherData.issues == null;
        } else {
            issuesEqual = otherData.issues != null;
        }

        return (lastUpdated == otherData.lastUpdated) &&
                (color == otherData.color) &&
                (deleted == otherData.deleted) &&
                (dateAdded == otherData.dateAdded) &&
                (description == null ? "" : description).equals(otherData.getDescription() == null ? "" : otherData.getDescription()) &&
                (icon == null ? "" : icon).equals(otherData.getIcon() == null ? "" : otherData.getIcon()) &&
                (name == null ? "" : name).equals(otherData.getName() == null ? "" : otherData.getName()) &&
                issuesEqual &&
                teamsEqual &&
                (picture == null ? "" : picture).equals(otherData.getPicture() == null ? "" : otherData.getPicture());
    }

    public void updateData(ProjectData projectData) {
        name = projectData.name;
        description = projectData.description;
        icon = projectData.icon;
        picture = projectData.picture;
        color = projectData.color;
        dateAdded = projectData.dateAdded;
        deleted = projectData.deleted;

        owner = projectData.owner;
        team = projectData.team;
        issues = projectData.issues;
        parentId = projectData.parentId;
        id = projectData.id;
        lastUpdated = projectData.lastUpdated;
    }

    public ProjectData clone() {
        try {
            return (ProjectData) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "clone: operation not supported.", e);
        }
        ProjectData clone = new ProjectData();
        clone.updateData(this);
        return clone;
    }
}
