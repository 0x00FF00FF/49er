package org.rares.miner49er.domain.users.userlist;

import androidx.recyclerview.widget.RecyclerView;

public interface UserInterfaces {
    String KEY_SELECTED_USERS = "selectedUsers";

    interface PositionListener {
        void clickedOnUser(long userId);
    }

    interface UserItemClickListener {
        void onListItemClick(RecyclerView.ViewHolder holder);
    }

    interface SelectedUsersListConsumer {
        void setSelectedList(long[] selectedUsersList);
        void UsersListFragmentClosed(String tag);
    }
}
