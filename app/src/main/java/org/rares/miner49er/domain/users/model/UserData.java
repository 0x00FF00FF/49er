package org.rares.miner49er.domain.users.model;


//import com.github.javafaker.Faker;

import lombok.Data;

/**
 * @author rares
 * @since 23.02.2018
 */

@Data
public class UserData {

    private int id;
    private int role;
    private long lastUpdated;
    private String name;
    private String email;
    private String picture;
    private String apiKey;

    public boolean compareContents(UserData otherData) {
        return (id == otherData.id) &&
                (role == otherData.role) &&
                (email == null ? "" : email).equals(otherData.email == null ? "" : otherData.email) &&
                (apiKey == null ? "" : apiKey).equals(otherData.apiKey == null ? "" : otherData.apiKey) &&
                (name == null ? "" : name).equals(otherData.getName() == null ? "" : otherData.getName()) &&
                (picture == null ? "" : picture).equals(otherData.picture == null ? "" : otherData.picture);
    }

}
