package org.rares.miner49er.entries.adapter;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.entries.model.TimeEntryData;
import org.rares.miner49er.util.NumberUtils;

import java.util.List;

/**
 * @author rares
 * @since 14.03.2018
 */

public class TimeEntriesAdapter extends AbstractAdapter<TimeEntriesViewHolder> {

    private SortedList<TimeEntryData> sortedData;

    public TimeEntriesAdapter(ResizeableItemsUiOps uiOps, int tempTeNumber) {
        sortedData = new SortedList<>(TimeEntryData.class, timeEntriesCallback);
        ops = uiOps;
        ops.setViewHolderList(viewHolders);
        initializeData(tempTeNumber);
//        setHasStableIds(true);
    }

    private void initializeData(int entries) {
        sortedData.beginBatchedUpdates();
        DateTime dt = new DateTime().minusDays(30);
        for (int i = 0; i < entries; i++) {
            TimeEntryData ted = new TimeEntryData();
            ted.setId(NumberUtils.getNextProjectId());
            ted.setAuthorName("Peter Piper");
            ted.setDate(dt.plusDays(i).getMillis());
            ted.setDateAdded(dt.withDayOfYear(i + 1).getMillis());
            ted.setHours(15);
            sortedData.add(ted);
        }
        sortedData.endBatchedUpdates();
    }

    @Override
    public TimeEntriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();

        View projectItemView =
                LayoutInflater.from(ctx)
                        .inflate(R.layout.resizeable_list_item, parent, false);

        final TimeEntriesViewHolder tevh = new TimeEntriesViewHolder(projectItemView);
        tevh.setItemClickListener(ops);
        tevh.setMaxItemElevation(ops.getMaxElevation() + 2);
        return tevh;
    }

    @Override
    public int getItemCount() {
        return sortedData.size();
    }

    public void onBindViewHolder(TimeEntriesViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.bindData(sortedData.get(position));
    }

    private final SortedListAdapterCallback<TimeEntryData> timeEntriesCallback =
            new SortedListAdapterCallback<TimeEntryData>(this) {
                @Override
                public int compare(TimeEntryData o1, TimeEntryData o2) {
                    return o1.compareTo(o2);
                }

                @Override
                public boolean areContentsTheSame(TimeEntryData oldItem, TimeEntryData newItem) {
                    return oldItem.deepEquals(newItem);
                }

                @Override
                public boolean areItemsTheSame(TimeEntryData item1, TimeEntryData item2) {
                    return item1.equals(item2);
                }
            };
}
