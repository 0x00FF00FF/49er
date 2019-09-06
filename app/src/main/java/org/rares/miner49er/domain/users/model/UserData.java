package org.rares.miner49er.domain.users.model;

import android.util.Log;
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
public class UserData extends AbstractViewModel implements Cloneable {

private static final String TAG = UserData.class.getSimpleName();
    private int role;
    private String name;
    private String email;
    private String picture;
    private String apiKey;
    private String password;
    private boolean active;

    public boolean compareContents(UserData otherData) {
        return (id.equals(otherData.id)) &&
                (objectId.equals(otherData.objectId)) &&
                (role == otherData.role) &&
                (email == null ? "" : email).equals(otherData.email == null ? "" : otherData.email) &&
                (apiKey == null ? "" : apiKey).equals(otherData.apiKey == null ? "" : otherData.apiKey) &&
                (name == null ? "" : name).equals(otherData.getName() == null ? "" : otherData.getName()) &&
                (password == null ? "" : password).equals(otherData.getPassword() == null ? "" : otherData.getPassword()) &&
                (picture == null ? "" : picture).equals(otherData.picture == null ? "" : otherData.picture);
    }

    public void updateData(UserData newUserData) {
        id = newUserData.id;
        lastUpdated = newUserData.lastUpdated;
        role = newUserData.role;
        name = newUserData.name;
        email = newUserData.email;
        password = newUserData.password;
        picture = newUserData.picture;
        apiKey = newUserData.apiKey;
        objectId = newUserData.objectId;
    }

    public UserData clone() {
        try {
            return (UserData) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "clone: operation not supported.", e);
        }
        UserData clone = new UserData();
        clone.updateData(this);
        return clone;
    }
}
