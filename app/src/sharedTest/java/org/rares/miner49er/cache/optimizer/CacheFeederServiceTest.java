package org.rares.miner49er.cache.optimizer;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.pushtorefresh.storio3.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.domain.users.model.UserData;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CacheFeederServiceTest {

    private ViewModelCache vmCache = ViewModelCache.getInstance();
    private CacheFeedWorker cacheWorker = null;

    @Before
    public void setUp() throws Exception {
        assertNull(vmCache.loggedInUser);
        cacheWorker = new CacheFeedWorker();
    }

    @Test
    public void enqueueCacheFillTest() throws InterruptedException {
        cacheWorker.enqueueCacheFill();
        Thread.sleep(2000);
        assertTrue(vmCache.getCache(UserData.class).getData(Optional.of(null)).size() > 0);
    }
}