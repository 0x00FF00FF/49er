package org.rares.miner49er.domain.issues.ui.actions.remove;

import com.pushtorefresh.storio3.Optional;
import lombok.Setter;
import org.rares.miner49er._abstract.ResizeableItemsUiOps;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.fragments.YesNoDialogFragment;

import java.lang.ref.WeakReference;

public class IssueRemoveAction implements YesNoDialogFragment.Listener {
    private WeakReference<ResizeableItemsUiOps> iops;
    private AsyncGenericDao<IssueData> cache = InMemoryCacheAdapterFactory.ofType(IssueData.class);
    @Setter
    private long issueId;

    public IssueRemoveAction(ResizeableItemsUiOps ops) {
        iops = new WeakReference<>(ops);
    }

    public void performAction() {
        if (iops != null && iops.get() != null) {
            iops.get().expandList();
        }
        Optional<IssueData> issueDataOptional = cache.get(issueId, true).blockingGet();
        if (issueDataOptional.isPresent()) {
            cache.delete(issueDataOptional.get());
        }
    }

    @Override
    public void onYes() {
        performAction();
    }

    @Override
    public void onNo() {
        iops.clear();
        iops = null;
        issueId = 0;
    }
}
