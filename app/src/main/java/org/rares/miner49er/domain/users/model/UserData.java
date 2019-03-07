package org.rares.miner49er.domain.users.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.rares.miner49er.persistence.dao.AbstractViewModel;

/**
 * @author rares
 * @since 23.02.2018
 */

@Getter
@Setter
@ToString
public class UserData extends AbstractViewModel {

    //    private long id;
//    private long lastUpdated;
    private int role;
    private String name;
    private String email;
    private String picture;
    private String apiKey;

    public boolean compareContents(UserData otherData) {
        return (id.equals(otherData.id)) &&
                (role == otherData.role) &&
                (email == null ? "" : email).equals(otherData.email == null ? "" : otherData.email) &&
                (apiKey == null ? "" : apiKey).equals(otherData.apiKey == null ? "" : otherData.apiKey) &&
                (name == null ? "" : name).equals(otherData.getName() == null ? "" : otherData.getName()) &&
                (picture == null ? "" : picture).equals(otherData.picture == null ? "" : otherData.picture);
    }

    public void updateData(UserData newUserData) {
        id = newUserData.id;
        lastUpdated = newUserData.lastUpdated;
        role = newUserData.role;
        name = newUserData.name;
        email = newUserData.email;
        picture = newUserData.picture;
        apiKey = newUserData.apiKey;
    }
}
