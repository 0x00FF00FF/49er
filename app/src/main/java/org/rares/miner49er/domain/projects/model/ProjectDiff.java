package org.rares.miner49er.domain.projects.model;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import org.rares.miner49er.domain.projects.ProjectsInterfaces;

import java.util.List;

public class ProjectDiff extends DiffUtil.Callback {

    private List<ProjectData> oldProjectData;
    private List<ProjectData> newProjectData;

    private static final String TAG = ProjectDiff.class.getSimpleName();

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
        return oldProjectData.get(oldItemPosition).getId() == newProjectData.get(newItemPosition).getId();
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

        Bundle diffBundle = new Bundle();
        if (!newData.getName().equals(oldData.getName())) {
            diffBundle.putString(ProjectsInterfaces.KEY_NAME, newData.getName());
        }
        if (newData.getColor() != oldData.getColor()) {
            diffBundle.putInt(ProjectsInterfaces.KEY_COLOR, newData.getColor());
        }
        if (!newData.getIcon().equals(oldData.getIcon())) {
            diffBundle.putString(ProjectsInterfaces.KEY_ICON, newData.getIcon());
        }
        if (!newData.getPicture().equals(oldData.getPicture())) {
            diffBundle.putString(ProjectsInterfaces.KEY_PICTURE, newData.getPicture());
        }
        if (diffBundle.size() == 0) {
            return null;
        } else {
            diffBundle.putInt("id", newData.getId());
        }
        return diffBundle;

    }


}
