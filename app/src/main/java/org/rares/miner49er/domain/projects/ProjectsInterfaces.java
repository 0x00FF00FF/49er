package org.rares.miner49er.domain.projects;

/**
 * @author rares
 * @since 29.09.2017.
 */

public interface ProjectsInterfaces {

    int SORT_TYPE_SIMPLE = 0;
    int SORT_TYPE_ALPHA_NUM = 1;
    /**
     * Not implemented yet.
     */
    int SORT_TYPE_RECENT = 2;
    /**
     * Not implemented yet.
     */
    int SORT_TYPE_FAVORITES = 3;

    String KEY_NAME = "name";
    String KEY_COLOR = "color";
    String KEY_ICON = "icon";
    String KEY_PICTURE = "picture";

    interface ProjectsResizeListener {
        void onProjectsListShrink();
    }

}

