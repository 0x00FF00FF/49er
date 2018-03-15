package org.rares.miner49er.issues;

import android.app.Activity;

import org.rares.miner49er.BaseInterfaces;
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

    public IssuesUiOps(Activity activity) {
        super(activity);    // why is this again? TODO: document on "no default constructor".
        setMaxElevation(BaseInterfaces.MAX_ELEVATION_ISSUES);
    }

    @Override
    public void onParentSelected(ItemViewProperties projectProperties, boolean enlarge) {
        getRv().setAdapter(createNewIssuesAdapter(projectProperties));
        resizeItems(getLastSelectedId());
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

    @Override
    public void onListItemClick(ItemViewProperties itemViewProperties) {
        boolean enlarge = resizeItems(itemViewProperties.getItemContainerCustomId());
        resizeRv(enlarge);
        domainLink.onParentSelected(itemViewProperties, enlarge);
    }

    private IssuesAdapter createNewIssuesAdapter(ItemViewProperties projectViewProperties) {
        IssuesAdapter issuesAdapter = new IssuesAdapter(this, NumberUtils.getRandomInt(5, 40));
        issuesAdapter.setParentColor(projectViewProperties.getItemBgColor());
        return issuesAdapter;
    }
}
