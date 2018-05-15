package org.rares.miner49er.projects;

import android.support.v7.widget.RecyclerView;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er._abstract.ResizeableViewHolder;
import org.rares.miner49er.layoutmanager.ResizeableLayoutManager;
import org.rares.miner49er.projects.ProjectsInterfaces.ProjectsResizeListener;


/**
 * @author rares
 * @since 01.03.2018
 */

public class ProjectsUiOps extends ResizeableItemsUiOps {

    @Setter
    private ProjectsResizeListener projectsListResizeListener;

    private static final String TAG = ProjectsUiOps.class.getSimpleName();

    public ProjectsUiOps() {
//        Miner49erApplication.getRefWatcher(activity).watch(this);
    }

    @Override
    public void onListItemClick(ResizeableViewHolder holder) {
        int adapterPosition = holder.getAdapterPosition();
        boolean enlarge = selectItem(adapterPosition);      // not very happy about this

        if (!enlarge) {
            projectsListResizeListener.onProjectsListShrink();
        }

//        holder.setIsRecyclable(false);
        domainLink.onParentSelected(holder.getItemProperties(), enlarge);

        RecyclerView.LayoutManager layoutManager = getRv().getLayoutManager();

        if (layoutManager instanceof ResizeableLayoutManager) {
            ResizeableLayoutManager mgr = (ResizeableLayoutManager) layoutManager;
            mgr.setSelectedPosition(enlarge ? -1 : adapterPosition);
            mgr.resizeSelectedView(holder.itemView, enlarge);
        }

        resizeRv(enlarge);
    }

    /**
     * Removes a project from the list.
     * Calls {@link DomainLink} to act on the event.
     */
    // TODO: 02.03.2018 refactor
    public void removeItem() {
//        RecyclerView.LayoutManager llm = getRv().getLayoutManager();
//        int itemCount = getRv().getAdapter().getItemCount();
//        if (itemCount > 0) {
//            View child = llm.getChildAt(0);
//
//            ItemViewProperties projectViewProperties =
//                    ((ResizeableViewHolder) getRv().getChildViewHolder(child)).getItemProperties();
//            // ^
//            if (projectViewProperties.getItemContainerCustomId() == getLastSelectedId()) {
//                if (itemCount > 1) {
//                    child = llm.getChildAt(1);
//
//                    projectViewProperties =
//                            ((ResizeableViewHolder) getRv().getChildViewHolder(child)).getItemProperties();
//                    // ^
//                    resizeItems(projectViewProperties.getItemContainerCustomId());
//
//                    domainLink.onParentRemoved(projectViewProperties);
//                    // ^
//                } else {
//                    domainLink.onParentRemoved(null);
//                    resizeAnimated(resizeItems(getLastSelectedId()));
//                }
//            }
//        }
//        ((ProjectsAdapter) getRv().getAdapter()).removeItem();
//        // ^
    }

}
