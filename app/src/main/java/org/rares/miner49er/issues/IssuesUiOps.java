package org.rares.miner49er.issues;

import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er._abstract.ResizeableViewHolder;
import org.rares.miner49er.issues.adapter.IssuesAdapter;

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
                onListItemClick((ResizeableViewHolder)
                        getRv().findViewHolderForAdapterPosition(selected));
                resetLastSelectedId();
                issuesAdapter.setPreviouslySelectedPosition(-1);
            }
        }

        if (parentWasEnlarged) {
            if (issuesAdapter != null) {
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
        issuesRepository.setParentProperties(itemViewProperties);
        issuesRepository.refreshData(true);
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
        issuesAdapter.setParentColor(projectViewProperties.getItemBgColor());
        issuesRepository
                .setup()
                .setParentProperties(projectViewProperties)
                .registerSubscriber(issuesAdapter);
        return issuesAdapter;
    }
}
