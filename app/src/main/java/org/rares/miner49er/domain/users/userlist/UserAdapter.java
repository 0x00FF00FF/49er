package org.rares.miner49er.domain.users.userlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.model.UserDiff;
import org.rares.miner49er.domain.users.userlist.UserInterfaces.PositionListener;

import java.util.List;

import static androidx.recyclerview.widget.DiffUtil.DiffResult.NO_POSITION;

@Builder
public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> implements UserInterfaces.UserItemClickListener {

    @Getter
    private List<UserData> data;
    private String roleDeveloper, roleProjectManager, roleDesigner;
    @Setter
    private List<Long> selectedData;
    private boolean clickable;
    private PositionListener positionListener;

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_list_v2, parent, false);
        UserViewHolder vh = new UserViewHolder(v, clickable);
        vh.setEventListener(this);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserData userData = data.get(position);
//            int otherProjects = projectsDAO.getProjectsForUser(userData);
        int otherProjects = userData.id % 2 == 0 ? 1 : userData.id % 3 == 0 ? 0 : 2;
        String userRole = userData.id % 2 == 0 ? roleDeveloper : userData.id % 3 == 0 ? roleProjectManager : roleDesigner;
        holder.bindData(userData, otherProjects, userRole, selectedData.contains(data.get(position).id));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public void setData(List<UserData> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new UserDiff(data, newData));
        data = newData;
        diffResult.dispatchUpdatesTo(this);
    }

    public long[] getSelectedItems() {
        int size = selectedData.size();
        long[] selectedList = new long[size];
        for (int i = 0; i < size; i++) {
            selectedList[i] = selectedData.get(i);
        }
        return selectedList;
    }

    private void addSelected(Long id) {
        if (!selectedData.contains(id)) {
            selectedData.add(id);
        }
    }

    private void removeSelected(Long id) {
        selectedData.remove(id);
    }

    @Override
    public void onListItemClick(ViewHolder holder) {
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition == NO_POSITION) {
            return;
        }
        UserData userData = data.get(adapterPosition);
        if (selectedData.contains(userData.id)) {
            removeSelected(userData.id);
        } else {
            addSelected(userData.id);
        }
        positionListener.clickedOnUser(userData.id);
    }
}