package org.rares.miner49er.issues;

import org.rares.miner49er.BaseInterfaces.DomainLink;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.issues.adapter.IssuesAdapter;
import org.rares.miner49er.util.NumberUtils;

/**
 * @author rares
 * @since 01.03.2018
 */

public class IssuesUiOps extends ResizeableItemsUiOps
        implements
        DomainLink {

    private static final String TAG = IssuesUiOps.class.getSimpleName();

    public IssuesUiOps(){}

    @Override
    public void onParentSelected(ItemViewProperties projectProperties, boolean enlarge) {
//        AbstractAdapter adapter = (AbstractAdapter) getRv().getAdapter();
//        final int lastPos = adapter.getLastSelectedPosition();
        getRv().setAdapter(createNewIssuesAdapter(projectProperties));
//        resizeItems(lastPos); //todo: test this.
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
        IssuesAdapter issuesAdapter = new IssuesAdapter(this, NumberUtils.getRandomInt(5, 40));
        issuesAdapter.setParentColor(projectViewProperties.getItemBgColor());
        return issuesAdapter;
    }
}
