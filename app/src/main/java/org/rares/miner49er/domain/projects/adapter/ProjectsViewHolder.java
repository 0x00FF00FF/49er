package org.rares.miner49er.domain.projects.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ResizeableViewHolder;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.util.TextUtils;

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

    private static final String TAG = ProjectsViewHolder.class.getSimpleName();

    @BindView(R.id.tv_resource_name_item)
    TextView projectName;

    private ProjectViewProperties projectViewProperties = new ProjectViewProperties();

    public ProjectsViewHolder(View itemView) {
        super(itemView);
        setItemProperties(projectViewProperties);
//        Miner49erApplication.getRefWatcher(itemView.getContext()).watch(this);
    }

    @Override
    public void bindData(Object o, boolean shortVersion) {
        ProjectData data = (ProjectData) o;
        int itemBgColor = data.getColor() == 0 ? Color.parseColor("cbbeb5") : data.getColor();
        projectViewProperties.setItemBgColor(itemBgColor);
        projectViewProperties.setId(data.getId());
        if (shortVersion) {
            projectName.setText(TextUtils.extractInitials(data.getName()));
        } else {
            projectName.setText(data.getName());
        }
        itemView.setBackgroundColor(itemBgColor);
//        Log.i(TAG, "ProjectsViewHolder: custom id: " + getItemProperties().getItemContainerCustomId());
    }

}
