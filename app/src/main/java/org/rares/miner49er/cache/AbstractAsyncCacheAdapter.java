package org.rares.miner49er.cache;

import android.util.Log;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import org.rares.miner49er.persistence.dao.EventBroadcaster;

public abstract class AbstractAsyncCacheAdapter implements EventBroadcaster {

    private static final String TAG = AbstractAsyncCacheAdapter.class.getSimpleName();
    protected SimpleCache cache = SimpleCache.getInstance();

    private CompositeDisposable disposables = null;

    @Override
    public void registerEventListener(Consumer<Object> listener) {
        cache.registerEventListener(listener);
    }

    @Override
    public void sendEvent() {
        // only proxy for event listener register - no events
    }

    public void shutdown() {
        dispose(disposables);
//        cache.close();      ////
    }

    protected void dispose(Disposable disposable) {
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                Log.v(TAG, "[dispose]");
                disposable.dispose();
            }
        }
    }

    protected CompositeDisposable getDisposables() {
        if (disposables == null || disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }
        return disposables;
    }
}
