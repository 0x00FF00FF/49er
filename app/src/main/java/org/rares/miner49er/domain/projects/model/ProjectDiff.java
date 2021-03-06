package org.rares.miner49er.domain.projects.model;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.ProjectsInterfaces;
import org.rares.miner49er.domain.users.model.UserData;

import java.util.List;

public class ProjectDiff extends DiffUtil.Callback {

    private List<ProjectData> oldProjectData;
    private List<ProjectData> newProjectData;

    private final String TAG = ProjectDiff.class.getSimpleName();

    ProjectDiff() {
    }

    public ProjectDiff(List<ProjectData> p1, List<ProjectData> p2) {
        oldProjectData = p1;
        newProjectData = p2;
    }

    @Override
    public int getOldListSize() {
        return oldProjectData.size();
    }

    @Override
    public int getNewListSize() {
        return newProjectData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldProjectData.get(oldItemPosition).getId().equals(newProjectData.get(newItemPosition).getId());
    }

    //only called when the rule set in areItemsTheSame is true
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldProjectData.get(oldItemPosition).compareContents(newProjectData.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {

        ProjectData newData = newProjectData.get(newItemPosition);
        ProjectData oldData = oldProjectData.get(oldItemPosition);

//        Log.v(TAG, "getChangePayload: project: "+ newData.getName());
        Bundle diffBundle = new Bundle();
        if (!newData.getName().equals(oldData.getName())) {
//            Log.i(TAG, "getChangePayload: name");
            diffBundle.putString(ProjectsInterfaces.KEY_NAME, newData.getName());
        }
        if (newData.getColor() != oldData.getColor()) {
//            Log.i(TAG, "getChangePayload: color");
            diffBundle.putInt(ProjectsInterfaces.KEY_COLOR, newData.getColor());
        }
        if (!newData.getIcon().equals(oldData.getIcon())) {
//            Log.i(TAG, "getChangePayload: icon");
            diffBundle.putString(ProjectsInterfaces.KEY_ICON, newData.getIcon());
        }
        if (!newData.getPicture().equals(oldData.getPicture())) {
//            Log.i(TAG, "getChangePayload: picture");
            diffBundle.putString(ProjectsInterfaces.KEY_PICTURE, newData.getPicture());
        }
        List<IssueData> oldIssueList = oldData.getIssues();
        List<IssueData> newIssueList = newData.getIssues();
        if (oldIssueList == null && newIssueList != null) {
//            Log.i(TAG, "getChangePayload: null, !null");
            diffBundle.putString(ProjectsInterfaces.KEY_ISSUES, "issues");
        }
        if (oldIssueList != null && newIssueList == null) {
//            Log.i(TAG, "getChangePayload: !null, null");
            diffBundle.putString(ProjectsInterfaces.KEY_ISSUES, "issues");
        }
        if (oldIssueList != null && newIssueList != null) {
//            Log.i(TAG, "getChangePayload: " + oldIssueList.size() + " " + newIssueList.size());
            if (oldIssueList.size() != newIssueList.size()) {
//                Log.i(TAG, "getChangePayload: !null, !null " + oldIssueList.size() + " " + newIssueList.size());
                diffBundle.putString(ProjectsInterfaces.KEY_ISSUES, "issues");
            }
        }
        List<UserData> oldUserList = oldData.getTeam();
        List<UserData> newUserList = newData.getTeam();
        if (oldUserList == null && newUserList != null) {
//            Log.i(TAG, "getChangePayload: u null, !null");
            diffBundle.putString(ProjectsInterfaces.KEY_USERS, "users");
        }
        if (oldUserList != null && newUserList == null) {
//            Log.i(TAG, "getChangePayload: u !null, null");
            diffBundle.putString(ProjectsInterfaces.KEY_USERS, "users");
        }
        if (oldUserList != null && newUserList != null) {
//            Log.i(TAG, "getChangePayload: u " + oldUserList.size() + " " + newUserList.size());
            if (oldUserList.size() != newUserList.size()) {
//                Log.i(TAG, "getChangePayload: u !null, !null " + oldUserList.size() + " " + newUserList.size());
                diffBundle.putString(ProjectsInterfaces.KEY_USERS, "users");
            }
        }
        if (diffBundle.size() == 0) {
            return null;
        } else {
            diffBundle.putLong("id", newData.getId());
        }
        return diffBundle;

    }


}
