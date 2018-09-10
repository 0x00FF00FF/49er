package org.rares.miner49er;

import butterknife.Unbinder;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableViewHolder;

/**
 * @author rares
 * @since 01.03.2018
 */

public interface BaseInterfaces {

    int MAX_ELEVATION_PROJECTS = 20;
    int MAX_ELEVATION_ISSUES = MAX_ELEVATION_PROJECTS - 4;
    int MAX_ELEVATION_TIME_ENTRIES = MAX_ELEVATION_ISSUES - 4;

    int TAG_ANIMATOR = Integer.MAX_VALUE - 4_1_47012;

    interface UnbinderHost {
        void registerUnbinder(Unbinder unbinder);

        boolean deRegisterUnbinder(Unbinder unbinder);

        void clearBindings();
    }

    interface ResizeableItems {


        /**
         * Used to show the final state of the recycler view.
         * The state changes when a user clicks on a child
         * item of the recycler view, thus changing the
         * width of the recycler view.
         */
        enum ListState {    // TODO: 8/16/18 refactor into ints
            SMALL,
            LARGE
        }

        /**
         * Resets the last selected id.
         */
        void resetLastSelectedId();

        /**
         * Sets the _selected_ item in the adapter.
         * The _selected_ status is not default RV behaviour.
         *
         * @param selectedId - denotes the current selected item's position within the adapter
         * @return - true if a click was performed on an already selected item
         */
        boolean selectItem(int selectedId);
    }

    interface RvResizeListener {
        void onRvGrow();
        void onRvShrink();
    }

    interface ListItemEventListener {
        int ITEM_CLICK_TAG = -9999;

        /**
         * Called when a project was clicked on.
         *
         * @param holder the viewHolder that was clicked upon.
         */
//        void onListItemClick(ItemViewProperties itemViewProperties);
        boolean onListItemClick(ResizeableViewHolder holder);
        void onListItemChanged( ItemViewProperties itemViewProperties);
    }

    interface DomainLink {
        /**
         * Called when a parent is selected from the list.
         *
         * @param viewProperties the transient parent view properties object
         *                       containing information about the selected parent
         * @param enlarge        determines if the parent's width has been
         *                       enlarged or made smaller. The child list's width
         *                       should also be reset.
         */
        void onParentSelected(ItemViewProperties viewProperties, boolean enlarge);

        /**
         * Called when a parent is removed from the list.
         *
         * @param viewProperties the the transient parent view properties object
         *                       containing information about the selected parent
         */
        void onParentRemoved(ItemViewProperties viewProperties);

        void onParentChanged(ItemViewProperties itemViewProperties);
    }
}
