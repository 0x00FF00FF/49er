package org.rares.miner49er.domain.projects.model;

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
public class ProjectData extends AbstractViewModel {

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
        return (color == otherData.color) &&
                (dateAdded == otherData.dateAdded) && 
                (description == null ? "" : description).equals(otherData.getDescription() == null ? "" : otherData.getDescription()) &&
                (icon == null ? "" : icon).equals(otherData.getIcon() == null ? "" : otherData.getIcon()) &&
                (name == null ? "" : name).equals(otherData.getName() == null ? "" : otherData.getName()) &&
                (picture == null ? "" : picture).equals(otherData.getPicture() == null ? "" : otherData.getPicture());
    }

}
