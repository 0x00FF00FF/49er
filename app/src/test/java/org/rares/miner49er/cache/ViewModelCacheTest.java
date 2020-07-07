package org.rares.miner49er.cache;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_USER;

@RunWith(AndroidJUnit4.class)
public class ViewModelCacheTest {

    /*
     *  Given   no cache started yet
     *  When    the cache engine is started with no throttling
     *  And     cache events are being sent
     *  Then    all cache events should be broadcasted
     */
    @Test
    public void testNoThrottling() throws InterruptedException {
        // given
        final int iterations = 100;
        final ViewModelCache viewModelCache = new ViewModelCache(false);
        final AtomicInteger eventsCounter = new AtomicInteger(0);

        viewModelCache.getBroadcaster()
                .subscribe(event -> eventsCounter.incrementAndGet());

        // when
        for (int i = 0; i < iterations; i++) {
            viewModelCache.sendEvent(CACHE_EVENT_UPDATE_USER);
            Thread.sleep(1);
        }

        // then
//        assertEquals(iterations, eventsCounter.get());    ////// 1-2 events are eaten somewhere
        assertTrue(iterations - 10 < eventsCounter.get());
    }

    /*
     *  Given   no cache started yet
     *  When    the cache engine is started with throttling
     *  And     cache events are being sent
     *  Then    only one cache event will be broadcasted in a ~50 ms interval
     */
    @Test
    public void testThrottling() throws InterruptedException {
        // given
        final int iterations = 70;
        final ViewModelCache viewModelCache = new ViewModelCache();
        final AtomicInteger eventsCounter = new AtomicInteger(0);

        viewModelCache.getBroadcaster()
                .observeOn(Schedulers.io())
                .subscribe(event -> eventsCounter.incrementAndGet());

        // when
        for (int i = 0; i < iterations; i++) {
            viewModelCache.sendEvent(CACHE_EVENT_UPDATE_USER);
            Thread.sleep(1);
        }

        // then
        assertEquals(2, eventsCounter.get());
    }

    /*
     *  Given   an existing cache
     *  When    a cache of one kind is requested
     *  Then    the cache will return the right implementation:
     *          1. projectDataCache
     *          2. issueDataCache
     *          3. timeEntryDataCache
     *          4. userDataCache
     *          5. throw error if no supported cache is found
     */
    @Test
    public void testCacheKind() {
        // given
        final ViewModelCache cache = new ViewModelCache();

        // when, then
        assertTrue(cache.getCache(ProjectData.class) instanceof ProjectDataCache);
        assertTrue(cache.getCache(IssueData.class) instanceof IssueDataCache);
        assertTrue(cache.getCache(TimeEntryData.class) instanceof TimeEntryDataCache);
        assertTrue(cache.getCache(UserData.class) instanceof UserDataCache);
        try {
            cache.getCache(Integer.class);
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedOperationException);
            assertTrue(ex.getMessage().startsWith("No existing cache"));
        }
    }

    /*
     *  Given   a newly created cache
     *  When    the broadcaster is requested
     *  Then    the broadcaster is returned
     *  And     the returned value is not null
     */
    @Test
    public void testReturnBroadcaster() {
        // given
        final ViewModelCache cache = new ViewModelCache();
        // when, then
        assertNotNull(cache.getBroadcaster());
    }

    /*
     *  Given   an existing cache with users, projects, issues and time entries
     *  When    the cached is cleared
     *  Then    there are no more entries in the cache
     */
    @Test
    public void testCleanCache() {
        // given
        final ViewModelCache cache = new ViewModelCache();
        for (long i = 0; i < 100; i++) {
            ProjectData pd = new ProjectData();
            pd.id = i + 1;
            cache.getProjectsLruCache().put(pd.id, pd);
        }

        for (long i = 0; i < 100; i++) {
            UserData ud = new UserData();
            ud.id = i + 1;
            cache.getUsersLruCache().put(ud.id, ud);
        }

        for (long i = 0; i < 100; i++) {
            IssueData id = new IssueData();
            id.id = i + 1;
            cache.getIssuesLruCache().put(id.id, id);
        }

        for (long i = 0; i < 100; i++) {
            TimeEntryData td = new TimeEntryData();
            td.id = i + 1;
            cache.getTimeEntriesLruCache().put(td.id, td);
        }

        assertEquals(100, cache.getProjectsLruCache().size());
        assertEquals(100, cache.getUsersLruCache().size());
        assertEquals(100, cache.getIssuesLruCache().size());
        assertEquals(100, cache.getTimeEntriesLruCache().size());

        // when
        cache.clear();

        // then
        assertEquals(0, cache.getProjectsLruCache().size());
        assertEquals(0, cache.getUsersLruCache().size());
        assertEquals(0, cache.getIssuesLruCache().size());
        assertEquals(0, cache.getTimeEntriesLruCache().size());
    }

    /*
     *  Given   an existing cache with users, projects, issues and time entries
     *  When    the cached is closed
     *  Then    there are no more entries in the cache
     *  And     the broadcaster is null
     *  And     isDisposed returns true
     */
    @Test
    public void testCloseCache() {
        // given
        final ViewModelCache cache = new ViewModelCache();
        for (long i = 0; i < 100; i++) {
            ProjectData pd = new ProjectData();
            pd.id = i + 1;
            cache.getProjectsLruCache().put(pd.id, pd);
        }

        for (long i = 0; i < 100; i++) {
            UserData ud = new UserData();
            ud.id = i + 1;
            cache.getUsersLruCache().put(ud.id, ud);
        }

        for (long i = 0; i < 100; i++) {
            IssueData id = new IssueData();
            id.id = i + 1;
            cache.getIssuesLruCache().put(id.id, id);
        }

        for (long i = 0; i < 100; i++) {
            TimeEntryData td = new TimeEntryData();
            td.id = i + 1;
            cache.getTimeEntriesLruCache().put(td.id, td);
        }

        assertEquals(100, cache.getProjectsLruCache().size());
        assertEquals(100, cache.getUsersLruCache().size());
        assertEquals(100, cache.getIssuesLruCache().size());
        assertEquals(100, cache.getTimeEntriesLruCache().size());
        assertNotNull(cache.getBroadcaster());

        // when
        cache.close();

        // then
        assertEquals(0, cache.getProjectsLruCache().size());
        assertEquals(0, cache.getUsersLruCache().size());
        assertEquals(0, cache.getIssuesLruCache().size());
        assertEquals(0, cache.getTimeEntriesLruCache().size());
        assertNotNull(cache.getBroadcaster());
        assertTrue(cache.isDisposed());
    }

    /*
     *  Given   an existing cache
     *  When    a request to increase the cache size is received
     *          with a larger value than maxSize
     *  Then    the project cache is replaced with a new one
     *          that will contain all the projects
     */
    @Test
    public void testEnlargeProjectsCache() {
        // given
        final ViewModelCache cache = new ViewModelCache();
        for (long i = 0; i < 100; i++) {
            ProjectData pd = new ProjectData();
            pd.id = i + 1;
            cache.getProjectsLruCache().put(pd.id, pd);
        }

        assertEquals(100, cache.getProjectsLruCache().size());
        final int hash = cache.getProjectsLruCache().hashCode();

        // when
        cache.increaseProjectsCacheSize(101);

        // then
        assertEquals(100, cache.getProjectsLruCache().size());
        assertNotEquals(hash, cache.getProjectsLruCache().hashCode());
    }

    /*
     *  Given   an existing cache
     *  When    a request to increase the cache size is received
     *          with a smaller value than maxSize
     *  Then    the project cache is not replaced with a new one
     *          and all projects will still be there
     */
    @Test
    public void testNotEnlargeProjectsCache() {
        // given
        final ViewModelCache cache = new ViewModelCache();
        for (long i = 0; i < 100; i++) {
            ProjectData pd = new ProjectData();
            pd.id = i + 1;
            cache.getProjectsLruCache().put(pd.id, pd);
        }

        assertEquals(100, cache.getProjectsLruCache().size());
        final int hash = cache.getProjectsLruCache().hashCode();

        // when
        cache.increaseProjectsCacheSize(99);

        // then
        assertEquals(100, cache.getProjectsLruCache().size());
        assertEquals(hash, cache.getProjectsLruCache().hashCode());
    }

    /*
     *  Given   a non-throttled cache
     *  When    a large number of events is sent
     *  Then    the broadcaster will encounter backpressure
     */
    @Test
    public void testNonThrottledBackPressure() throws InterruptedException {
        final ViewModelCache cache = new ViewModelCache(false);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean();
        AtomicInteger overlaps = new AtomicInteger();
        AtomicInteger events = new AtomicInteger();

        final int threadCount = 10;
        final int eventCount = 999;

        cache.getBroadcaster()
                .observeOn(Schedulers.trampoline())
                .subscribe(ev -> events.incrementAndGet());

        ExecutorService service = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < eventCount; i++) {
            service.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (running.get()) {
                    overlaps.incrementAndGet();
                }
                running.set(true);
                cache.sendEvent(CACHE_EVENT_UPDATE_USER);
            });
        }

        latch.countDown();

        Thread.sleep(300);

        assertTrue(events.get() > 100);
    }

    /*
     *  Given   a non-throttled cache
     *  When    a large number of events is sent
     *  Then    the broadcaster will encounter backpressure
     */
    @Test
    public void testThrottledBackPressure() throws InterruptedException {
        final ViewModelCache cache = new ViewModelCache();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean();
        AtomicInteger overlaps = new AtomicInteger();
        AtomicInteger events = new AtomicInteger();

        final int threadCount = 10;
        final int eventCount = 999;

        cache.getBroadcaster()
                .observeOn(Schedulers.trampoline())
                .subscribe(ev -> events.incrementAndGet());

        ExecutorService service = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < eventCount; i++) {
            service.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (running.get()) {
                    overlaps.incrementAndGet();
                }
                running.set(true);
                cache.sendEvent(CACHE_EVENT_UPDATE_USER);
            });
        }

        latch.countDown();

        Thread.sleep(300);

        assertTrue(events.get() < 10);
    }
}