package org.rares.miner49er.projects.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.rares.miner49er.Miner49erApplication;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ResizeableViewHolder;
import org.rares.miner49er.projects.model.ProjectData;

import butterknife.BindView;

/*
* To avoid inner classes, i've split the adapter,
* view holder and the click listener into separate
* classes.
* This minimizes the risk of memory leaks, and
* helps respecting the single responsibility principle.
*
* I hope i can keep up with this and make it a habit
* to not use any inner classes anymore, be they named
* or anonymous.
*/

/**
 * @author rares
 * @since 29.09.2017.
 */

public class ProjectsViewHolder extends ResizeableViewHolder {

    private static final String TAG = ProjectsViewHolder.class.getName();

    @BindView(R.id.tv_resource_name_item)
    TextView projectName;

    private ProjectViewProperties projectViewProperties = new ProjectViewProperties();

    public ProjectsViewHolder(View itemView) {
        super(itemView);
        setItemProperties(projectViewProperties);
//        Miner49erApplication.getRefWatcher(itemView.getContext()).watch(this);
    }

    @Override
    public void bindData(Object o) {
        ProjectData data = (ProjectData) o;
        int itemBgColor = Color.parseColor(data.getColor());
        projectName.setText(data.getProjectName());
        projectViewProperties.setSelected(false);
        projectViewProperties.setText(data.getProjectName());
        projectViewProperties.setItemBgColor(itemBgColor);
        projectViewProperties.setProjectId(114);

        itemView.setBackgroundColor(itemBgColor);
        Log.i(TAG, "ProjectsViewHolder: custom id: " + getItemProperties().getItemContainerCustomId());
    }

}
