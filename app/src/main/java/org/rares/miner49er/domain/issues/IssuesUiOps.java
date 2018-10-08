package org.rares.miner49er.domain.issues;

import android.support.v7.widget.RecyclerView;
import org.rares.miner49er.BaseInterfaces.DomainLink;
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

    public IssuesUiOps() {
        issuesRepository.setup();
        repository = issuesRepository;
    }

    @Override
    public void onParentSelected(ItemViewProperties projectProperties, boolean parentWasEnlarged) {

        IssuesAdapter issuesAdapter = (IssuesAdapter) getRv().getAdapter();

        // the following (if) block is here to resize issue items
        // after selecting another project while one of the
        // issues was selected
        if (issuesAdapter != null) {
            int selected = issuesAdapter.getLastSelectedPosition();
            if (selected != -1) {
                ResizeableItemViewHolder vh = (ResizeableItemViewHolder)
                        getRv().findViewHolderForAdapterPosition(selected);
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
                getRv().setAdapter(null);
                resetRv();
            } else if (issuesAdapter != null) {
                issuesAdapter.clearData();
            }
        } else {
            if (issuesAdapter != null) {
                onParentChanged(projectProperties);
            } else {
                getRv().setAdapter(createNewIssuesAdapter(projectProperties));
            }
        }
        resizeRv(!parentWasEnlarged);
    }

    @Override
    public void onParentChanged(ItemViewProperties itemViewProperties) {
        RecyclerView.LayoutManager _tempLm = getRv().getLayoutManager();
//        AbstractAdapter adapter = (AbstractAdapter) getRv().getAdapter();
//        int lastSelectedPosition = adapter.getLastSelectedPosition();
//        boolean somethingSelected = lastSelectedPosition > -1;
//        getRv().scrollToPosition(somethingSelected ? lastSelectedPosition : 0);

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
            getRv().setAdapter(createNewIssuesAdapter(projectProperties));
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
