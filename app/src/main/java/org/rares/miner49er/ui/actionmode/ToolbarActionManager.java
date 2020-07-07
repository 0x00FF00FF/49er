package org.rares.miner49er.ui.actionmode;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.internal.view.SupportMenuItem;
import org.rares.miner49er.R;
import org.rares.miner49er.ui.custom.spannable.CenteredImageSpan;

import java.util.Stack;

import static androidx.core.internal.view.SupportMenuItem.SHOW_AS_ACTION_ALWAYS;
import static androidx.core.internal.view.SupportMenuItem.SHOW_AS_ACTION_NEVER;
import static androidx.core.internal.view.SupportMenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ENABLED;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.FLAGS;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ICON_ID;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_ID;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_NAME;

public class ToolbarActionManager
        implements
        ActionListenerManager,
        Toolbar.OnMenuItemClickListener {

    public static final String TAG = ToolbarActionManager.class.getSimpleName();

    protected Toolbar toolbar;

    private Stack<MenuActionListener> actionListenerStack = new Stack<>();
    private MenuConfig config = new MenuConfig();

    private final BackClickListener bcl = new BackClickListener();

    private boolean inActionMode = false;

    public ToolbarActionManager(Toolbar toolbar) {
        this.toolbar = toolbar;
        toolbar.setOnMenuItemClickListener(this);
    }

//    @Setter
//    private long entityId;

    /**
     * Adds the listener to the action stack, if necessary.
     * Configures the toolbar with the listener's current configuration.
     *
     * @param listener component that listens to events or commands sent by the manager {@link MenuActionListener}
     */
    @Override
    public void registerActionListener(MenuActionListener listener) {
//        Log.w(TAG, "registerActionListener() called with: listener = [" + listener + "]");
        if (actionListenerStack.isEmpty() || !actionListenerStack.peek().equals(listener)) {

            if (actionListenerStack.contains(listener)) {
                Log.w(TAG, "addActionListener: THE LISTENER IS ALREADY CONTAINED IN THE STACK!");
            }

            actionListenerStack.push(listener);

//            dumpStack();

        }
        configureNextListener();
    }

    /**
     * Removes the listener if it's ready to pop or throws an exception if not.
     *
     * @param listener component that listens to events or commands sent by the manager: {@link MenuActionListener}
     */
    @Override
    public boolean unregisterActionListener(MenuActionListener listener) {
//        Log.w(TAG, "unregisterActionListener() called with: listener = [" + listener + "]");
        if (listener != null && listener.equals(actionListenerStack.peek())) {
            if (actionListenerStack.size() > 1) {
                actionListenerStack.remove(listener);
                Log.d(TAG, "unregisterActionListener: " + listener);
            } else {
                if (!inActionMode) {
                    return false;
                }
            }
            configureNextListener();
            return true;
        } else {
            if (actionListenerStack.contains(listener)) {
                dumpStack();
                throw new IllegalStateException("Someone accidentally the menu back stack. O_o ");
            }
            configureNextListener();
        }
        return false;
    }

    /**
     * @return <code>true</code> if the event has been fully processed by the component <br />
     * <code>false</code> if the event should be also processed by the framework
     */
    public boolean onBackPressed() {
        MenuActionListener listener = actionListenerStack.peek();
        if (listener != null) {
            if (listener.onToolbarBackPressed()) {
                return unregisterActionListener(listener);
            } else {
                return true;
            }
        }
        return false;
    }

    private void startActionMode() {
        inActionMode = true;

        toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.Custom_Toolbar_Title_Action);
        toolbar.setSubtitleTextAppearance(toolbar.getContext(), R.style.Custom_Toolbar_Subtitle);

        toolbar.setNavigationIcon(R.drawable.icon_path_left_arrow);
        toolbar.setNavigationContentDescription(R.string._toolbar_back_button_description);
        toolbar.setNavigationOnClickListener(bcl);

        refreshActionMode();

        // + inAnimation
    }

    private void endActionMode() {
        inActionMode = false;
        // + outAnimation
        toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.Custom_Toolbar_Title);
        toolbar.setNavigationIcon(null);
        toolbar.setSubtitle("");
        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationOnClickListener(null);
        createCustomHomeMenu();
    }

    public void refreshActionMode() {
        if (!inActionMode) {
            Log.w(TAG, "refreshActionMode: not in action mode.");
            return;
        }
        ensureStackNotEmpty();
        actionListenerStack.peek().configureCustomActionMenu(config);

        toolbar.setTitle(config.title);
        if (config.titleRes != 0) {
            toolbar.setTitle(config.titleRes);
        }

        toolbar.setSubtitle(config.subtitle);
        if (config.subtitleRes != 0) {
            toolbar.setSubtitle(config.subtitleRes);
        }
        createCustomActionMenu();
    }

    private void createCustomHomeMenu() {
        Menu menu = toolbar.getMenu();
        menu.clear();
        toolbar.inflateMenu(R.menu.menu_home);

        Context context = toolbar.getContext();

        addIconToMenuItem(context, menu, R.id.action_add_project, R.drawable.icon_path_add, 0, R.string.action_add_project);
        addIconToMenuItem(context, menu, R.id.action_settings, R.drawable.icon_path_settings, 0, 0);
    }

    private void createCustomActionMenu() {
        Menu menu = toolbar.getMenu();
        menu.clear();

        if (config.createGenericMenu) {
            configureGenericMenu();
        }
        if (config.menuId != 0) {
            toolbar.inflateMenu(config.menuId);
        }

        Context context = toolbar.getContext();

        if (config.menuResources != null) {
            for (int i = 0; i < config.menuResources.length; i++) {
                menu
                        .findItem(config.menuResources[i][ITEM_ID])
                        .setEnabled(1 == config.menuResources[i][ENABLED]);
                addIconToMenuItem(
                        context,
                        menu,
                        config.menuResources[i][ITEM_ID],
                        config.menuResources[i][ICON_ID],
                        config.menuResources[i][FLAGS],
                        config.menuResources[i][ITEM_NAME]
                );
            }
        }

        if (config.additionalMenuId != 0) {
            toolbar.inflateMenu(config.additionalMenuId);
        }

        if (config.additionalResources != null) {
            for (int i = 0; i < config.additionalResources.length; i++) {
                menu
                        .findItem(config.additionalResources[i][ITEM_ID])
                        .setEnabled(1 == config.additionalResources[i][ENABLED]);
                addIconToMenuItem(
                        context,
                        menu,
                        config.additionalResources[i][ITEM_ID],
                        config.additionalResources[i][ICON_ID],
                        config.additionalResources[i][FLAGS],
                        config.additionalResources[i][ITEM_NAME]
                );
            }
        }
    }

    private void ensureStackNotEmpty() {
        if (actionListenerStack.empty()) {
            throw new IllegalStateException(
                    "You need to add an action listener to the action stack before starting action mode!");
        }
    }

    private void configureNextListener() {
//        Log.i(TAG, "configureNextListener: title : " + config.title + " " + config.titleRes);
        MenuActionListener listener;
        if (!actionListenerStack.isEmpty()) {
            listener = actionListenerStack.peek();
            if (listener != null) {
                // TODO: 01.04.2019 this gets called twice when unregisterActionListener is called
                // once here and once in refreshActionMode
                listener.configureCustomActionMenu(config);
                if (config.requireActionMode) {
                    if (inActionMode) {
                        refreshActionMode();
                    } else {
                        startActionMode();
                    }
                } else {
                    endActionMode();
                }
            }
        }
    }

    public class MenuConfig {
        /**
         * menuItemId - used to search for the item in the menu
         */
        public static final int ITEM_ID = 0;
        /**
         * icon that should appear next to the item
         */
        public static final int ICON_ID = 1;
        /**
         * show as action flags - describe where to show the menu item
         */
        public static final int FLAGS = 2;
        /**
         * change the item name with this resource
         */
        public static final int ITEM_NAME = 3;
        /**
         * enable/disable the menu item
         */
        public static final int ENABLED = 4;
        /**
         * indicates that the component needs
         * action mode
         */
        public boolean requireActionMode = false;
        /**
         * indicates that the component needs
         * the generic menu
         */
        public boolean createGenericMenu = true;
        /**
         * the id of the menu to inflate
         */
        public int menuId;
        /**
         * THESE VALUES ARE ALWAYS CREATED BY THE
         * {@link ToolbarActionManager TAM}!<br />
         * these are used to inflate
         * icons in the overflow menu (items are
         * not in action mode) <br />
         * first vector enumerates the items <br />
         * second vector should contain the items in the following order <br />
         * <code>[x][0]</code> - <code>ITEM_ID</code>  - menuItemId - used to search for the item in the menu <br />
         * <code>[x][1]</code> - <code>ICON_ID</code>  - iconId - icon that should appear next to the item <br />
         * <code>[x][2]</code> - <code>FLAG   </code>  - show as action flags - describe where to show the menu item <br />
         * <code>[x][3]</code> - <code>ITEM_NAME</code>  - override default item name with this string resource <br />
         * <code>[x][4]</code> - <code>ENABLED</code>  - override default item state (0 = disabled) <br />
         * (see
         * {@link SupportMenuItem#SHOW_AS_ACTION_NEVER},
         * {@link SupportMenuItem#SHOW_AS_ACTION_IF_ROOM},
         * {@link SupportMenuItem#SHOW_AS_ACTION_ALWAYS},
         * {@link SupportMenuItem#SHOW_AS_ACTION_WITH_TEXT},
         * {@link SupportMenuItem#SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW})
         */
        public int[][] menuResources;           // todo: refactor?
        /**
         * Use these values to override the ones in the {@link MenuConfig#menuResources}
         * which are always created by the {@link ToolbarActionManager}.
         */
        public int[][] overrideGenericMenuResources;// todo: refactor?
        /**
         * additional menu to inflate
         */
        public int additionalMenuId = 0;
        /**
         * same as {@link #menuResources}, but for the
         * additional menu
         */
        public int[][] additionalResources;// todo: refactor?
        /**
         * toolbar title
         */
        public String title;
        /**
         * toolbar subtitle
         */
        public String subtitle;
        /**
         * if !0 is set after the string version
         */
        public int titleRes = 0;
        /**
         * if !0 is set after the string version
         */
        public int subtitleRes = 0;
    }

    private void configureGenericMenu() {
        config.menuId = R.menu.menu_generic_actions;
        config.menuResources = new int[8][5];

        config.menuResources[0][ITEM_ID] = R.id.action_add;
        config.menuResources[0][ICON_ID] = R.drawable.icon_path_add;
        config.menuResources[0][FLAGS] = SHOW_AS_ACTION_NEVER;
        config.menuResources[0][ITEM_NAME] = 0;
        config.menuResources[0][ENABLED] = 1;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_add: " + R.drawable.icon_path_add);

        config.menuResources[1][ITEM_ID] = R.id.action_details;
        config.menuResources[1][ICON_ID] = R.drawable.icon_path_details;
        config.menuResources[1][FLAGS] = SHOW_AS_ACTION_NEVER;
        config.menuResources[1][ITEM_NAME] = 0;
        config.menuResources[1][ENABLED] = 1;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_details: " + R.drawable.icon_path_details);

        config.menuResources[2][ITEM_ID] = R.id.action_edit;
        config.menuResources[2][ICON_ID] = R.drawable.icon_path_edit;
        config.menuResources[2][FLAGS] = SHOW_AS_ACTION_NEVER;
        config.menuResources[2][ITEM_NAME] = 0;
        config.menuResources[2][ENABLED] = 1;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_edit: " + R.drawable.icon_path_edit);

        config.menuResources[3][ITEM_ID] = R.id.action_remove;
        config.menuResources[3][ICON_ID] = R.drawable.icon_path_remove;
        config.menuResources[3][FLAGS] = SHOW_AS_ACTION_NEVER;
        config.menuResources[3][ITEM_NAME] = 0;
        config.menuResources[3][ENABLED] = 1;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_remove: " + R.drawable.icon_path_remove);

        config.menuResources[4][ITEM_ID] = R.id.action_favorite;
        config.menuResources[4][ICON_ID] = R.drawable.icon_path_star_empty;
        config.menuResources[4][FLAGS] = SHOW_AS_ACTION_NEVER;
        config.menuResources[4][ITEM_NAME] = 0;
        config.menuResources[4][ENABLED] = 1;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_star_empty: " + R.drawable.icon_path_star_empty);

        config.menuResources[5][ITEM_ID] = R.id.action_search;
        config.menuResources[5][ICON_ID] = R.drawable.icon_path_search;
        config.menuResources[5][FLAGS] = SHOW_AS_ACTION_NEVER;
        config.menuResources[5][ITEM_NAME] = 0;
        config.menuResources[5][ENABLED] = 1;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_search: " + R.drawable.icon_path_search);

        config.menuResources[6][ITEM_ID] = R.id.action_filter;
        config.menuResources[6][ICON_ID] = R.drawable.icon_path_filter;
        config.menuResources[6][FLAGS] = SHOW_AS_ACTION_NEVER;
        config.menuResources[6][ITEM_NAME] = 0;
        config.menuResources[6][ENABLED] = 1;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_filter: " + R.drawable.icon_path_filter);

        config.menuResources[7][ITEM_ID] = R.id.action_sort;
        config.menuResources[7][ICON_ID] = R.drawable.icon_path_sort;
        config.menuResources[7][FLAGS] = SHOW_AS_ACTION_NEVER;
        config.menuResources[7][ITEM_NAME] = 0;
        config.menuResources[7][ENABLED] = 1;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_filter: " + R.drawable.icon_path_filter);
        overrideGenericMenuResources();
    }

    private void overrideGenericMenuResources() {
        if (config.overrideGenericMenuResources == null || config.overrideGenericMenuResources.length == 0) {
            return;
        }
        int overriddenNumber = 0;
        boolean[] overridden = new boolean[config.overrideGenericMenuResources.length];

        for (int i = 0; i < config.menuResources.length; i++) {
            if (overriddenNumber == config.overrideGenericMenuResources.length) {
                break;
            }
            for (int j = 0; j < config.overrideGenericMenuResources.length; j++) {
                if (!overridden[j] && config.overrideGenericMenuResources[j][ITEM_ID] == config.menuResources[i][ITEM_ID]) {
                    config.menuResources[i][ICON_ID] = config.overrideGenericMenuResources[j][ICON_ID];
                    config.menuResources[i][FLAGS] = config.overrideGenericMenuResources[j][FLAGS];
                    config.menuResources[i][ITEM_NAME] = config.overrideGenericMenuResources[j][ITEM_NAME];
                    config.menuResources[i][ENABLED] = config.overrideGenericMenuResources[j][ENABLED];
                    overridden[j] = true;
                    overriddenNumber++;
                }
            }
        }
    }

    public static void addIconToMenuItem(Context context,
                                         Menu menu,
                                         @IdRes int menuItemId,
                                         @DrawableRes int iconId,
                                         int itemFlags,
                                         @StringRes int itemName) {
        MenuItem item = menu.findItem(menuItemId);
        if (item == null) {
            return;
        }

        if (itemName != 0) {
            item.setTitle(context.getResources().getString(itemName));
        }

        if (iconId != 0) {
            SpannableStringBuilder builder;

            builder = new SpannableStringBuilder(" *     " + item.getTitle());

            ImageSpan span = new CenteredImageSpan(context, iconId);

            builder.setSpan(span, 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.replace(1, 2, " ");
            item.setTitle(builder);
        }

        item.setShowAsActionFlags(itemFlags);
    }

    public interface MenuActionListener {   // MenuActionExecutor
        /**
         * @return <code>false</code> = toolbarManager should not unregister this component
         */
        boolean onToolbarBackPressed();

        default void configureCustomActionMenu(MenuConfig config) {
            config.subtitle = "";
            config.createGenericMenu = false;
            config.menuId = R.menu.menu_action_done;
            config.overrideGenericMenuResources = null;

            config.menuResources = new int[1][5];
            config.menuResources[0][ITEM_ID] = R.id.action_add;
            config.menuResources[0][ICON_ID] = 0;
            config.menuResources[0][ITEM_NAME] = 0;
            config.menuResources[0][ENABLED] = 1;
            config.menuResources[0][FLAGS] = SHOW_AS_ACTION_WITH_TEXT | SHOW_AS_ACTION_ALWAYS;

            config.additionalMenuId = 0;
            config.additionalResources = null;
            config.requireActionMode = true;
        }

        GenericMenuActions getMenuActionsProvider();

        long getMenuActionEntityId();

        void setMenuActionEntityId(long entityId);
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        ensureStackNotEmpty();

        int itemId = item.getItemId();
        MenuActionListener menuActionListener = actionListenerStack.peek();
        GenericMenuActions listener = menuActionListener.getMenuActionsProvider();

        if (listener == null) {
            return true;
        }

        switch (itemId) {
            // generic list (applicable to any selected domain):
            case R.id.action_add:
                return listener.add(menuActionListener.getMenuActionEntityId());
            case R.id.action_remove:
                return listener.remove(menuActionListener.getMenuActionEntityId());
            case R.id.action_details:
                return listener.details(menuActionListener.getMenuActionEntityId());
            case R.id.action_edit:
                return listener.edit(menuActionListener.getMenuActionEntityId());
            case R.id.action_favorite:
                return listener.favorite(menuActionListener.getMenuActionEntityId());
            case R.id.action_search:
                return listener.search(menuActionListener.getMenuActionEntityId());
            case R.id.action_filter:
                return listener.filter(menuActionListener.getMenuActionEntityId());
            // the menu that appears on the projects list:
            case R.id.action_add_project:
            case R.id.action_settings:
            default: {
                return listener.menuAction(itemId, menuActionListener.getMenuActionEntityId());
            }
        }
    }

    private class BackClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }

    }

    private void dumpStack() {
        Log.e(TAG, "unregisterActionListener: --- dumppo ---");
        for (int i = 0; i < actionListenerStack.size(); i++) {
            MenuActionListener l = actionListenerStack.get(i);
            Log.e(TAG, "unregisterActionListener: --- " + l);
        }
        Log.e(TAG, "unregisterActionListener: --- dumppo ---");
    }

}
