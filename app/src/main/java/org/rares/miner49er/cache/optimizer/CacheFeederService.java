package org.rares.miner49er.cache.optimizer;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;

public class CacheFeederService extends IntentService /*implements EntityOptimizer.DbUpdateFinishedListener*/ {

    private CacheFeedWorker worker = null;

    public CacheFeederService() {
        super(CacheFeederService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        worker = new CacheFeedWorker();
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
