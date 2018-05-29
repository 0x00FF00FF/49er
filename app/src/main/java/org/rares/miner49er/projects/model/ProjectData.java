package org.rares.miner49er.projects.model;


//import com.github.javafaker.Faker;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author rares
 * @since 23.02.2018
 */

public class ProjectData {


//    private Faker faker = Faker.instance();

    @Getter
    @Setter
    String projectName;// = faker.zelda().character();

    @Getter
    @Setter
    String color;

    @Getter
    @Setter
    List<String> issues;

    public boolean areContentsTheSameWith(ProjectData otherData) {
        boolean areIssuesTheSame = true;
        return color.equals(otherData.getColor()) && areIssuesTheSame;
    }
}
