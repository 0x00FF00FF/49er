package org.rares.miner49er.cache;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.observers.BaseTestConsumer.TestWaitStrategy;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_REMOVE_USER;
import static org.rares.miner49er.cache.Cache.CACHE_EVENT_UPDATE_USER;

@RunWith(AndroidJUnit4.class)
public class UserDataCacheTest {
    private UserDataCache uc;
    private Cache<ProjectData> pc;
    private Cache<IssueData> ic;
    private Cache<TimeEntryData> tc;
    private final long napTime = 60;
    private ViewModelCache cache = new ViewModelCache();

//    @Rule
//    public final ImmediateSchedulersRule schedulers = new ImmediateSchedulersRule();

    @Before
    public void setUp() {
        if (cache != null) {
            cache.close();
        }
        cache = new ViewModelCache(false);
        uc = (UserDataCache) cache.getCache(UserData.class);
        pc = cache.getCache(ProjectData.class);
        ic = cache.getCache(IssueData.class);
        tc = cache.getCache(TimeEntryData.class);

//        RxJavaPlugins.setFailOnNonBlockingScheduler(true);
    }

    @After
    public void tearDown() throws IOException {
        uc.close();
    }

    /*
     *  Given   empty cache,
     *  When    putData(list, predicate, boolean) is called,
     *  Then    nothing is added to the cache
     */
    @Test
    public void predicatePut() {
        UserData userData = new UserData();
        userData.id = 1L;
        userData.setName("some name");
        List<UserData> users = new ArrayList<>();

        assertEquals(0, uc.getSize());

        uc.putData(users, p -> true, false);

        assertEquals(0, uc.getSize());
    }

    /*
     *  Given   an empty cache,
     *  When    a list of users is added to the cache using putData(list, boolean)
     *  Then    the users are available in the cache
     */
    @Test
    public void putList() {
        UserData userData = new UserData();
        userData.id = 1L;
        userData.setName("some name");
        List<UserData> users = new ArrayList<>();
        users.add(userData);

        assertEquals(0, uc.getSize());


        uc.putData(users, false);

        assertEquals(1, uc.getSize());
        assertNotNull(uc.getData(1L));
        assertEquals(userData.getName(), uc.getData(1L).getName());
        assertTrue(uc.getData(Optional.empty()).contains(userData));
    }

    /*
     *  Given   an empty cache,
     *  When    a user is added to the cache using putData(userData, boolean),
     *  Then    the user is available in the cache
     */
    @Test
    public void insertUser() {
        UserData userData = new UserData();
        userData.id = 1L;
        userData.setName("some name");

        assertEquals(0, uc.getSize());

        uc.putData(userData, false);

        assertEquals(1, uc.getSize());
        assertNotNull(uc.getData(1L));
        assertEquals(userData.getName(), uc.getData(1L).getName());
        assertTrue(uc.getData(Optional.empty()).contains(userData));
    }


    /*
     *  Given   a cached user,
     *  When    the user is updated using putData(userData, boolean),
     *  Then    the updated information is available in the cache
     */
    @Test
    public void updateUser() {
        String name = "some name";
        String updatedName = "updated name";

        UserData userData = new UserData();
        userData.id = 1L;
        userData.setName(name);

        UserData userData1 = new UserData();
        userData1.id = 1L;
        userData1.setName(updatedName);

        assertEquals(0, uc.getSize());

        uc.putData(userData, false);

        assertEquals(1, uc.getSize());
        assertNotNull(uc.getData(1L));
        assertEquals(name, uc.getData(1L).getName());
        assertTrue(uc.getData(Optional.empty()).contains(userData));

        uc.putData(userData1, false);

        assertEquals(1, uc.getSize());
        assertNotNull(uc.getData(1L));
        assertEquals(updatedName, uc.getData(1L).getName());
        assertTrue(uc.getData(Optional.empty()).contains(userData1));
    }

    /*
     *  Given   two cached users,
     *  When    getData(Optional.empty()) is called,
     *  Then    both of the users will be contained in the returned list1
     */
    @Test
    public void getAllUsers() {
        String userName = "user";
        List<UserData> users = new ArrayList<>();

        UserData user1 = new UserData();
        user1.id = 1L;
        user1.setName(userName + 1);

        UserData user2 = new UserData();
        user2.id = 2L;
        user2.setName(userName + 1);

        users.add(user1);
        users.add(user2);

        assertEquals(0, uc.getSize());

        uc.putData(users, false);

        assertEquals(2, uc.getSize());
        assertNotNull(uc.getData(1L));
        assertNotNull(uc.getData(2L));

        assertTrue(uc.getData(Optional.empty()).contains(user1));
        assertTrue(uc.getData(Optional.empty()).contains(user2));
    }


    /*
     *  Given   a cached project with no users (null for team),
     *  When    getData(Optional.of(project.id)) is called,
     *  Then    null will be returned
     */
    @Test
    public void getProjectUsers_null() {
        ProjectData projectData = new ProjectData();
        projectData.id = 1L;
        projectData.setTeam(null);

        assertEquals(0, uc.getSize());
        assertEquals(0, pc.getSize());

        pc.putData(projectData, false);

        assertNull(uc.getData(Optional.of(projectData.id)));
    }

    /*
     *  Given   a cached project with two (cached) users, 3 cached users in total
     *  When    getData(Optional.of(project.id) is called,
     *  Then    both of the users will be contained in the returned list
     */
    @Test
    public void getAllProjectUsers() {
        ProjectData projectData = new ProjectData();
        projectData.id = 1L;

        String userName = "user";
        List<UserData> users = new ArrayList<>();

        UserData user1 = new UserData();
        user1.id = 1L;
        user1.setName(userName + 1);

        UserData user2 = new UserData();
        user2.id = 2L;
        user2.setName(userName + 2);

        UserData user3 = new UserData();
        user3.id = 3L;
        user3.setName(userName + 3);


        users.add(user1);
        users.add(user2);

        projectData.setTeam(users);

        assertEquals(0, uc.getSize());
        assertEquals(0, pc.getSize());

        pc.putData(projectData, false);

        uc.putData(users, false);
        uc.putData(user3, false);

        assertEquals(3, uc.getSize());
        assertNotNull(uc.getData(1L));
        assertNotNull(uc.getData(2L));
        assertNotNull(uc.getData(3L));

        assertTrue(uc.getData(Optional.empty()).contains(user1));
        assertTrue(uc.getData(Optional.empty()).contains(user2));
        assertTrue(uc.getData(Optional.empty()).contains(user3));

        assertEquals(2, uc.getData(Optional.of(projectData.id)).size());

        assertTrue(uc.getData(Optional.of(projectData.id)).contains(user1));
        assertTrue(uc.getData(Optional.of(projectData.id)).contains(user2));
        assertFalse(uc.getData(Optional.of(projectData.id)).contains(user3));
    }

    /*
     *  Given   a cached user, member of multiple [cached] projects
     *              that is no owner of projects, issues, or time entries
     *  When    removeUser(userData) is called,
     *  Then    the user is removed
     */
    @Test
    public void testRemoveProjectMember_noLinkedEntities() throws InterruptedException {
        // given

        List<ProjectData> projects = new ArrayList<>();
        List<UserData> users = new ArrayList<>();
        List<IssueData> issues = new ArrayList<>();
        List<TimeEntryData> timeEntries = new ArrayList<>();

        ProjectData projectData1 = new ProjectData();
        ProjectData projectData2 = new ProjectData();
        ProjectData projectData3 = new ProjectData();

        projectData1.id = 1L;
        projectData2.id = 2L;
        projectData3.id = 3L;

        UserData userData1 = new UserData();
        UserData userData2 = new UserData();
        UserData userData3 = new UserData();
        UserData userData4 = new UserData();

        userData1.id = 1L;
        userData2.id = 2L;
        userData3.id = 3L;
        userData4.id = 4L;

        IssueData issueData1 = new IssueData();
        IssueData issueData2 = new IssueData();
        IssueData issueData3 = new IssueData();

        issueData1.id = 1L;
        issueData2.id = 2L;
        issueData3.id = 3L;

        TimeEntryData timeEntryData1 = new TimeEntryData();
        TimeEntryData timeEntryData2 = new TimeEntryData();
        TimeEntryData timeEntryData3 = new TimeEntryData();

        timeEntryData1.id = 1L;
        timeEntryData2.id = 2L;
        timeEntryData3.id = 3L;

        users.add(userData1);
        users.add(userData4);
        projectData1.setTeam(users);

        users.clear();
        users.add(userData2);
        users.add(userData4);

        projectData2.setTeam(users);

        users.clear();
        users.add(userData3);
        users.add(userData4);

        projectData3.setTeam(users);

        projects.add(projectData1);
        projects.add(projectData2);
        projects.add(projectData3);

        issueData1.parentId = projectData1.id;
        issueData2.parentId = projectData2.id;
        issueData3.parentId = projectData3.id;

        timeEntryData1.parentId = issueData1.id;
        timeEntryData2.parentId = issueData2.id;
        timeEntryData3.parentId = issueData3.id;

        timeEntryData1.setUserId(userData1.id);
        timeEntryData2.setUserId(userData2.id);
        timeEntryData3.setUserId(userData3.id);

        users.clear();
        users.add(userData1);
        users.add(userData2);
        users.add(userData3);
        users.add(userData4);

        timeEntries.add(timeEntryData1);
        timeEntries.add(timeEntryData2);
        timeEntries.add(timeEntryData3);

        issues.add(issueData1);
        issues.add(issueData2);
        issues.add(issueData3);

        assertEquals(0, pc.getSize());
        assertEquals(0, uc.getSize());
        assertEquals(0, tc.getSize());
        assertEquals(0, ic.getSize());

        pc.putData(projects, false);
        uc.putData(users, false);
        ic.putData(issues, true);
        tc.putData(timeEntries, true);

        assertEquals(3, pc.getSize());
        assertEquals(4, uc.getSize());
        assertEquals(3, tc.getSize());
        assertEquals(3, ic.getSize());

        assertTrue(pc.getData(Optional.empty()).contains(projectData1));
        assertTrue(pc.getData(Optional.empty()).contains(projectData2));
        assertTrue(pc.getData(Optional.empty()).contains(projectData3));

        assertTrue(ic.getData(Optional.empty()).contains(issueData1));
        assertTrue(ic.getData(Optional.empty()).contains(issueData2));
        assertTrue(ic.getData(Optional.empty()).contains(issueData3));

        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData1));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData2));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData3));

        assertTrue(uc.getData(Optional.empty()).contains(userData1));
        assertTrue(uc.getData(Optional.empty()).contains(userData2));
        assertTrue(uc.getData(Optional.empty()).contains(userData3));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData1.id).getIssues().contains(issueData1));
        assertTrue(pc.getData(projectData2.id).getIssues().contains(issueData2));
        assertTrue(pc.getData(projectData3.id).getIssues().contains(issueData3));

        assertTrue(ic.getData(issueData1.id).getTimeEntries().contains(timeEntryData1));
        assertTrue(ic.getData(issueData2.id).getTimeEntries().contains(timeEntryData2));
        assertTrue(ic.getData(issueData3.id).getTimeEntries().contains(timeEntryData3));

        TestSubscriber<Byte> testSubscriber = cache.getBroadcaster()
                .filter(e -> e.equals(CACHE_EVENT_REMOVE_USER))
                .test();

        // when

        uc.removeData(userData4);

        testSubscriber.request(1);
        testSubscriber.awaitCount(1);

        // then

        testSubscriber.assertValue(CACHE_EVENT_REMOVE_USER);

        assertTrue(uc.getData(Optional.empty()).contains(userData1));
        assertTrue(uc.getData(Optional.empty()).contains(userData2));
        assertTrue(uc.getData(Optional.empty()).contains(userData3));
        assertFalse(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData1.id).getTeam().contains(userData1));
        assertTrue(pc.getData(projectData2.id).getTeam().contains(userData2));
        assertTrue(pc.getData(projectData3.id).getTeam().contains(userData3));

        assertFalse(pc.getData(projectData1.id).getTeam().contains(userData4));
        assertFalse(pc.getData(projectData2.id).getTeam().contains(userData4));
        assertFalse(pc.getData(projectData3.id).getTeam().contains(userData4));

        assertEquals(3, pc.getSize());
        assertEquals(3, uc.getSize());
        assertEquals(3, tc.getSize());
        assertEquals(3, ic.getSize());
    }


    /*
     *  Given   a cached user, member of multiple [cached] projects
     *              that is no owner of projects, issues and time entries
     *  When    removeUser(userData) is called from 3 different threads [in parallel],
     *  Then    the user is removed
     */
    @Test
    public void testRemoveProjectMember_noLinkedEntities_parallel() {
        // given

        List<ProjectData> projects = new ArrayList<>();
        List<UserData> users = new ArrayList<>();
        List<IssueData> issues = new ArrayList<>();
        List<TimeEntryData> timeEntries = new ArrayList<>();

        ProjectData projectData1 = new ProjectData();
        ProjectData projectData2 = new ProjectData();
        ProjectData projectData3 = new ProjectData();

        projectData1.id = 1L;
        projectData2.id = 2L;
        projectData3.id = 3L;

        UserData userData1 = new UserData();
        UserData userData2 = new UserData();
        UserData userData3 = new UserData();
        UserData userData4 = new UserData();

        userData1.id = 1L;
        userData2.id = 2L;
        userData3.id = 3L;
        userData4.id = 4L;

        IssueData issueData1 = new IssueData();
        IssueData issueData2 = new IssueData();
        IssueData issueData3 = new IssueData();

        issueData1.id = 1L;
        issueData2.id = 2L;
        issueData3.id = 3L;

        TimeEntryData timeEntryData1 = new TimeEntryData();
        TimeEntryData timeEntryData2 = new TimeEntryData();
        TimeEntryData timeEntryData3 = new TimeEntryData();

        timeEntryData1.id = 1L;
        timeEntryData2.id = 2L;
        timeEntryData3.id = 3L;

        users.add(userData1);
        users.add(userData4);
        projectData1.setTeam(users);

        users.clear();
        users.add(userData2);
        users.add(userData4);

        projectData2.setTeam(users);

        users.clear();
        users.add(userData3);
        users.add(userData4);

        projectData3.setTeam(users);

        projects.add(projectData1);
        projects.add(projectData2);
        projects.add(projectData3);

        issueData1.parentId = projectData1.id;
        issueData2.parentId = projectData2.id;
        issueData3.parentId = projectData3.id;

        timeEntryData1.parentId = issueData1.id;
        timeEntryData2.parentId = issueData2.id;
        timeEntryData3.parentId = issueData3.id;

        timeEntryData1.setUserId(userData1.id);
        timeEntryData2.setUserId(userData2.id);
        timeEntryData3.setUserId(userData3.id);

        users.clear();
        users.add(userData1);
        users.add(userData2);
        users.add(userData3);
        users.add(userData4);

        timeEntries.add(timeEntryData1);
        timeEntries.add(timeEntryData2);
        timeEntries.add(timeEntryData3);

        issues.add(issueData1);
        issues.add(issueData2);
        issues.add(issueData3);

        assertEquals(0, pc.getSize());
        assertEquals(0, uc.getSize());
        assertEquals(0, tc.getSize());
        assertEquals(0, ic.getSize());

        pc.putData(projects, false);
        uc.putData(users, false);
        ic.putData(issues, true);
        tc.putData(timeEntries, true);

        assertEquals(3, pc.getSize());
        assertEquals(4, uc.getSize());
        assertEquals(3, tc.getSize());
        assertEquals(3, ic.getSize());

        assertTrue(pc.getData(Optional.empty()).contains(projectData1));
        assertTrue(pc.getData(Optional.empty()).contains(projectData2));
        assertTrue(pc.getData(Optional.empty()).contains(projectData3));

        assertTrue(ic.getData(Optional.empty()).contains(issueData1));
        assertTrue(ic.getData(Optional.empty()).contains(issueData2));
        assertTrue(ic.getData(Optional.empty()).contains(issueData3));

        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData1));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData2));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData3));

        assertTrue(uc.getData(Optional.empty()).contains(userData1));
        assertTrue(uc.getData(Optional.empty()).contains(userData2));
        assertTrue(uc.getData(Optional.empty()).contains(userData3));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData1.id).getIssues().contains(issueData1));
        assertTrue(pc.getData(projectData2.id).getIssues().contains(issueData2));
        assertTrue(pc.getData(projectData3.id).getIssues().contains(issueData3));

        assertTrue(ic.getData(issueData1.id).getTimeEntries().contains(timeEntryData1));
        assertTrue(ic.getData(issueData2.id).getTimeEntries().contains(timeEntryData2));
        assertTrue(ic.getData(issueData3.id).getTimeEntries().contains(timeEntryData3));

//
//        // when
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean();
        AtomicInteger overlaps = new AtomicInteger();

        final int threadCount = 10;
        final int deleteCount = 9;

        ExecutorService service = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < deleteCount; i++) {
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
                uc.removeData(userData4);
            });
        }

        latch.countDown();

        TestSubscriber<Byte> testSubscriber = cache.getBroadcaster()
                .filter(e -> e.equals(CACHE_EVENT_REMOVE_USER))
                .test();

        assertThat(overlaps.get(), greaterThan(0));

        // when

        testSubscriber.request(1);
        testSubscriber.awaitCount(deleteCount, TestWaitStrategy.SLEEP_10MS, 200);

        // then

//        testSubscriber.assertValueCount(deleteCount);

        assertTrue(uc.getData(Optional.empty()).contains(userData1));
        assertTrue(uc.getData(Optional.empty()).contains(userData2));
        assertTrue(uc.getData(Optional.empty()).contains(userData3));
        assertFalse(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData1.id).getTeam().contains(userData1));
        assertTrue(pc.getData(projectData2.id).getTeam().contains(userData2));
        assertTrue(pc.getData(projectData3.id).getTeam().contains(userData3));

        assertFalse(pc.getData(projectData1.id).getTeam().contains(userData4));
        assertFalse(pc.getData(projectData2.id).getTeam().contains(userData4));
        assertFalse(pc.getData(projectData3.id).getTeam().contains(userData4));

        assertEquals(3, pc.getSize());
        assertEquals(3, uc.getSize());
        assertEquals(3, tc.getSize());
        assertEquals(3, ic.getSize());

    }

    /*
     *  Given   a cached user, member of multiple [cached] projects
     *              that is owner of:
     *              1. project [x], issue [0], time entry [0],
     *              2. project [0], issue [0], time entry [0],
     *  When    removeUser(userData) is called,
     *  Then    the user is not removed
     */
    @Test
    public void testRemoveProjectMember_linkedEntities_projectOwner() {
        List<ProjectData> projects = new ArrayList<>();
        List<UserData> users = new ArrayList<>();
        List<IssueData> issues = new ArrayList<>();
        List<TimeEntryData> timeEntries = new ArrayList<>();

        ProjectData projectData1 = new ProjectData();
        ProjectData projectData4 = new ProjectData();

        projectData1.id = 1L;
        projectData4.id = 4L;

        UserData userData1 = new UserData();
        UserData userData4 = new UserData();

        userData1.id = 1L;
        userData4.id = 4L;

        projectData1.setOwner(userData4);
        projectData4.setOwner(userData1);

        IssueData issueData1 = new IssueData();
        IssueData issueData4 = new IssueData();

        issueData1.id = 1L;
        issueData4.id = 4L;

        TimeEntryData timeEntryData1 = new TimeEntryData();
        TimeEntryData timeEntryData4 = new TimeEntryData();

        timeEntryData1.id = 1L;
        timeEntryData4.id = 4L;

        users.add(userData1);
        users.add(userData4);

        projectData1.setTeam(users);
        projectData4.setTeam(users);

        projects.add(projectData1);
        projects.add(projectData4);

        issueData1.parentId = projectData1.id;
        issueData4.parentId = projectData4.id;

        issueData1.setOwnerId(userData1.id);
        issueData4.setOwnerId(userData1.id);

        timeEntryData1.parentId = issueData1.id;
        timeEntryData4.parentId = issueData4.id;

        timeEntryData1.setUserId(userData1.id);
        timeEntryData4.setUserId(userData1.id);

        timeEntries.add(timeEntryData1);
        timeEntries.add(timeEntryData4);

        issues.add(issueData1);
        issues.add(issueData4);

        assertEquals(0, pc.getSize());
        assertEquals(0, uc.getSize());
        assertEquals(0, tc.getSize());
        assertEquals(0, ic.getSize());

        pc.putData(projects, false);
        uc.putData(users, false);
        ic.putData(issues, true);
        tc.putData(timeEntries, true);

        assertEquals(2, pc.getSize());
        assertEquals(2, uc.getSize());
        assertEquals(2, tc.getSize());
        assertEquals(2, ic.getSize());

        assertTrue(pc.getData(Optional.empty()).contains(projectData1));
        assertTrue(pc.getData(Optional.empty()).contains(projectData4));

        assertTrue(ic.getData(Optional.empty()).contains(issueData1));
        assertTrue(ic.getData(Optional.empty()).contains(issueData4));

        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData1));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData4));

        assertTrue(uc.getData(Optional.empty()).contains(userData1));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData1.id).getIssues().contains(issueData1));
        assertTrue(pc.getData(projectData4.id).getIssues().contains(issueData4));

        assertTrue(ic.getData(issueData1.id).getTimeEntries().contains(timeEntryData1));
        assertTrue(ic.getData(issueData4.id).getTimeEntries().contains(timeEntryData4));

        uc.removeData(userData4);

        assertTrue(uc.getData(Optional.empty()).contains(userData1));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData1.id).getTeam().contains(userData1));
        assertTrue(pc.getData(projectData4.id).getTeam().contains(userData1));

        assertTrue(pc.getData(projectData1.id).getTeam().contains(userData4));
        assertTrue(pc.getData(projectData4.id).getTeam().contains(userData4));

        assertEquals(2, pc.getSize());
        assertEquals(2, uc.getSize());
        assertEquals(2, tc.getSize());
        assertEquals(2, ic.getSize());
    }

    /*
     *  Given   a cached user, member of multiple [cached] projects
     *              that is owner of:
     *              1. project [0], issue [x], time entry [0],
     *              2. project [0], issue [0], time entry [0],
     *  When    removeUser(userData) is called,
     *  Then    the user is not removed
     */
    @Test
    public void testRemoveProjectMember_linkedEntities_issueOwner() {
        List<ProjectData> projects = new ArrayList<>();
        List<UserData> users = new ArrayList<>();
        List<IssueData> issues = new ArrayList<>();
        List<TimeEntryData> timeEntries = new ArrayList<>();

        ProjectData projectData2 = new ProjectData();
        ProjectData projectData4 = new ProjectData();

        projectData2.id = 2L;
        projectData4.id = 4L;

        UserData userData2 = new UserData();
        UserData userData4 = new UserData();

        userData2.id = 2L;
        userData4.id = 4L;

        IssueData issueData2 = new IssueData();
        IssueData issueData4 = new IssueData();

        issueData2.id = 2L;
        issueData4.id = 4L;

        TimeEntryData timeEntryData2 = new TimeEntryData();
        TimeEntryData timeEntryData4 = new TimeEntryData();

        timeEntryData2.id = 2L;
        timeEntryData4.id = 4L;

        users.add(userData2);
        users.add(userData4);

        projectData2.setTeam(users);
        projectData2.setOwner(userData2);

        projectData4.setTeam(users);
        projectData4.setOwner(userData2);

        projects.add(projectData2);
        projects.add(projectData4);

        issueData2.parentId = projectData2.id;
        issueData4.parentId = projectData4.id;

        issueData2.setOwnerId(userData4.id);
        issueData4.setOwnerId(userData2.id);

        timeEntryData2.parentId = issueData2.id;
        timeEntryData4.parentId = issueData4.id;

        timeEntryData2.setUserId(userData2.id);
        timeEntryData4.setUserId(userData2.id);

        timeEntries.add(timeEntryData2);
        timeEntries.add(timeEntryData4);

        issues.add(issueData2);
        issues.add(issueData4);

        assertEquals(0, pc.getSize());
        assertEquals(0, uc.getSize());
        assertEquals(0, tc.getSize());
        assertEquals(0, ic.getSize());

        pc.putData(projects, false);
        uc.putData(users, false);
        ic.putData(issues, true);
        tc.putData(timeEntries, true);

        assertEquals(2, pc.getSize());
        assertEquals(2, uc.getSize());
        assertEquals(2, tc.getSize());
        assertEquals(2, ic.getSize());

        assertTrue(pc.getData(Optional.empty()).contains(projectData2));
        assertTrue(pc.getData(Optional.empty()).contains(projectData4));

        assertTrue(ic.getData(Optional.empty()).contains(issueData2));
        assertTrue(ic.getData(Optional.empty()).contains(issueData4));

        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData2));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData4));

        assertTrue(uc.getData(Optional.empty()).contains(userData2));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData2.id).getIssues().contains(issueData2));
        assertTrue(pc.getData(projectData4.id).getIssues().contains(issueData4));

        assertTrue(ic.getData(issueData2.id).getTimeEntries().contains(timeEntryData2));
        assertTrue(ic.getData(issueData4.id).getTimeEntries().contains(timeEntryData4));

        uc.removeData(userData4);

        assertTrue(uc.getData(Optional.empty()).contains(userData2));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData4.id).getTeam().contains(userData2));
        assertTrue(pc.getData(projectData2.id).getTeam().contains(userData2));

        assertTrue(pc.getData(projectData2.id).getTeam().contains(userData4));
        assertTrue(pc.getData(projectData4.id).getTeam().contains(userData4));

        assertEquals(2, pc.getSize());
        assertEquals(2, uc.getSize());
        assertEquals(2, tc.getSize());
        assertEquals(2, ic.getSize());
    }

    /*
     *  Given   a cached user, member of multiple [cached] projects
     *              that is owner of:
     *              1. project [0], issue [0], time entry [x],
     *              2. project [0], issue [0], time entry [0],
     *  When    removeUser(userData) is called,
     *  Then    the user is not removed
     */
    @Test
    public void testRemoveProjectMember_linkedEntities_timeEntryOwner() {

        List<ProjectData> projects = new ArrayList<>();
        List<UserData> users = new ArrayList<>();
        List<IssueData> issues = new ArrayList<>();
        List<TimeEntryData> timeEntries = new ArrayList<>();

        ProjectData projectData3 = new ProjectData();
        ProjectData projectData4 = new ProjectData();

        projectData3.id = 3L;
        projectData4.id = 4L;

        UserData userData3 = new UserData();
        UserData userData4 = new UserData();

        userData3.id = 3L;
        userData4.id = 4L;

        projectData3.setOwner(userData3);
        projectData4.setOwner(userData3);

        IssueData issueData3 = new IssueData();
        IssueData issueData4 = new IssueData();

        issueData3.id = 3L;
        issueData4.id = 4L;

        issueData3.setOwnerId(userData3.id);
        issueData4.setOwnerId(userData3.id);

        TimeEntryData timeEntryData3 = new TimeEntryData();
        TimeEntryData timeEntryData4 = new TimeEntryData();

        timeEntryData3.id = 3L;
        timeEntryData4.id = 4L;

        users.add(userData3);
        users.add(userData4);

        projectData3.setTeam(users);

        projectData4.setTeam(users);

        projects.add(projectData3);
        projects.add(projectData4);

        issueData3.parentId = projectData3.id;
        issueData4.parentId = projectData4.id;

        timeEntryData3.parentId = issueData3.id;
        timeEntryData4.parentId = issueData4.id;

        timeEntryData3.setUserId(userData4.id);
        timeEntryData4.setUserId(userData3.id);

        timeEntries.add(timeEntryData3);
        timeEntries.add(timeEntryData4);

        issues.add(issueData3);
        issues.add(issueData4);

        assertEquals(0, pc.getSize());
        assertEquals(0, uc.getSize());
        assertEquals(0, tc.getSize());
        assertEquals(0, ic.getSize());

        pc.putData(projects, false);
        uc.putData(users, false);
        ic.putData(issues, true);
        tc.putData(timeEntries, true);

        assertEquals(2, pc.getSize());
        assertEquals(2, uc.getSize());
        assertEquals(2, tc.getSize());
        assertEquals(2, ic.getSize());

        assertTrue(pc.getData(Optional.empty()).contains(projectData3));
        assertTrue(pc.getData(Optional.empty()).contains(projectData4));

        assertTrue(ic.getData(Optional.empty()).contains(issueData3));
        assertTrue(ic.getData(Optional.empty()).contains(issueData4));

        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData3));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData4));

        assertTrue(uc.getData(Optional.empty()).contains(userData3));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData3.id).getIssues().contains(issueData3));
        assertTrue(pc.getData(projectData4.id).getIssues().contains(issueData4));

        assertTrue(ic.getData(issueData3.id).getTimeEntries().contains(timeEntryData3));
        assertTrue(ic.getData(issueData4.id).getTimeEntries().contains(timeEntryData4));

        uc.removeData(userData4);

        assertTrue(uc.getData(Optional.empty()).contains(userData3));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData3.id).getTeam().contains(userData3));
        assertTrue(pc.getData(projectData4.id).getTeam().contains(userData3));

        assertTrue(pc.getData(projectData3.id).getTeam().contains(userData4));
        assertTrue(pc.getData(projectData4.id).getTeam().contains(userData4));

        assertEquals(2, pc.getSize());
        assertEquals(2, uc.getSize());
        assertEquals(2, tc.getSize());
        assertEquals(2, ic.getSize());
    }

    /*
     *  Given   a cached user, member of multiple [cached] projects
     *              that is owner of:
     *              1. project [x], issue [0], time entry [0]
     *              2. project [0], issue [x], time entry [0],
     *              3. project [0], issue [0], time entry [x],
     *              4. project [0], issue [0], time entry [0],
     *  When    removeUser(userData) is called,
     *  Then    the user is not removed
     */
    @Test
    public void testRemoveProjectMember_linkedEntities_eachOwner() {
        List<ProjectData> projects = new ArrayList<>();
        List<UserData> users = new ArrayList<>();
        List<IssueData> issues = new ArrayList<>();
        List<TimeEntryData> timeEntries = new ArrayList<>();

        ProjectData projectData1 = new ProjectData();
        ProjectData projectData2 = new ProjectData();
        ProjectData projectData3 = new ProjectData();
        ProjectData projectData4 = new ProjectData();

        projectData1.id = 1L;
        projectData2.id = 2L;
        projectData3.id = 3L;
        projectData4.id = 4L;

        UserData userData1 = new UserData();
        UserData userData2 = new UserData();
        UserData userData3 = new UserData();
        UserData userData4 = new UserData();

        userData1.id = 1L;
        userData2.id = 2L;
        userData3.id = 3L;
        userData4.id = 4L;

        projectData1.setOwner(userData4);

        IssueData issueData1 = new IssueData();
        IssueData issueData2 = new IssueData();
        IssueData issueData3 = new IssueData();
        IssueData issueData4 = new IssueData();

        issueData1.id = 1L;
        issueData2.id = 2L;
        issueData3.id = 3L;
        issueData4.id = 4L;

        issueData3.setOwnerId(userData4.id);

        TimeEntryData timeEntryData1 = new TimeEntryData();
        TimeEntryData timeEntryData2 = new TimeEntryData();
        TimeEntryData timeEntryData3 = new TimeEntryData();
        TimeEntryData timeEntryData4 = new TimeEntryData();

        timeEntryData1.id = 1L;
        timeEntryData2.id = 2L;
        timeEntryData3.id = 3L;
        timeEntryData4.id = 4L;

        users.add(userData1);
        users.add(userData4);
        projectData1.setTeam(users);

        users.clear();
        users.add(userData2);
        users.add(userData4);

        projectData2.setTeam(users);

        users.clear();
        users.add(userData3);
        users.add(userData4);

        projectData3.setTeam(users);

        users.clear();
        users.add(userData1);
        users.add(userData4);

        projectData4.setTeam(users);

        projects.add(projectData1);
        projects.add(projectData2);
        projects.add(projectData3);
        projects.add(projectData4);

        issueData1.parentId = projectData1.id;
        issueData2.parentId = projectData2.id;
        issueData3.parentId = projectData3.id;
        issueData4.parentId = projectData4.id;

        timeEntryData1.parentId = issueData1.id;
        timeEntryData2.parentId = issueData2.id;
        timeEntryData3.parentId = issueData3.id;
        timeEntryData4.parentId = issueData4.id;

        timeEntryData1.setUserId(userData4.id);
        timeEntryData2.setUserId(userData4.id);
        timeEntryData3.setUserId(userData4.id);

        timeEntryData4.setUserId(userData1.id);

        users.clear();
        users.add(userData1);
        users.add(userData2);
        users.add(userData3);
        users.add(userData4);

        timeEntries.add(timeEntryData1);
        timeEntries.add(timeEntryData2);
        timeEntries.add(timeEntryData3);
        timeEntries.add(timeEntryData4);

        issues.add(issueData1);
        issues.add(issueData2);
        issues.add(issueData3);
        issues.add(issueData4);

        assertEquals(0, pc.getSize());
        assertEquals(0, uc.getSize());
        assertEquals(0, tc.getSize());
        assertEquals(0, ic.getSize());

        pc.putData(projects, false);
        uc.putData(users, false);
        ic.putData(issues, true);
        tc.putData(timeEntries, true);

        assertEquals(4, pc.getSize());
        assertEquals(4, uc.getSize());
        assertEquals(4, tc.getSize());
        assertEquals(4, ic.getSize());

        assertTrue(pc.getData(Optional.empty()).contains(projectData1));
        assertTrue(pc.getData(Optional.empty()).contains(projectData2));
        assertTrue(pc.getData(Optional.empty()).contains(projectData3));
        assertTrue(pc.getData(Optional.empty()).contains(projectData4));

        assertTrue(ic.getData(Optional.empty()).contains(issueData1));
        assertTrue(ic.getData(Optional.empty()).contains(issueData2));
        assertTrue(ic.getData(Optional.empty()).contains(issueData3));
        assertTrue(ic.getData(Optional.empty()).contains(issueData4));

        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData1));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData2));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData3));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData4));

        assertTrue(uc.getData(Optional.empty()).contains(userData1));
        assertTrue(uc.getData(Optional.empty()).contains(userData2));
        assertTrue(uc.getData(Optional.empty()).contains(userData3));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData1.id).getIssues().contains(issueData1));
        assertTrue(pc.getData(projectData2.id).getIssues().contains(issueData2));
        assertTrue(pc.getData(projectData3.id).getIssues().contains(issueData3));
        assertTrue(pc.getData(projectData4.id).getIssues().contains(issueData4));

        assertTrue(ic.getData(issueData1.id).getTimeEntries().contains(timeEntryData1));
        assertTrue(ic.getData(issueData2.id).getTimeEntries().contains(timeEntryData2));
        assertTrue(ic.getData(issueData3.id).getTimeEntries().contains(timeEntryData3));
        assertTrue(ic.getData(issueData4.id).getTimeEntries().contains(timeEntryData4));

        uc.removeData(userData4);

        assertTrue(uc.getData(Optional.empty()).contains(userData1));
        assertTrue(uc.getData(Optional.empty()).contains(userData2));
        assertTrue(uc.getData(Optional.empty()).contains(userData3));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData1.id).getTeam().contains(userData1));
        assertTrue(pc.getData(projectData4.id).getTeam().contains(userData1));
        assertTrue(pc.getData(projectData2.id).getTeam().contains(userData2));
        assertTrue(pc.getData(projectData3.id).getTeam().contains(userData3));

        assertTrue(pc.getData(projectData1.id).getTeam().contains(userData4));
        assertTrue(pc.getData(projectData2.id).getTeam().contains(userData4));
        assertTrue(pc.getData(projectData3.id).getTeam().contains(userData4));
        assertTrue(pc.getData(projectData4.id).getTeam().contains(userData4));

        assertEquals(4, pc.getSize());
        assertEquals(4, uc.getSize());
        assertEquals(4, tc.getSize());
        assertEquals(4, ic.getSize());
    }

    /*
     *  Given   a cached user, member of multiple projects, owner of [at least one] project, issue and time entry
     *  When    putData(userData, boolean) is called,
     *  Then    the updated data is available in the user cache,
     *  And     the updated data is available in all related projects, issues, and time entries
     */
    @Test
    public void testUpdateProjectMember_linkedEntities_eachOwner() throws InterruptedException {
        String name = "Radio Star";
        String updatedName = "Video Star";
        List<ProjectData> projects = new ArrayList<>();
        List<UserData> users = new ArrayList<>();
        List<IssueData> issues = new ArrayList<>();
        List<TimeEntryData> timeEntries = new ArrayList<>();

        ProjectData projectData1 = new ProjectData();
        ProjectData projectData2 = new ProjectData();
        ProjectData projectData3 = new ProjectData();
        ProjectData projectData4 = new ProjectData();

        projectData1.id = 1L;
        projectData2.id = 2L;
        projectData3.id = 3L;
        projectData4.id = 4L;

        UserData userData1 = new UserData();
        UserData userData2 = new UserData();
        UserData userData3 = new UserData();
        UserData userData4 = new UserData();
        UserData userData4Updated = new UserData();

        userData1.id = 1L;
        userData2.id = 2L;
        userData3.id = 3L;
        userData4.id = 4L;
        userData4Updated.id = 4L;

        userData4.setName(name);
        userData4Updated.setName(updatedName);

        projectData1.setOwner(userData4);
        projectData2.setOwner(userData2);
        projectData3.setOwner(userData3);
        projectData4.setOwner(userData1);

        IssueData issueData1 = new IssueData();
        IssueData issueData2 = new IssueData();
        IssueData issueData3 = new IssueData();
        IssueData issueData4 = new IssueData();

        issueData1.id = 1L;
        issueData2.id = 2L;
        issueData3.id = 3L;
        issueData4.id = 4L;

        issueData1.setOwnerId(userData1.id);
        issueData2.setOwnerId(userData4.id);
        issueData3.setOwnerId(userData3.id);
        issueData4.setOwnerId(userData1.id);

        TimeEntryData timeEntryData1 = new TimeEntryData();
        TimeEntryData timeEntryData2 = new TimeEntryData();
        TimeEntryData timeEntryData3 = new TimeEntryData();
        TimeEntryData timeEntryData4 = new TimeEntryData();

        timeEntryData1.id = 1L;
        timeEntryData2.id = 2L;
        timeEntryData3.id = 3L;
        timeEntryData4.id = 4L;

        users.add(userData1);
        users.add(userData4);
        projectData1.setTeam(users);

        users.clear();
        users.add(userData2);
        users.add(userData4);

        projectData2.setTeam(users);

        users.clear();
        users.add(userData3);
        users.add(userData4);

        projectData3.setTeam(users);

        users.clear();
        users.add(userData1);
        users.add(userData4);

        projectData4.setTeam(users);

        projects.add(projectData1);
        projects.add(projectData2);
        projects.add(projectData3);
        projects.add(projectData4);

        issueData1.parentId = projectData1.id;
        issueData2.parentId = projectData2.id;
        issueData3.parentId = projectData3.id;
        issueData4.parentId = projectData4.id;

        timeEntryData1.parentId = issueData1.id;
        timeEntryData2.parentId = issueData2.id;
        timeEntryData3.parentId = issueData3.id;
        timeEntryData4.parentId = issueData4.id;

        timeEntryData1.setUserId(userData1.id);
        timeEntryData2.setUserId(userData2.id);
        timeEntryData3.setUserId(userData4.id);
        timeEntryData4.setUserId(userData1.id);

        users.clear();
        users.add(userData1);
        users.add(userData2);
        users.add(userData3);
        users.add(userData4);

        timeEntries.add(timeEntryData1);
        timeEntries.add(timeEntryData2);
        timeEntries.add(timeEntryData3);
        timeEntries.add(timeEntryData4);

        issues.add(issueData1);
        issues.add(issueData2);
        issues.add(issueData3);
        issues.add(issueData4);

        assertEquals(0, pc.getSize());
        assertEquals(0, uc.getSize());
        assertEquals(0, tc.getSize());
        assertEquals(0, ic.getSize());

        pc.putData(projects, false);
        uc.putData(users, false);
        ic.putData(issues, true);
        tc.putData(timeEntries, true);

        assertEquals(4, pc.getSize());
        assertEquals(4, uc.getSize());
        assertEquals(4, tc.getSize());
        assertEquals(4, ic.getSize());

        assertTrue(pc.getData(Optional.empty()).contains(projectData1));
        assertTrue(pc.getData(Optional.empty()).contains(projectData2));
        assertTrue(pc.getData(Optional.empty()).contains(projectData3));
        assertTrue(pc.getData(Optional.empty()).contains(projectData4));

        assertTrue(ic.getData(Optional.empty()).contains(issueData1));
        assertTrue(ic.getData(Optional.empty()).contains(issueData2));
        assertTrue(ic.getData(Optional.empty()).contains(issueData3));
        assertTrue(ic.getData(Optional.empty()).contains(issueData4));

        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData1));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData2));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData3));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData4));

        assertTrue(uc.getData(Optional.empty()).contains(userData1));
        assertTrue(uc.getData(Optional.empty()).contains(userData2));
        assertTrue(uc.getData(Optional.empty()).contains(userData3));
        assertTrue(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData1.id).getIssues().contains(issueData1));
        assertTrue(pc.getData(projectData2.id).getIssues().contains(issueData2));
        assertTrue(pc.getData(projectData3.id).getIssues().contains(issueData3));
        assertTrue(pc.getData(projectData4.id).getIssues().contains(issueData4));

        assertTrue(ic.getData(issueData1.id).getTimeEntries().contains(timeEntryData1));
        assertTrue(ic.getData(issueData2.id).getTimeEntries().contains(timeEntryData2));
        assertTrue(ic.getData(issueData3.id).getTimeEntries().contains(timeEntryData3));
        assertTrue(ic.getData(issueData4.id).getTimeEntries().contains(timeEntryData4));

        uc.putData(userData4Updated, true);

        Thread.sleep(napTime);

        assertTrue(uc.getData(Optional.empty()).contains(userData1));
        assertTrue(uc.getData(Optional.empty()).contains(userData2));
        assertTrue(uc.getData(Optional.empty()).contains(userData3));
        assertTrue(uc.getData(Optional.empty()).contains(userData4Updated));
        assertFalse(uc.getData(Optional.empty()).contains(userData4));

        assertTrue(pc.getData(projectData1.id).getTeam().contains(userData1));
        assertTrue(pc.getData(projectData4.id).getTeam().contains(userData1));
        assertTrue(pc.getData(projectData2.id).getTeam().contains(userData2));
        assertTrue(pc.getData(projectData3.id).getTeam().contains(userData3));

        assertTrue(pc.getData(projectData1.id).getTeam().contains(userData4Updated));
        assertTrue(pc.getData(projectData2.id).getTeam().contains(userData4Updated));
        assertTrue(pc.getData(projectData3.id).getTeam().contains(userData4Updated));
        assertTrue(pc.getData(projectData4.id).getTeam().contains(userData4Updated));

        assertEquals(4, pc.getSize());
        assertEquals(4, uc.getSize());
        assertEquals(4, tc.getSize());
        assertEquals(4, ic.getSize());

        assertEquals(updatedName, uc.getData(userData4Updated.id).getName());
        assertEquals(updatedName, pc.getData(projectData1.id).getOwner().getName());
        assertEquals(updatedName, tc.getData(timeEntryData3.id).getUserName());
    }

    /*
     *  Given   a cached project member that is an issue owner
     *  When    the member is updated
     *  Then    the issue owner information is also changed
     */
    @Test
    public void updateIssueOwnerData() throws InterruptedException {
        String email = "an@em.ail";
        String updatedEmail = "updated@em.ail";

        ProjectData projectData = new ProjectData();
        projectData.id = 1L;
        projectData.setTeam(new ArrayList<>());

        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issueData.parentId = projectData.id;

        UserData userData = new UserData();
        userData.id = 1L;
        userData.setEmail(email);
        projectData.getTeam().add(userData);

        UserData updatedUserData = new UserData();
        updatedUserData.id = 1L;
        updatedUserData.setEmail(updatedEmail);

        issueData.setOwnerId(userData.id);
        issueData.setOwner(userData);

        assertEquals(0, pc.getSize());
        assertEquals(0, ic.getSize());
        assertEquals(0, uc.getSize());

        pc.putData(projectData, false);
        ic.putData(issueData, true);
        uc.putData(userData, true);

        Thread.sleep(napTime);

        assertEquals(1, pc.getSize());
        assertEquals(1, ic.getSize());
        assertEquals(1, uc.getSize());

        assertTrue(pc.getData(1L).getIssues().contains(issueData));
        assertTrue(pc.getData(1L).getTeam().contains(userData));
        assertEquals(ic.getData(1L).getOwner(), userData);
        assertEquals(email, ic.getData(1L).getOwner().getEmail());

        uc.putData(updatedUserData, true);

        Thread.sleep(napTime);

        assertEquals(updatedEmail, ic.getData(1L).getOwner().getEmail());
        assertEquals(updatedUserData, ic.getData(1L).getOwner());
    }


    /*
     *  Given   a cached project member that is an issue owner, project owner, time entry owner
     *  When    the member is updated
     *  Then    the project, issue, time entry owner information is also changed
     *  And     the old user data is not found anywhere else in the cache
     *              (search projectData.owner, issueData.owner, users cache, time entry name/photo)
     */
    @Test
    public void updateIssueOwnerData_NoOldOwner() throws InterruptedException {
        String photo = "some/path/image.png";
        String updatedPhoto = "new/path/image.png";

        ProjectData projectData = new ProjectData();
        projectData.id = 1L;
        projectData.setTeam(new ArrayList<>());

        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issueData.parentId = projectData.id;

        UserData userData = new UserData();
        userData.id = 1L;
        userData.setPicture(photo);
        projectData.getTeam().add(userData);

        UserData updatedUserData = new UserData();
        updatedUserData.id = 1L;
        updatedUserData.setPicture(updatedPhoto);

        final int userDataHash = userData.hashCode();
        final int updatedUserDataHash = updatedUserData.hashCode();

        issueData.setOwnerId(userData.id);
        issueData.setOwner(userData);

        projectData.setOwner(userData);

        TimeEntryData timeEntryData = new TimeEntryData();
        timeEntryData.id = 1L;
        timeEntryData.parentId = issueData.id;
        timeEntryData.setUserId(userData.id);

        assertEquals(0, pc.getSize());
        assertEquals(0, ic.getSize());
        assertEquals(0, uc.getSize());
        assertEquals(0, tc.getSize());

        pc.putData(projectData, false);
        ic.putData(issueData, true);
        uc.putData(userData, true);
        tc.putData(timeEntryData, true);

        Thread.sleep(napTime);

        assertEquals(1, pc.getSize());
        assertEquals(1, ic.getSize());
        assertEquals(1, uc.getSize());
        assertEquals(1, tc.getSize());

        assertTrue(pc.getData(1L).getIssues().contains(issueData));
        assertTrue(pc.getData(1L).getTeam().contains(userData));
        assertTrue(ic.getData(issueData.id).getTimeEntries().contains(timeEntryData));
        assertEquals(userDataHash, pc.getData(projectData.id).getOwner().hashCode());
        assertEquals(userData, ic.getData(1L).getOwner());
        assertEquals(userDataHash, ic.getData(1L).getOwner().hashCode());
        assertEquals(photo, ic.getData(1L).getOwner().getPicture());

        uc.putData(updatedUserData, true);

        Thread.sleep(napTime);

        assertEquals(updatedPhoto, ic.getData(issueData.id).getOwner().getPicture());
        assertEquals(updatedPhoto, pc.getData(projectData.id).getOwner().getPicture());
        assertEquals(updatedPhoto, tc.getData(timeEntryData.id).getUserPhoto());
        assertEquals(updatedUserData, ic.getData(issueData.id).getOwner());

        assertEquals(updatedUserDataHash, pc.getData(projectData.id).getOwner().hashCode());
        assertEquals(updatedUserDataHash, ic.getData(1L).getOwner().hashCode());
    }

    /*
     *  Given   a clean cache
     *  When    multiple users are added from different threads,
     *  Then    all the users are stored in the cache
     */
    @Test
    public void insertMultipleUsers_parallel() {
        // given

        assertThat(uc.getSize(), is(0));

        // when

        final int threadCount = 10;
        final int insertCount = 10;

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean();
        AtomicInteger overlaps = new AtomicInteger();

        ExecutorService service = Executors.newFixedThreadPool(threadCount);

        for (long i = 1; i <= insertCount; i++) {
            UserData userData = new UserData();
            userData.id = i;
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
                uc.putData(userData, false);
            });
        }


        TestSubscriber<Byte> testSubscriber = cache.getBroadcaster()
                .filter(e -> e.equals(CACHE_EVENT_UPDATE_USER))
                .test();

        latch.countDown();

        testSubscriber.request(1);
        testSubscriber.awaitCount(insertCount, TestWaitStrategy.SLEEP_10MS, 100);
//        testSubscriber.assertValueCount(insertCount);

        // then

        assertThat(uc.getSize(), is(insertCount));
        assertThat(overlaps.get(), greaterThan(0));
    }

    /*
     *  Given   Ten projects with one issue each with one time entry each, all owned by different users
     *  When    the users are updated from different threads
     *  Then    all data is correctly updated and stored in the cache
     */
    @Test
    public void updateOwnerUsers_parallel() throws InterruptedException {
        // given

        assertThat(pc.getSize(), is(0));
        assertThat(uc.getSize(), is(0));
        assertThat(ic.getSize(), is(0));
        assertThat(tc.getSize(), is(0));

        final int threadCount = 10;
        final int projectCount = 10;
        AtomicLong userDataId = new AtomicLong(0);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean running = new AtomicBoolean();
        final AtomicInteger overlaps = new AtomicInteger();
        final ExecutorService service = Executors.newFixedThreadPool(threadCount);

        for (long i = 0; i < projectCount; i++) {
            ProjectData projectData = new ProjectData();
            projectData.id = i + 1;
            projectData.setTeam(new ArrayList<>());

            IssueData issueData = new IssueData();
            issueData.id = projectData.id;
            issueData.parentId = projectData.id;

            TimeEntryData timeEntryData = new TimeEntryData();
            timeEntryData.id = projectData.id;
            timeEntryData.parentId = issueData.id;

            UserData projectOwner = new UserData();
            projectOwner.id = userDataId.incrementAndGet();
            projectOwner.setName(String.valueOf(projectOwner.id));

            projectData.setOwner(projectOwner);

            UserData issueOwner = new UserData();
            issueOwner.id = userDataId.incrementAndGet();
            issueOwner.setName(String.valueOf(issueOwner.id));

            issueData.setOwner(issueOwner);
            issueData.setOwnerId(issueOwner.id);

            UserData timeEntryOwner = new UserData();
            timeEntryOwner.id = userDataId.incrementAndGet();
            timeEntryOwner.setName(String.valueOf(timeEntryOwner.id));

            timeEntryData.setUserId(timeEntryOwner.id);
            timeEntryData.setUserName(timeEntryOwner.getName());

            projectData.getTeam().add(projectOwner);
            projectData.getTeam().add(issueOwner);
            projectData.getTeam().add(timeEntryOwner);

            uc.putData(projectOwner, false);
            uc.putData(issueOwner, false);
            uc.putData(timeEntryOwner, false);
            pc.putData(projectData, false);
            ic.putData(issueData, true);
            tc.putData(timeEntryData, true);
        }

        assertThat(uc.getSize(), is(projectCount * 3));
        assertThat(pc.getSize(), is(projectCount));
        assertThat(ic.getSize(), is(projectCount));
        assertThat(tc.getSize(), is(projectCount));

        userDataId.set(0);

        // when

        for (long i = 0; i < projectCount; i++) {
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

                UserData projectOwner = new UserData();
                projectOwner.id = userDataId.incrementAndGet();
                projectOwner.setName("updated_" + projectOwner.id);

                UserData issueOwner = new UserData();
                issueOwner.id = userDataId.incrementAndGet();
                issueOwner.setName("updated_" + issueOwner.id);

                UserData timeEntryOwner = new UserData();
                timeEntryOwner.id = userDataId.incrementAndGet();
                timeEntryOwner.setName("updated_" + timeEntryOwner.id);

                uc.putData(projectOwner, true);
                uc.putData(issueOwner, true);
                uc.putData(timeEntryOwner, true);
            });
        }

        TestSubscriber<Byte> testSubscriber = cache.getBroadcaster()
//                .filter(event -> event.equals(CACHE_EVENT_UPDATE_USER))
                .test();

        testSubscriber.request(projectCount * 3);

        latch.countDown();

        testSubscriber.awaitCount(projectCount * 3);
//        testSubscriber.assertValueCount(projectCount * 3);

        assertThat(overlaps.get(), greaterThan(0));

        // then

        assertThat(uc.getSize(), is(projectCount * 3));

        List<ProjectData> projects = pc.getData(Optional.empty());
        for (ProjectData p : projects) {

            assertNotNull(p.getOwner());
            assertNotNull(p.getIssues().get(0).getOwner());

            UserData userData = p.getOwner();
            assertThat(userData.getName(), is("updated_" + userData.id));

            userData = p.getIssues().get(0).getOwner();
            assertThat(userData.getName(), is("updated_" + userData.id));

            TimeEntryData ted = p.getIssues().get(0).getTimeEntries().get(0);
            assertThat(ted.getUserName(), is("updated_" + uc.getData(ted.getUserId()).id));
        }
    }
}