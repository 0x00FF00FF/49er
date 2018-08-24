package org.rares.miner49er.entries.model;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {

        Bundle bundle = new Bundle();
        TimeEntryData newData = newTimeEntryData.get(newItemPosition);
        TimeEntryData oldData = oldTimeEntryData.get(oldItemPosition);

        if (!newData.getUserName().equals(oldData.getUserName())) {
            bundle.putString("UserName", newData.getUserName());
        }
        if(newData.getWorkDate() != oldData.getWorkDate()){
            bundle.putLong("WorkDate", newData.getWorkDate());
        }
        if (!newData.getUserPhoto().equals(oldData.getUserPhoto())) {
            bundle.putString("UserPhoto", newData.getUserPhoto());
        }
        if (newData.getHours() != oldData.getHours()) {
            bundle.putInt("Hours", newData.getHours());
        }
        if (!newData.getComments().equals(oldData.getComments())) {
            bundle.putString("Comments", newData.getComments());
        }
        if (newData.getUserId() != oldData.getUserId()) {
            bundle.putLong("UserId", newData.getUserId());
        }
        if (newData.getColor() != oldData.getColor()) {
            bundle.putInt("Color", newData.getColor());
        }

        if (bundle.size() == 0) {
            return null;
        }
        return bundle;
    }
}
