package org.rares.miner49er.ui.actionmode;

import android.support.v7.widget.Toolbar;
import android.view.View;
import lombok.Setter;
import org.rares.miner49er.R;

public class ProjectsActionModeCallback {

    public static final String TAG = ProjectsActionModeCallback.class.getSimpleName();

    private Toolbar toolbar;
    private String title;
    private String subtitle;
    private int projectId;

    @Setter
    private ActionListener actionListener;


    private final BackClickListener bcl = new BackClickListener();

    public ProjectsActionModeCallback(Toolbar toolbar) {
        this.toolbar = toolbar;
    }

    public void startActionMode() {
        toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.Custom_TextAppearance_Widget_AppCompat_Toolbar_Title_Action);
        toolbar.setSubtitleTextAppearance(toolbar.getContext(), R.style.Custom_TextAppearance_Widget_AppCompat_Toolbar_Subtitle);
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
        toolbar.setNavigationIcon(R.drawable.icon_path_left_arrow);
        toolbar.setNavigationContentDescription(R.string._toolbar_back_button_description);
        toolbar.setNavigationOnClickListener(bcl);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_generic_actions);

//        toolbar.setContentInsetStartWithNavigation((int) UiUtil.pxFromDp(toolbar.getContext(), R.dimen.second_keyline));
        // + inAnimation
    }

    public void endActionMode() {
        // + outAnimation
        toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.Custom_TextAppearance_Widget_AppCompat_Toolbar_Title);
        toolbar.setNavigationIcon(null);
        toolbar.setSubtitle("");
        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationOnClickListener(null);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_home_scrolling);
    }

    public ProjectsActionModeCallback setTitle(String title) {
        toolbar.setTitle(title);
        this.title = title;
        return this;
    }

    public ProjectsActionModeCallback setSubtitle(String subtitle) {
        toolbar.setSubtitle(subtitle);
        this.subtitle = subtitle;
        return this;
    }

    public ProjectsActionModeCallback setProjectId(int projectId) {
        this.projectId = projectId;
        return this;
    }

    class BackClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (actionListener != null) {
                actionListener.onEndActionMode();
            }
        }
    }

    public interface ActionListener {
        void onEndActionMode();
    }
}
