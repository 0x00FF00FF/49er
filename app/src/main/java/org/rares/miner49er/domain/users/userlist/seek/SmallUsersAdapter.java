package org.rares.miner49er.domain.users.userlist.seek;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import lombok.Getter;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.userlist.UserInterfaces;
import org.rares.miner49er.domain.users.userlist.UserInterfaces.PositionListener;

import java.util.ArrayList;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class SmallUsersAdapter extends RecyclerView.Adapter<SmallUserItemVH>
        implements UserInterfaces.UserItemClickListener {

    private PositionListener positionListener;
    @Getter
    private List<UserData> data = new ArrayList<>();

    public SmallUsersAdapter(PositionListener positionListener) {
        this.positionListener = positionListener;
    }

    @NonNull
    @Override
    public SmallUserItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_small, parent, false);
        return new SmallUserItemVH(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull SmallUserItemVH holder, int position) {
        holder.bind(data.get(position).getPicture());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onListItemClick(ViewHolder holder) {
        int position = holder.getAdapterPosition();
        if (position != NO_POSITION) {
            positionListener.clickedOnUser(data.get(holder.getAdapterPosition()).id);
        }
    }

    public void updateData(List<UserData> newData) {
        data.clear();
        data.addAll(newData);
    }
}