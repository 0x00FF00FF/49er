package org.rares.miner49er.projects.model;


//import com.github.javafaker.Faker;

import lombok.Data;
import org.rares.miner49er.issues.model.IssueData;

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
    private String color;
    private long dateAdded;

    //    User owner;
//    List<User> team;
    List<IssueData> issues;

    public boolean compareContents(ProjectData otherData) {
        return (color == null ? "" : color).equals(otherData.getColor() == null ? "" : otherData.getColor()) &&
                (description == null ? "" : description).equals(otherData.getDescription() == null ? "" : otherData.getDescription()) &&
                (icon == null ? "" : icon).equals(otherData.getIcon() == null ? "" : otherData.getIcon()) &&
                (name == null ? "" : name).equals(otherData.getName() == null ? "" : otherData.getName()) &&
                (picture == null ? "" : picture).equals(otherData.getPicture() == null ? "" : otherData.getPicture());
    }

}
