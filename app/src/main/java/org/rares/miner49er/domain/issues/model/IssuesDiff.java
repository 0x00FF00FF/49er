package org.rares.miner49er.domain.issues.model;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class IssuesDiff extends DiffUtil.Callback {
    private static final String TAG = "IssuesDiff";
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
//        Log.d(TAG, String.format("areItemsTheSame() returned: [%s:%s] %s", oldIssueData.get(oldItemPosition).getId(), newIssueData.get(newItemPosition).getId(), oldIssueData.get(oldItemPosition).getId().equals(newIssueData.get(newItemPosition).getId())));
        return oldIssueData.get(oldItemPosition).getId().equals(newIssueData.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
//        Log.d(TAG, "areContentsTheSame() returned: " + oldIssueData.get(oldItemPosition).compareContents(newIssueData.get(newItemPosition)));
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
        if (!newData.parentId.equals(oldData.parentId)) {
            bundle.putLong("ProjectId", newData.parentId);
        }
        if (newData.getColor() != oldData.getColor()) {
            bundle.putInt("Color", newData.getColor());
        }
        if (newData.getTimeEntries() != oldData.getTimeEntries()) {
            bundle.putString("timeEntries", "timeEntries");
        }
        if (bundle.size() == 0) {
            return null;
        } else {
            bundle.putLong("id", newData.getId());
        }
        return bundle;
    }
}
