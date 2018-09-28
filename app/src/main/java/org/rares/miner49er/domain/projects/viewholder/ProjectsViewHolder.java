package org.rares.miner49er.domain.projects.viewholder;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.domain.projects.adapter.ProjectViewProperties;
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

@Deprecated
public class ProjectsViewHolder extends ResizeableItemViewHolder {

    private static final String TAG = ProjectsViewHolder.class.getSimpleName();

    @BindView(R.id.tv_resource_name_item)
    TextView projectName;

    @BindView(R.id.project_logo)
    View projectLogo;

    private ProjectViewProperties projectViewProperties = new ProjectViewProperties();

    public ProjectsViewHolder(View itemView) {
        super(itemView);
        setItemProperties(projectViewProperties);
//        Miner49erApplication.getRefWatcher(itemView.getContext()).watch(this);
    }

    @Override
    public void bindData(Object o, boolean shortVersion, boolean selected) {
        ProjectData data = (ProjectData) o;
        int itemBgColor = data.getColor() == 0 ? Color.parseColor("cbbeb5") : data.getColor();
        projectViewProperties.setItemBgColor(itemBgColor);
        projectViewProperties.setId(data.getId());
        shortTitle = TextUtils.extractInitials(data.getName());
        longTitle = data.getName();

        projectName.setText(shortVersion ? shortTitle : longTitle);

        itemView.setBackgroundColor(itemBgColor);
//        Log.i(TAG, "ProjectsViewHolder: custom id: " + getItemProperties().getItemContainerCustomId());
        projectLogo.setVisibility(View.VISIBLE);
    }


    @Override
    public void toggleItemText(boolean shortVersion) {
        projectName.setText(shortVersion ? shortTitle : longTitle);
    }

}
