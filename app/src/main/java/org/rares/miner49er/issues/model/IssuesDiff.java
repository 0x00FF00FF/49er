package org.rares.miner49er.issues.model;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

public class IssuesDiff extends DiffUtil.Callback {

    private List<IssueData> oldIssueData;
    private List<IssueData> newIssueData;

    IssuesDiff() {
    }

    public IssuesDiff(List<IssueData> p1, List<IssueData> p2) {
        oldIssueData = p1;
        newIssueData = p2;
    }

    @Override
    public int getOldListSize() {
        return oldIssueData.size();
    }

    @Override
    public int getNewListSize() {
        return newIssueData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldIssueData.get(oldItemPosition).getId() == newIssueData.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldIssueData.get(oldItemPosition).compareContents(newIssueData.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        IssueData oldData = oldIssueData.get(oldItemPosition);
        IssueData newData = newIssueData.get(newItemPosition);

        Bundle bundle = new Bundle();
        if (!newData.getName().equals(oldData.getName())) {
            bundle.putString("Name", newData.getName());
        }
        if (newData.getDateAdded() != (oldData.getDateAdded())) {
            bundle.putLong("DateAdded", newData.getDateAdded());
        }
        if (newData.getProjectId()!=(oldData.getProjectId())) {
            bundle.putInt("ProjectId", newData.getProjectId());
        }
        if (bundle.size() == 0) {
            return null;
        }
        return bundle;
    }
}
