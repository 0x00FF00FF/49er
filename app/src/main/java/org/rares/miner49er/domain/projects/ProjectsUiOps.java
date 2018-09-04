package org.rares.miner49er.domain.projects;

import android.util.Log;
import io.reactivex.functions.Consumer;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er._abstract.ResizeableViewHolder;

import java.util.List;


/**
 * @author rares
 * @since 01.03.2018
 */

public class ProjectsUiOps extends ResizeableItemsUiOps {

    @Setter
    private ProjectsInterfaces.ProjectsResizeListener projectsListResizeListener;

    private static final String TAG = ProjectsUiOps.class.getSimpleName();

    private ProjectsRepository projectsRepository;

    public ProjectsUiOps() {
//        Miner49erApplication.getRefWatcher(activity).watch(this);
        projectsRepository = new ProjectsRepository();
        repository = projectsRepository;
    }

    @Override
    public boolean onListItemClick(ResizeableViewHolder holder) {
        boolean enlarge = super.onListItemClick(holder);
        if (!enlarge) {
            projectsListResizeListener.onProjectsListShrink();
        }
        return enlarge;
    }

    /**
     * Should be called on activity start.
     */
    public void setupRepository() {
        Log.e(TAG, "setupRepository() called");
        projectsRepository
                .setup()
                .registerSubscriber((Consumer<List>) getRv().getAdapter());
    }

    /**
     * Should be called on activity stop.
     */
    public void shutdown() {
        projectsRepository.shutdown();
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
