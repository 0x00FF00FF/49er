package org.rares.miner49er;

import org.rares.miner49er._abstract.ItemViewProperties;

import butterknife.Unbinder;

/**
 * @author rares
 * @since 01.03.2018
 */

public interface BaseInterfaces {

    int MAX_ELEVATION_PROJECTS = 20;
    int MAX_ELEVATION_ISSUES = MAX_ELEVATION_PROJECTS - 4;
    int MAX_ELEVATION_TIME_ENTRIES = MAX_ELEVATION_ISSUES - 4;

    interface UnbinderHost {
        void registerUnbinder(Unbinder unbinder);

        boolean deRegisterUnbinder(Unbinder unbinder);
    }

    interface ResizeableItems {

        /**
         * Resets the last selected id.
         */
        void resetLastSelectedId();

        /**
         * Resize each item view from the holders list.
         *
         * @param selectedId - denotes the current selected item's position within the adapter
         * @return - true if a click was performed on an already selected item
         */
        boolean resizeItems(int selectedId);
    }

    interface ListItemClickListener {
        int ITEM_CLICK_TAG = -9999;

        /**
         * Called when a project was clicked on.
         *
         * @param itemViewProperties the the transient project properties object
         *                           containing information about the selected project
         */
        void onListItemClick(ItemViewProperties itemViewProperties);
    }

    interface DomainLink {
        /**
         * Called when a parent is selected from the list.
         *
         * @param viewProperties the transient parent view properties object
         *                       containing information about the selected parent
         * @param enlarge        determines if a reset of the parent properties has
         *                       taken place. The child list's width should also be reset.
         */
        void onParentSelected(ItemViewProperties viewProperties, boolean enlarge);

        /**
         * Called when a parent is removed from the list.
         *
         * @param viewProperties the the transient parent view properties object
         *                       containing information about the selected parent
         */
        void onParentRemoved(ItemViewProperties viewProperties);
    }
}
