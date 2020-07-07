package org.rares.miner49er;

import android.view.View;
import butterknife.Unbinder;
import io.reactivex.Completable;
import io.reactivex.annotations.Nullable;
import org.rares.miner49er._abstract.ItemViewProperties;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.actionmode.ActionEnforcer.FragmentResultListener;

/**
 * @author rares
 * @since 01.03.2018
 */

public interface BaseInterfaces {

    String DB_NAME = "49er.db";

    int MAX_ELEVATION_PROJECTS = 20;
    int MAX_ELEVATION_ISSUES = MAX_ELEVATION_PROJECTS - 4;
    int MAX_ELEVATION_TIME_ENTRIES = MAX_ELEVATION_ISSUES - 4;

    int UPDATE_INTERVAL = 1_800_000; // 30 min;

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

    String UTFEnc = "UTF-8";

    interface SelectableItemsManager {

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

        /**
         * @return The selected item id in the db. <br />-1 if nothing is selected.
         */
        int getSelectedItemId();

        /**
         * @return The selected item view holder or null.
         */
        @Nullable
        ResizeableItemViewHolder getSelectedViewHolder();
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

    interface Messenger {
        int DISMISSIBLE = 1;
        int UNDOABLE = 2;

        void showMessage(String message, int actionType, Completable action);
//        void showMessage(Snackbar snackbar);
    }

    interface ActionFragmentDependencyProvider {
        ViewModelCache getCache();

        Cache<ProjectData> getProjectDataCache();
        Cache<IssueData> getIssueDataCache();
        Cache<TimeEntryData> getTimeEntryDataCache();
        Cache<UserData> getUserDataCache();

        AsyncGenericDao<ProjectData> getProjectsDAO();
        AsyncGenericDao<IssueData> getIssuesDAO();
        AsyncGenericDao<TimeEntryData> getTimeEntriesDAO();
        AsyncGenericDao<UserData> getUsersDAO();

        View getReplacedView();
        FragmentResultListener getResultListener();
    }
}
