package org.rares.miner49er.cache.optimizer;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;

public class CacheFeederService extends IntentService /*implements EntityOptimizer.DbUpdateFinishedListener*/ {

    private CacheFeedWorker worker = null;

    public CacheFeederService() {
        super(CacheFeederService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        worker = new CacheFeedWorker.Builder()
                .userDao(InMemoryCacheAdapterFactory.ofType(UserData.class))
                .projectsDao(InMemoryCacheAdapterFactory.ofType(ProjectData.class))
                .issuesDao(InMemoryCacheAdapterFactory.ofType(IssueData.class))
                .timeEntriesDao(InMemoryCacheAdapterFactory.ofType(TimeEntryData.class))
                .cache(ViewModelCacheSingleton.getInstance())
                .build();
    }

    @Override
    public void onDestroy() {
//        worker.close();
//        not disposing, as we want to have the work done
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        worker.enqueueCacheFill();
    }

}
