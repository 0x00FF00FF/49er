package org.rares.miner49er.cache.cacheadapter;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.persistence.dao.EventBroadcaster;

public abstract class AbstractAsyncCacheAdapter implements EventBroadcaster {

    private static final String TAG = AbstractAsyncCacheAdapter.class.getSimpleName();
    protected ViewModelCache cache = ViewModelCache.getInstance();

    private CompositeDisposable disposables = null;

//    @Override
//    public void registerEventListener(Consumer<Byte> listener) {
//        cache.registerEventListener(listener);
//    }


    @Override
    public Flowable<Byte> getBroadcaster() {
        return cache.getBroadcaster();
    }

    @Override
    public void sendEvent(Byte event) {
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
