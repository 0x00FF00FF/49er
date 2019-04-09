package org.rares.miner49er.domain.users.userlist;

import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

public interface UserInterfaces {
    String KEY_ALL_USERS = "allUsers";
    String KEY_SELECTED_USERS = "selectedUsers";
    String KEY_SELECTED_USERS_CONSUMER ="selectedUsersConsumer";
    String KEY_CLOSE_LISTENER = "closeListener";

    interface UserItemClickListener {
        void onListItemClick(RecyclerView.ViewHolder holder);
    }

    interface SelectedUsersListConsumer extends Serializable {
        void setSelectedList(List<Long> selectedUsersList);
        void fragmentClosed(String tag);
    }
}
