package org.rares.miner49er.issues.model;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class IssuesDiff extends DiffUtil.Callback {

    private List<IssueData> oldIssueData;
    private List<IssueData> newIssueData;

    IssuesDiff() {
    }

    IssuesDiff(List<IssueData> p1, List<IssueData> p2) {
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
}
