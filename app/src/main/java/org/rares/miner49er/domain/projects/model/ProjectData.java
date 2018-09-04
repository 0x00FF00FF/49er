package org.rares.miner49er.domain.projects.model;


//import com.github.javafaker.Faker;

import lombok.Data;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.users.model.UserData;

import java.util.List;

/**
 * @author rares
 * @since 23.02.2018
 */

@Data
public class ProjectData {

    private int id;

    private String name;// = faker.zelda().character();
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
