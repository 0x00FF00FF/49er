package org.rares.miner49er.domain.users.userlist;

import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;

public interface UserInterfaces {
    String KEY_SELECTED_USERS = "selectedUsers";

    interface PositionListener {
        void clickedOnUser(long userId);
    }

    interface UserItemClickListener {
        void onListItemClick(RecyclerView.ViewHolder holder);
    }

    interface SelectedUsersListConsumer extends Serializable {
        void setSelectedList(long[] selectedUsersList);
        void UsersListFragmentClosed(String tag);
    }
}
