package org.rares.miner49er.issues;

import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
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
    }

    @Override
    public void onParentSelected(ItemViewProperties projectProperties, boolean enlarge) {
        issuesRepository.shutdown();

        if (enlarge) {
            getRv().setAdapter(null);
        } else {
            getRv().swapAdapter(createNewIssuesAdapter(projectProperties), true);
        }
        resizeRv(!enlarge);
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
                .setParentId(projectViewProperties.getId())
                .registerSubscriber(issuesAdapter)
                .refreshData();
        return issuesAdapter;
    }
}
