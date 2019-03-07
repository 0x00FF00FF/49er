package org.rares.miner49er.ui.custom.glide;

import androidx.annotation.NonNull;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestManager;
import org.rares.miner49er.domain.projects.model.ProjectData;

public class ProjectListPreloader extends ListPreloader<ProjectData> {
    /**
     * Constructor for {@link ListPreloader} that accepts interfaces for providing
     * the dimensions of images to preload, the list of models to preload for a given position, and
     * the request to use to load images.
     *
     * @param requestManager
     * @param preloadModelProvider     Provides models to load and requests capable of loading them.
     * @param preloadDimensionProvider Provides the dimensions of images to load.
     * @param maxPreload               Maximum number of items to preload.
     */
    public ProjectListPreloader(
            @NonNull RequestManager requestManager,
            @NonNull PreloadModelProvider<ProjectData> preloadModelProvider,
            @NonNull PreloadSizeProvider<ProjectData> preloadDimensionProvider,
            int maxPreload) {
        super(requestManager, preloadModelProvider, preloadDimensionProvider, maxPreload);
    }
}
