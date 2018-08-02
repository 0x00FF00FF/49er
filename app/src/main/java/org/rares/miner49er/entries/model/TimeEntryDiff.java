package org.rares.miner49er.entries.model;

import android.support.v7.util.DiffUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class TimeEntryDiff extends DiffUtil.Callback {

    @Getter @Setter
    private List<TimeEntryData> oldTimeEntryData;
    @Getter @Setter
    private List<TimeEntryData> newTimeEntryData;

    public TimeEntryDiff() {
    }

    public TimeEntryDiff(List<TimeEntryData> p1, List<TimeEntryData> p2) {
        oldTimeEntryData = p1;
        newTimeEntryData = p2;
    }

    @Override
    public int getOldListSize() {
        return oldTimeEntryData.size();
    }

    @Override
    public int getNewListSize() {
        return newTimeEntryData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldTimeEntryData.get(oldItemPosition).getId() == newTimeEntryData.get(newItemPosition).getId();
    }

    //only called when the rule set in areItemsTheSame is true
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldTimeEntryData.get(oldItemPosition).compareContents(newTimeEntryData.get(newItemPosition));
    }
}
