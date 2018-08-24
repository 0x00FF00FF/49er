package org.rares.miner49er.entries.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er.entries.model.TimeEntryData;
import org.rares.miner49er.entries.model.TimeEntryDiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author rares
 * @since 14.03.2018
 */

public class TimeEntriesAdapter extends AbstractAdapter<TimeEntriesViewHolder> {

    private List<TimeEntryData> data = new ArrayList<>();

    public TimeEntriesAdapter(BaseInterfaces.ListItemEventListener listener) {
        eventListener = listener;
        setMaxElevation(BaseInterfaces.MAX_ELEVATION_TIME_ENTRIES);
//        setHasStableIds(true);
    }


    @NonNull
    @Override
    public TimeEntriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();

        View projectItemView =
                LayoutInflater.from(ctx)
                        .inflate(R.layout.resizeable_list_item, parent, false);

        final TimeEntriesViewHolder tevh = new TimeEntriesViewHolder(projectItemView);
        tevh.setItemClickListener(eventListener);
//        tevh.setMaxItemElevation(getMaxElevation() + 2);
        return tevh;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void onBindViewHolder(@NonNull TimeEntriesViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.bindData(data.get(position), getLastSelectedPosition() != -1);
    }

    @Override
    public void clearData() {
        updateList(Collections.emptyList());
    }

    @Override
    public String resolveData(int position) {
        return data.get(position).toString();
    }

    private void updateList(List<TimeEntryData> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TimeEntryDiff(this.data, newData));
        data = newData;
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public void accept(List list) throws Exception {
//        Log.d(TAG, "accept! called with: list = [" + list + "]");
        updateList(list);
    }
}
