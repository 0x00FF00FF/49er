package org.rares.miner49er;

import butterknife.Unbinder;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;

/**
 * @author rares
 * @since 01.03.2018
 */

public interface BaseInterfaces {

    int MAX_ELEVATION_PROJECTS = 20;
    int MAX_ELEVATION_ISSUES = MAX_ELEVATION_PROJECTS - 4;
    int MAX_ELEVATION_TIME_ENTRIES = MAX_ELEVATION_ISSUES - 4;

    short ANIMATION_DURATION = 200;

    interface SetValues {
        byte NOT_SET = -1;
        byte DISABLED = 0;
        byte ENABLED = 1;
    }

    interface ColorAnimation {
        int DO_NOT_TOUCH = 0x00_FF0100;
        int NOT_SET = 0x00_010000;
        int ITEM_DATA = 0x00_000001;
    }

    interface UnbinderHost {
        void registerUnbinder(Unbinder unbinder);

        boolean deRegisterUnbinder(Unbinder unbinder);

        void clearBindings();
    }

    interface ResizeableItems {

        String ANIMATION_WIDTH = "width";
        String ANIMATION_ELEVATION = "elevation";
        String ANIMATION_SOLID_COLOR = "bgColorSolid";
        String ANIMATION_STROKE_COLOR = "strokeColor";
        String ANIMATION_OVERLAY_LEFT_COLOR = "bgColorL";
        String ANIMATION_OVERLAY_RIGHT_COLOR = "bgColorR";

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
        boolean onListItemClick(ResizeableItemViewHolder holder);

        void onListItemChanged(ItemViewProperties itemViewProperties);
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
