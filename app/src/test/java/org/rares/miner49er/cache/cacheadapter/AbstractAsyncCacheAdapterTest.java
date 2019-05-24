package org.rares.miner49er.cache.cacheadapter;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class AbstractAsyncCacheAdapterTest {

    private class TestAsyncCacheAdapter extends AbstractAsyncCacheAdapter {

    }

    @Test
    public void testDisposables() {
        TestAsyncCacheAdapter taca = new TestAsyncCacheAdapter();
        assertNotNull(taca.getDisposables());
        assertFalse(taca.getDisposables().isDisposed());
        taca.shutdown();
        assertNotNull(taca.getDisposables());
        assertNotNull(taca.getBroadcaster());
    }
}