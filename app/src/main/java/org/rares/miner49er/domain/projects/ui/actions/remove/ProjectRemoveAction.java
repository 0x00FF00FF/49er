package org.rares.miner49er.domain.projects.ui.actions.remove;

import com.pushtorefresh.storio3.Optional;
import lombok.Setter;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.fragments.YesNoDialogFragment;

import java.lang.ref.WeakReference;

public class ProjectRemoveAction implements YesNoDialogFragment.Listener {
    private WeakReference<ResizeableItemsUiOps> ops;
    private AsyncGenericDao<ProjectData> cache = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
    @Setter
    private long projectId;

    public ProjectRemoveAction(ResizeableItemsUiOps ops) {
        this.ops = new WeakReference<>(ops);
    }

    public void performAction() {
        if (ops != null && ops.get() != null) {
            ops.get().expandList();
        }
        Optional<ProjectData> projectDataOptional = cache.get(projectId, true).blockingGet();
        if (projectDataOptional.isPresent()) {
            cache.delete(projectDataOptional.get());
        }
    }

    @Override
    public void onYes() {
        performAction();
    }

    @Override
    public void onNo() {
        ops.clear();
        ops = null;
        projectId = 0;
    }
}
