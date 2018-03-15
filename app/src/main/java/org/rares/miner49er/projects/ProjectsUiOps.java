package org.rares.miner49er.projects;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er._abstract.ResizeableViewHolder;
import org.rares.miner49er.projects.ProjectsInterfaces.ProjectsResizeListener;
import org.rares.miner49er.projects.adapter.ProjectsAdapter;

import lombok.Setter;


/**
 * @author rares
 * @since 01.03.2018
 */

public class ProjectsUiOps extends ResizeableItemsUiOps {

    @Setter
    private ProjectsResizeListener projectsListResizeListener;

    private static final String TAG = ProjectsUiOps.class.getSimpleName();

    public ProjectsUiOps(Activity activity) {
        super(activity);
        setMaxElevation(BaseInterfaces.MAX_ELEVATION_PROJECTS);
//        Miner49erApplication.getRefWatcher(activity).watch(this);
    }

    @Override
    public void onListItemClick(ItemViewProperties projectViewProperties) {
        boolean enlarge = resizeItems(
                projectViewProperties.getItemContainerCustomId());

        if (!enlarge) {
            projectsListResizeListener.onProjectsListShrink();
        }
        resizeRv(enlarge);
        domainLink.onParentSelected(projectViewProperties, enlarge);
    }

    /**
     * Removes a project from the list.
     * Calls {@link DomainLink} to act on the event.
     */
    // TODO: 02.03.2018 refactor
    public void removeItem() {
        RecyclerView.LayoutManager llm = getRv().getLayoutManager();
        int itemCount = getRv().getAdapter().getItemCount();
        if (itemCount > 0) {
            View child = llm.getChildAt(0);

            ItemViewProperties projectViewProperties =
                    ((ResizeableViewHolder) getRv().getChildViewHolder(child)).getItemProperties();
            // ^
            if (projectViewProperties.getItemContainerCustomId() == getLastSelectedId()) {
                if (itemCount > 1) {
                    child = llm.getChildAt(1);

                    projectViewProperties =
                            ((ResizeableViewHolder) getRv().getChildViewHolder(child)).getItemProperties();
                    // ^
                    resizeItems(projectViewProperties.getItemContainerCustomId());

                    domainLink.onParentRemoved(projectViewProperties);
                    // ^
                } else {
                    domainLink.onParentRemoved(null);
                    resizeRv(resizeItems(getLastSelectedId()));
                }
            }
        }
        ((ProjectsAdapter) getRv().getAdapter()).removeItem();
        // ^
    }


    /**
     * Called by a {@link org.rares.miner49er.BaseInterfaces.UnbinderHost}
     * when the activity is destroyed.
     */
    @Override
    public void unbind() {
        super.unbind();
        projectsListResizeListener = null;
    }
}
