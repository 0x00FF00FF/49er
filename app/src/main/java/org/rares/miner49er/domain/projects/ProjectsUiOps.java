package org.rares.miner49er.domain.projects;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import io.reactivex.functions.Consumer;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.domain.projects.adapter.ProjectsAdapter;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.ui.actionmode.ProjectsActionModeCallback;

import java.util.List;


/**
 * @author rares
 * @since 01.03.2018
 */

public class ProjectsUiOps extends ResizeableItemsUiOps implements ProjectsActionModeCallback.ActionListener {

    @Setter
    private ProjectsInterfaces.ProjectsResizeListener projectsListResizeListener;

    private static final String TAG = ProjectsUiOps.class.getSimpleName();

    private ProjectsRepository projectsRepository;

    private ProjectsActionModeCallback callback = null;

    public ProjectsUiOps(RecyclerView rv) {
//        Miner49erApplication.getRefWatcher(activity).watch(this);
        setRv(rv);
        projectsRepository = new ProjectsRepository();
        repository = projectsRepository;

        selectedDrawableRes = R.drawable.transient_semitransparent_rectangle_tr_bl;
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

    @Override
    public boolean onListItemClick(ResizeableItemViewHolder holder) {
        final ListState state = getRvState();
        boolean enlarge = super.onListItemClick(holder);

        ProjectsAdapter adapter = (ProjectsAdapter) getRv().getAdapter();
        int projectId = ((ProjectData) adapter.getDisplayData(holder.getAdapterPosition())).getId();
        if (callback == null) {
            // TODO
            Toolbar t = ((AppCompatActivity) getRv().getContext()).findViewById(R.id.toolbar_c);
            callback = new ProjectsActionModeCallback(t);
            callback.setActionListener(this);
        }

        callback.setTitle(holder.getLongTitle())
                .setSubtitle(holder.getInfoLabelString())
                .setProjectId(projectId);

        if (enlarge) {
            callback.endActionMode();
        } else {
            if (state == ListState.LARGE) {
                callback.startActionMode();
            }
        }

        return enlarge;
    }

    @Override
    public void onEndActionMode() {
        ProjectsAdapter adapter = (ProjectsAdapter) getRv().getAdapter();
        ResizeableItemViewHolder holder =
                (ResizeableItemViewHolder) getRv().findViewHolderForAdapterPosition(adapter.getLastSelectedPosition());
        onListItemClick(holder);
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
