package org.rares.miner49er.domain.users.model;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class UserDiff extends DiffUtil.Callback {

    private List<UserData> oldUserData;
    private List<UserData> newUserData;

    private static final String TAG = UserDiff.class.getSimpleName();

    UserDiff() {
    }

    public UserDiff(List<UserData> p1, List<UserData> p2) {
        oldUserData = p1;
        newUserData = p2;
    }

    @Override
    public int getOldListSize() {
        return oldUserData.size();
    }

    @Override
    public int getNewListSize() {
        return newUserData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldUserData.get(oldItemPosition).getId().equals(newUserData.get(newItemPosition).getId());
    }

    //only called when the rule set in areItemsTheSame is true
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldUserData.get(oldItemPosition).compareContents(newUserData.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {

        UserData newData = newUserData.get(newItemPosition);
        UserData oldData = oldUserData.get(oldItemPosition);

        Bundle diffBundle = new Bundle();
        if (!newData.getName().equals(oldData.getName())) {
            diffBundle.putString("name", newData.getName());
        }
        if (!newData.getId().equals(oldData.getId())) {
            diffBundle.putLong("id", newData.getId());
        }
        if (newData.getRole() != oldData.getRole()) {
            diffBundle.putInt("role", newData.getRole());
        }
        if (!newData.getEmail().equals(oldData.getEmail())) {
            diffBundle.putString("email", newData.getEmail());
        }
        if (!newData.getPicture().equals(oldData.getPicture())) {
            diffBundle.putString("picture", newData.getPicture());
        }
        if (!newData.getApiKey().equals(oldData.getApiKey())) {
            diffBundle.putString("apiKey", newData.getApiKey());
        }
        if (diffBundle.size() == 0) {
            return null;
        } else {
            diffBundle.putLong("id", newData.getId());
        }
        return diffBundle;

    }


}
