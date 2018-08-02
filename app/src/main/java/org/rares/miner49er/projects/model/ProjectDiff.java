package org.rares.miner49er.projects.model;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class ProjectDiff extends DiffUtil.Callback {

    private List<ProjectData> oldProjectData;
    private List<ProjectData> newProjectData;

    ProjectDiff() {
    }

    ProjectDiff(List<ProjectData> p1, List<ProjectData> p2) {
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
}
