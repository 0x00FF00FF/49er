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

    private long id;

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
        return color.equals(otherData.getColor()) &&
                description.equals(otherData.getDescription()) &&
                icon.equals(otherData.getIcon()) &&
                name.equals(otherData.getName()) &&
                picture.equals(otherData.getPicture());
    }
}
