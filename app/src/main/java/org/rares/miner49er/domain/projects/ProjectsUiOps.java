package org.rares.miner49er.domain.projects;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import io.reactivex.functions.Consumer;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;

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

    public ProjectsUiOps(RecyclerView rv) {
//        Miner49erApplication.getRefWatcher(activity).watch(this);
        this.rv = rv;
        projectsRepository = new ProjectsRepository();
        repository = projectsRepository;

        Resources res = rv.getResources();
        indigo = res.getColor(R.color.indigo_100_grayed);
        white = res.getColor(R.color.pureWhite);
        bgLeft = res.getColor(R.color.semitransparent_black_left);
        bgRight = res.getColor(R.color.semitransparent_black_right);
        bgLeftSelected = res.getColor(R.color.semitransparent_black_left_selected);
        bgRightSelected = res.getColor(R.color.semitransparent_black_right_selected);

        guideline = ((ViewGroup) rv.getParent()).findViewById(R.id.guideline_projects_end);
    }

    @Override
    public boolean onListItemClick(ResizeableItemViewHolder holder) {
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
                .registerSubscriber((Consumer<List>) rv.getAdapter());
    }

    /**
     * Should be called on activity stop.
     */
    public void shutdown() {
        projectsRepository.shutdown();
    }

    @Override
    protected AbstractAdapter createNewAdapter(ItemViewProperties itemViewProperties) {
        return null;
    }

    /**
     * Removes a project from the list.
     * Calls {@link DomainLink} to act on the event.
     */
    // TODO: 02.03.2018 refactor
    public void removeItem() {
//        RecyclerView.LayoutManager llm = rv.getLayoutManager();
//        int itemCount = rv.getAdapter().getItemCount();
//        if (itemCount > 0) {
//            View child = llm.getChildAt(0);
//
//            ItemViewProperties projectViewProperties =
//                    ((ResizeableViewHolder) rv.getChildViewHolder(child)).getItemProperties();
//            // ^
//            if (projectViewProperties.getItemContainerCustomId() == getLastSelectedId()) {
//                if (itemCount > 1) {
//                    child = llm.getChildAt(1);
//
//                    projectViewProperties =
//                            ((ResizeableViewHolder) rv.getChildViewHolder(child)).getItemProperties();
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
//        ((ProjectsAdapter) rv.getAdapter()).removeItem();
//        // ^
    }

}
