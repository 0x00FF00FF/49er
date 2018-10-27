package org.rares.miner49er.domain.issues;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.domain.issues.adapter.IssuesAdapter;
import org.rares.miner49er.layoutmanager.ResizeableLayoutManager;

/**
 * @author rares
 * @since 01.03.2018
 */

public class IssuesUiOps extends ResizeableItemsUiOps
        implements
        DomainLink {

    private static final String TAG = IssuesUiOps.class.getSimpleName();
    private IssuesRepository issuesRepository = new IssuesRepository();

    public IssuesUiOps(RecyclerView rv) {
       this.rv=rv;
        issuesRepository.setup();
        repository = issuesRepository;

/*        Resources res = rv.getResources();
        indigo = res.getColor(R.color.indigo_100_grayed);
        white = res.getColor(R.color.pureWhite);
        bgLeft = res.getColor(R.color.semitransparent_black_left_issues);
        bgRight = res.getColor(R.color.semitransparent_black_right);
        bgLeftSelected = res.getColor(R.color.semitransparent_black_left_selected_issues);
        bgRightSelected = res.getColor(R.color.semitransparent_black_right_selected);*/

        Resources res = rv.getResources();
        indigo = res.getColor(R.color.indigo_100_grayed);
        white = res.getColor(R.color.pureWhite);
        bgLeft = res.getColor(R.color.semitransparent_black_left);
        bgRight = res.getColor(R.color.semitransparent_black_right);
        bgLeftSelected = res.getColor(R.color.semitransparent_black_left_selected);
        bgRightSelected = res.getColor(R.color.semitransparent_black_right_selected);
    }

    @Override
    public void onParentSelected(ItemViewProperties projectProperties, boolean parentWasEnlarged) {

        IssuesAdapter issuesAdapter = (IssuesAdapter) rv.getAdapter();

        // the following (if) block is here to resize issue items
        // after selecting another project while one of the
        // issues was selected
        if (issuesAdapter != null) {
            int selected = issuesAdapter.getLastSelectedPosition();
            if (selected != -1) {
                ResizeableItemViewHolder vh = (ResizeableItemViewHolder)
                        rv.findViewHolderForAdapterPosition(selected);
                if (vh != null) {
                    onListItemClick(vh);
                }
                resetLastSelectedId();
                issuesAdapter.setPreviouslySelectedPosition(-1);
            }
        }

        if (parentWasEnlarged) {
            // clear the viewHolders if
            // they reach a certain number
            // so that the chance of leaked
            // context or views will be
            // minimal.
            if (unbinderList.size() > 40) {
                repository.shutdown();
                rv.setAdapter(null);
                resetRv();
            } else if (issuesAdapter != null) {
                issuesAdapter.clearData();
            }
        } else {
            if (issuesAdapter != null) {
                onParentChanged(projectProperties);
            } else {
                rv.setAdapter(createNewIssuesAdapter(projectProperties));
            }
        }
        resizeRv(!parentWasEnlarged);
    }

    @Override
    public void onParentChanged(ItemViewProperties itemViewProperties) {
        RecyclerView.LayoutManager _tempLm = rv.getLayoutManager();
//        AbstractAdapter adapter = (AbstractAdapter) rv.getAdapter();
//        int lastSelectedPosition = adapter.getLastSelectedPosition();
//        boolean somethingSelected = lastSelectedPosition > -1;
//        rv.scrollToPosition(somethingSelected ? lastSelectedPosition : 0);

        if (_tempLm instanceof ResizeableLayoutManager) {
            ((ResizeableLayoutManager) _tempLm).resetState(true);
        }
        issuesRepository.setParentProperties(itemViewProperties);
        issuesRepository.refreshData(true);

//    TODO:    trigger some thing that shows all info containers at once
    }

    @Override
    public void onParentRemoved(ItemViewProperties projectProperties) {
        if (projectProperties != null) {
            rv.setAdapter(createNewIssuesAdapter(projectProperties));
        }
        resizeRv(true);
        domainLink.onParentRemoved(projectProperties);
    }

    private IssuesAdapter createNewIssuesAdapter(ItemViewProperties projectViewProperties) {
        IssuesAdapter issuesAdapter = new IssuesAdapter(this);
        issuesAdapter.setUnbinderHost(this);
        issuesAdapter.setParentColor(projectViewProperties.getItemBgColor());
        issuesRepository
                .setup()
                .setParentProperties(projectViewProperties)
                .registerSubscriber(issuesAdapter);
        return issuesAdapter;
    }

    @Override
    protected AbstractAdapter createNewAdapter(ItemViewProperties itemViewProperties) {
        return createNewIssuesAdapter(itemViewProperties);
    }
}
