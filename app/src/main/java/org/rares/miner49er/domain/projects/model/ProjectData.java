package org.rares.miner49er.domain.projects.model;

import android.util.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AbstractViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
            if (otherData.team != null) {
                teamsEqual = deepEqualsUsers(team, otherData.team);
            }
        }

        if (issues == null) {
            issuesEqual = otherData.issues == null;
        } else {
            if (otherData.issues != null) {
                issuesEqual = deepEqualsIssues(issues, otherData.issues);
            }
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

    public ProjectData clone(boolean shallow) {
        return shallow ? shallowClone() : deepClone();
    }

    private ProjectData deepClone() {
        ProjectData clone = new ProjectData();
        clone.updateData(this);
        if (this.issues != null) {
            List<IssueData> issues = new ArrayList<>();
            for (IssueData id : this.issues) {
                issues.add(id.clone(true)); // shallow (?)
            }
            clone.issues = issues;
        }
        if (this.team != null) {
            List<UserData> team = new ArrayList<>();
            for (UserData i : this.team) {
                team.add(i.clone()); // shallow
            }
            clone.team = team;
        }
        if (owner != null) {
            clone.owner = owner.clone(); // shallow
        }
        return clone;
    }

    private ProjectData shallowClone() {
        try {
            return (ProjectData) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "clone: operation not supported.", e);
        }
        ProjectData clone = new ProjectData();
        clone.updateData(this);
        return clone;
    }

    private boolean deepEqualsIssues(List<IssueData> list1, List<IssueData> list2) {
        if (list1 == null && list2 != null || list1 != null && list2 == null) {
            return false;
        }
        if (list1 == null && list2 == null) {
            return true;
        }
        if (list1.size() == list2.size()) {
            Comparator<IssueData> c = (te1, te2) -> te1.id > te2.id ? 1 : te1.id.equals(te2.id) ? 0 : -1;
            Collections.sort(list1, c);
            Collections.sort(list2, c);
            for (int i = 0; i < list1.size(); i++) {
                if (!list1.get(i).compareContents(list2.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    private boolean deepEqualsUsers(List<UserData> list1, List<UserData> list2) {
        if (list1 == null && list2 != null || list1 != null && list2 == null) {
            return false;
        }
        if (list1 == null && list2 == null) {
            return true;
        }
        if (list1.size() == list2.size()) {
            Comparator<UserData> c = (te1, te2) -> te1.id > te2.id ? 1 : te1.id.equals(te2.id) ? 0 : -1;
            Collections.sort(list1, c);
            Collections.sort(list2, c);
            for (int i = 0; i < list1.size(); i++) {
                if (!list1.get(i).compareContents(list2.get(i))) {      // how to compareContents on the abstract class?
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
