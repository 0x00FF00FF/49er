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
import androidx.appcompat.widget.Toolbar;
import org.rares.miner49er.R;
import org.rares.miner49er.ui.custom.spannable.CenteredImageSpan;

import java.util.Stack;

import static androidx.core.internal.view.SupportMenuItem.SHOW_AS_ACTION_NEVER;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.FLAGS;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ICON_ID;
import static org.rares.miner49er.ui.actionmode.ToolbarActionManager.MenuConfig.ITEM_ID;

// TODO: 11/29/18  singleton this
public class ToolbarActionManager implements Toolbar.OnMenuItemClickListener {

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

    public void startActionMode() {
        inActionMode = true;

        toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.Custom_Toolbar_Title_Action);
        toolbar.setSubtitleTextAppearance(toolbar.getContext(), R.style.Custom_Toolbar_Subtitle);

        toolbar.setNavigationIcon(R.drawable.icon_path_left_arrow);
        toolbar.setNavigationContentDescription(R.string._toolbar_back_button_description);
        toolbar.setNavigationOnClickListener(bcl);

        refreshActionMode();

        createCustomActionMenu();
        // + inAnimation
    }

    public void endActionMode() {
        inActionMode = false;
        // + outAnimation
        toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.Custom_Toolbar_Title);
        toolbar.setNavigationIcon(null);
        toolbar.setSubtitle("");
        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationOnClickListener(null);
        createCustomHomeMenu();
        if (actionListenerStack.size() > 1) {
            removeActionListener(actionListenerStack.get(actionListenerStack.size() - 1)); /// ????
        }
    }

    public void refreshActionMode() {
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
        createCustomActionMenu();   //
    }

    public void addActionListener(MenuActionListener listener) {
        if (actionListenerStack.isEmpty() || !actionListenerStack.peek().equals(listener)) {
            if (actionListenerStack.contains(listener)) {
                Log.w(TAG, "addActionListener: THE LISTENER IS ALREADY CONTAINED IN THE STACK!");
            }
            actionListenerStack.push(listener);
            Log.d(TAG, "Add Action Listener " + listener);
            dumpStack();
        }
    }

    public boolean removeActionListener(MenuActionListener listener) {
        if (listener != null && listener.equals(actionListenerStack.peek())) {
            if (actionListenerStack.size() > 1) {
                actionListenerStack.remove(listener);
                Log.d(TAG, "removeActionListener: " + listener);
                configureNextListener();
                return true;
            }
        } else {
            if (actionListenerStack.contains(listener)) {
                dumpStack();
                throw new IllegalStateException("Someone accidentally the menu back stack. O_o ");
            }
        }
        return false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        ensureStackNotEmpty();
        int itemId = item.getItemId();
        GenericMenuActions listener = actionListenerStack.peek().getMenuActionsProvider();

        if (listener == null) {
            return true;
        }

        switch (itemId) {
            // generic list (applicable to any selected domain):
            case R.id.action_add:
                return listener.add(0);
            case R.id.action_remove:
                return listener.remove(0);
            case R.id.action_details:
                return listener.details(0);
            case R.id.action_edit:
                return listener.edit(0);
            case R.id.action_favorite:
                return listener.favorite(0);
            case R.id.action_search:
                return listener.search(0);
            case R.id.action_filter:
                return listener.filter(0);
            // the menu that appears on the projects list:
            case R.id.action_add_project:
            case R.id.action_settings:
            default: {
                return listener.menuAction(itemId, 0);
            }
        }
    }

    private void createCustomHomeMenu() {
        Menu menu = toolbar.getMenu();
        menu.clear();
        toolbar.inflateMenu(R.menu.menu_home);

        Context context = toolbar.getContext();

        addIconToMenuItem(context, menu, R.id.action_add_project, R.drawable.icon_path_add, 0);
        addIconToMenuItem(context, menu, R.id.action_settings, R.drawable.icon_path_settings, 0);
    }

    private void createCustomActionMenu() {
        Menu menu = toolbar.getMenu();
        menu.clear();

        if (config.createGenericMenu) {
            createGenericMenu();
        }
        if (config.menuId != 0) {
            toolbar.inflateMenu(config.menuId);
        }

        Context context = toolbar.getContext();

        if (config.menuResources != null) {
            for (int i = 0; i < config.menuResources.length; i++) {
                addIconToMenuItem(
                        context,
                        menu,
                        config.menuResources[i][ITEM_ID],
                        config.menuResources[i][ICON_ID],
                        config.menuResources[i][FLAGS]
                );
            }
        }

        if (config.additionalMenuId != 0) {
            toolbar.inflateMenu(config.additionalMenuId);
        }

        if (config.additionalResources != null) {
            for (int i = 0; i < config.additionalResources.length; i++) {
                addIconToMenuItem(
                        context,
                        menu,
                        config.additionalResources[i][ITEM_ID],
                        config.additionalResources[i][ICON_ID],
                        config.additionalResources[i][FLAGS]
                );
            }
        }

//        addIconToMenuItem(context, menu, R.id.action_done, R.drawable.icon_path_done);
    }

    public static void addIconToMenuItem(Context context, Menu menu, @IdRes int menuItemId, @DrawableRes int iconId, int itemFlags) {
        MenuItem item = menu.findItem(menuItemId);
        if (item == null) {
            return;
        }

        SpannableStringBuilder builder;

        builder = new SpannableStringBuilder(" *     " + item.getTitle());

        ImageSpan span = new CenteredImageSpan(context, iconId);

        builder.setSpan(span, 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        item.setTitle(builder);

        item.setShowAsActionFlags(itemFlags);
    }

    public boolean onBackPressed() {
        MenuActionListener listener = actionListenerStack.peek();
        boolean handled = false;
        if (listener != null) {
            final int listeners = actionListenerStack.size();
            final boolean wasInActionMode = inActionMode;
            handled = listener.onToolbarBackPressed();
            // checks to see if onToolbarBackPressed had effect
            Log.i(TAG, "onBackPressed: " + listeners + "|" + wasInActionMode + "|" + inActionMode);
            if (listeners == 1 && wasInActionMode && !inActionMode) {
                return true;
            }
            if (listeners == actionListenerStack.size()) {
                return removeActionListener(listener);
            }
        }
        return false;
    }

    private void ensureStackNotEmpty() {
        if (actionListenerStack.empty()) {
            throw new IllegalStateException(
                    "You need to add an action listener to the action stack before starting action mode!");
        }
    }


    class BackClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    }

    private void configureNextListener() {
        MenuActionListener listener;
        if (!actionListenerStack.isEmpty()) {
            listener = actionListenerStack.peek();
            if (listener != null) {
                listener.configureCustomActionMenu(config);
                // assume that not all listeners
                // can command end action mode so
                // if there is no menu id configured,
                // then end the action mode.
                if (config.menuId == 0) {
                    endActionMode();
                } else {
                    refreshActionMode();
                }
            }
        }
    }

    public class MenuConfig {
        public static final int ITEM_ID = 0;
        public static final int ICON_ID = 1;
        public static final int FLAGS = 2;
        public boolean createGenericMenu = true;
        public int menuId;
        public int[][] menuResources;
        public int additionalMenuId = 0;
        public int[][] additionalResources;
        public String title;
        public String subtitle;
        public int titleRes = 0;
        public int subtitleRes = 0;
    }

    public void createGenericMenu() {
        config.menuId = R.menu.menu_generic_actions;
        config.menuResources = new int[8][3];

        config.menuResources[0][ITEM_ID] = R.id.action_add;
        config.menuResources[0][ICON_ID] = R.drawable.icon_path_add;
        config.menuResources[0][FLAGS] = SHOW_AS_ACTION_NEVER;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_add: " + R.drawable.icon_path_add);

        config.menuResources[1][ITEM_ID] = R.id.action_details;
        config.menuResources[1][ICON_ID] = R.drawable.icon_path_details;
        config.menuResources[1][FLAGS] = SHOW_AS_ACTION_NEVER;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_details: " + R.drawable.icon_path_details);

        config.menuResources[2][ITEM_ID] = R.id.action_edit;
        config.menuResources[2][ICON_ID] = R.drawable.icon_path_edit;
        config.menuResources[2][FLAGS] = SHOW_AS_ACTION_NEVER;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_edit: " + R.drawable.icon_path_edit);

        config.menuResources[3][ITEM_ID] = R.id.action_remove;
        config.menuResources[3][ICON_ID] = R.drawable.icon_path_remove;
        config.menuResources[3][FLAGS] = SHOW_AS_ACTION_NEVER;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_remove: " + R.drawable.icon_path_remove);

        config.menuResources[4][ITEM_ID] = R.id.action_favorite;
        config.menuResources[4][ICON_ID] = R.drawable.icon_path_star_empty;
        config.menuResources[4][FLAGS] = SHOW_AS_ACTION_NEVER;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_star_empty: " + R.drawable.icon_path_star_empty);

        config.menuResources[5][ITEM_ID] = R.id.action_search;
        config.menuResources[5][ICON_ID] = R.drawable.icon_path_search;
        config.menuResources[5][FLAGS] = SHOW_AS_ACTION_NEVER;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_search: " + R.drawable.icon_path_search);

        config.menuResources[6][ITEM_ID] = R.id.action_filter;
        config.menuResources[6][ICON_ID] = R.drawable.icon_path_filter;
        config.menuResources[6][FLAGS] = SHOW_AS_ACTION_NEVER;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_filter: " + R.drawable.icon_path_filter);

        config.menuResources[7][ITEM_ID] = R.id.action_sort;
        config.menuResources[7][ICON_ID] = R.drawable.icon_path_sort;
        config.menuResources[7][FLAGS] = SHOW_AS_ACTION_NEVER;
//        Log.i(TAG, "configureCustomActionMenu: R.drawable.icon_path_filter: " + R.drawable.icon_path_filter);
    }

    public interface MenuActionListener {

        boolean onToolbarBackPressed();

        void configureCustomActionMenu(MenuConfig config);

        GenericMenuActions getMenuActionsProvider();
    }


    private void dumpStack() {
        Log.e(TAG, "removeActionListener: --- dumppo ---");
        for (int i = 0; i < actionListenerStack.size(); i++) {
            MenuActionListener l = actionListenerStack.get(i);
            Log.e(TAG, "removeActionListener: --- " + l);
        }
        Log.e(TAG, "removeActionListener: --- dumppo ---");
    }


}
