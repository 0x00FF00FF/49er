package org.rares.miner49er.cache.optimizer;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.pushtorefresh.storio3.Optional;
import com.pushtorefresh.storio3.sqlite.Changes;
import edu.emory.mathcs.backport.java.util.Collections;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CacheFeedWorkerTest {

//    private ViewModelCache vmCache = new ViewModelCache();
    private ViewModelCache vmCache;
    private CacheFeedWorker cacheWorker = null;

    @Mock
    private AsyncGenericDao<UserData> uDao;
    @Mock
    private AsyncGenericDao<ProjectData> pDao;
    @Mock
    private AsyncGenericDao<IssueData> iDao;
    @Mock
    private AsyncGenericDao<TimeEntryData> tDao;

    private PublishProcessor<Changes> publishProcessor = PublishProcessor.create();
    private Flowable<Changes> dbChangesFlowable;

    private UserData testProjectOwner;
    private UserData testProjectTeamMember;
    private ProjectData testProject;
    private IssueData testIssue;
    private TimeEntryData testTimeEntry;

    private Cache<ProjectData> projectDataCache;
    private Cache<IssueData> issueDataCache;
    private Cache<TimeEntryData> timeEntryDataCache;
    private Cache<UserData> userDataCache;
    
    private final int waitTime = 25;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dbChangesFlowable = publishProcessor.share();
        if (vmCache != null) {
            vmCache.close();
        }
//        vmCache = new ViewModelCache();
        vmCache = ViewModelCacheSingleton.getInstance();
        projectDataCache = vmCache.getCache(ProjectData.class);
        issueDataCache = vmCache.getCache(IssueData.class);
        timeEntryDataCache = vmCache.getCache(TimeEntryData.class);
        userDataCache = vmCache.getCache(UserData.class);

        setUpMocks();
        setUpData();

        cacheWorker = new CacheFeedWorker.Builder()
                .timeEntriesDao(tDao)
                .issuesDao(iDao)
                .projectsDao(pDao)
                .userDao(uDao)
                .cache(vmCache)
                .build();
    }

    @After
    public void tearDown() {
        publishProcessor.onComplete();
    }

    private void setUpData() {
        testProjectOwner = new UserData();
        testProjectOwner.setName("Parshival Faker");
        testProjectOwner.setPicture("");
        testProjectOwner.setEmail("fake.user@emailindustry.net");
        testProjectOwner.setPassword("nice_try");
        testProjectOwner.setActive(true);
        testProjectOwner.setRole(1);
        testProjectOwner.setApiKey("A_P_I___K_E_Y");
        testProjectOwner.id = 1L;
        testProjectOwner.deleted = false;

        testProjectTeamMember = new UserData();
        testProjectTeamMember.updateData(testProjectOwner);
        testProjectTeamMember.setName("Phonyeus Userastic");
        testProjectTeamMember.setEmail("phoney.user@emailindustry.net");
        testProjectTeamMember.id = 2L;
        testProjectTeamMember.deleted = false;

        testProject = new ProjectData();
        testProject.id = 1L;
        testProject.deleted = false;
        testProject.lastUpdated = System.currentTimeMillis();
        testProject.setName("Fake Project Name");
        testProject.setDateAdded(System.currentTimeMillis());
        testProject.setDescription("This is a fake project to be used inside a test case.");
        testProject.setIcon("");
        testProject.setPicture("");
        testProject.parentId = testProjectOwner.id;
        // the project comes with the team already added (ProjectTeamGetResolver)
        List<UserData> team = new ArrayList<>();
        team.add(testProjectTeamMember);
        testProject.setTeam(team);

        testIssue = new IssueData();
        testIssue.setDateAdded(System.currentTimeMillis());
        testIssue.setDateDue(System.currentTimeMillis() + 100000);
        testIssue.setName("Issue number five");
        testIssue.setOwnerId(testProjectTeamMember.id);
        testIssue.id = 1L;
        testIssue.deleted = false;
        testIssue.parentId = testProject.id;
        testIssue.lastUpdated = System.currentTimeMillis();

        testTimeEntry = new TimeEntryData();
        testTimeEntry.setHours(2);
        testTimeEntry.setDateAdded(System.currentTimeMillis());
        testTimeEntry.setWorkDate(System.currentTimeMillis());
        testTimeEntry.setUserId(testProjectTeamMember.id);
        testTimeEntry.deleted = false;
        testTimeEntry.id = 1L;
        testTimeEntry.parentId = testIssue.id;

        vmCache.clear();
        // cache -> putData should be taken care of in the mocked/fake dao.getAll()
        // but we just do it here
        projectDataCache.putData(testProject, false);
        issueDataCache.putData(testIssue, false);
        timeEntryDataCache.putData(testTimeEntry, false);
        userDataCache.putData(testProjectOwner, false);
        userDataCache.putData(testProjectTeamMember, false);
    }

    private void setUpMocks() {
        Mockito.when(uDao.getAll(true)).thenReturn(Single.just(new ArrayList<>()));
/*
        Mockito.when(uDao.get(0, true)).thenReturn(Single.just(Optional.empty()));
        Mockito.when(uDao.get(1, true)).thenReturn(Single.just(Optional.of(testProjectOwner)));
        Mockito.when(uDao.getAll(1, true)).thenReturn(Single.just(userDataList));
        Mockito.when(uDao.getMatching("", Optional.empty(), true)).thenReturn(Single.just(userDataList));
        Mockito.when(uDao.delete(testProjectTeamMember)).thenReturn(Single.just(true));
        Mockito.when(uDao.delete(testProjectOwner)).thenReturn(Single.just(false));
        Mockito.when(uDao.insert(testProjectOwner)).thenReturn(Single.just(1L));
        Mockito.when(uDao.update(testProjectOwner)).thenReturn(Single.just(true));
        Mockito.when(uDao.getDbChangesFlowable()).thenReturn(dbChangesFlowable);            */

        Mockito.when(pDao.getAll(true)).thenReturn(Single.just(new ArrayList<>()));
        Mockito.when(iDao.getAll(true)).thenReturn(Single.just(new ArrayList<>()));
        Mockito.when(tDao.getAll(true)).thenReturn(Single.just(new ArrayList<>()));
    }

    /*
     * Given     a cache containing lists of unlinked objects,
     * When      enqueue cache fill is called,
     * Then      the cached objects will be linked
     * And       the entities are not changed (other than being linked)
     * And       the cache does not contain other entities than the original ones
     */
    @Test
    public void enqueueCacheFill_Happy() throws InterruptedException {

        assertNotNull(projectDataCache.getData(1L).getTeam());  // project comes with team.
        assertNull(projectDataCache.getData(1L).getOwner());
        assertNull(issueDataCache.getData(1L).getTimeEntries());
        assertNull(issueDataCache.getData(1L).getOwner());

        assertEquals(1, projectDataCache.getData(Optional.empty()).size());
        assertEquals(1, issueDataCache.getData(Optional.empty()).size());
        assertEquals(1, timeEntryDataCache.getData(Optional.empty()).size());
        assertEquals(2, userDataCache.getData(Optional.empty()).size());

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        assertEquals(1, projectDataCache.getData(Optional.empty()).size());
        assertEquals(1, issueDataCache.getData(Optional.empty()).size());
        assertEquals(1, timeEntryDataCache.getData(Optional.empty()).size());
        assertEquals(2, userDataCache.getData(Optional.empty()).size());

        assertEquals(testProject.getName(), projectDataCache.getData(Optional.empty()).get(0).getName());
        assertEquals(1, projectDataCache.getData(Optional.empty()).get(0).getIssues().size());
        assertEquals(testIssue.id, projectDataCache.getData(Optional.empty()).get(0).getIssues().get(0).id);
        assertEquals(testIssue.getName(), issueDataCache.getData(Optional.empty()).get(0).getName());
        assertEquals(1, issueDataCache.getData(Optional.empty()).get(0).getTimeEntries().size());
        assertEquals(testTimeEntry.getComments(), timeEntryDataCache.getData(Optional.empty()).get(0).getComments());
        assertEquals(testProjectOwner.getName(), projectDataCache.getData(Optional.empty()).get(0).getOwner().getName());
        assertEquals(testProjectOwner.getName(), userDataCache.getData(testProjectOwner.id).getName());
        assertEquals(testProjectOwner.id, userDataCache.getData(testProjectOwner.id).id);
        assertEquals(testProjectTeamMember.getName(), projectDataCache.getData(Optional.empty()).get(0).getTeam().get(0).getName());
        assertEquals(testProjectTeamMember.getName(), userDataCache.getData(testProjectTeamMember.id).getName());
        assertEquals(testProjectTeamMember.id, userDataCache.getData(testProjectTeamMember.id).id);
    }

    /*
     *  Given   cached unlinked models with project without a team
     *  When    enqueue cache fill is called
     *  Then    the entities are linked and the project still contains no team
     *  And     the entities are not changed (other than being linked)
     *  And     the cache does not contain other entities than the original ones
     */
    @Test
    public void enqueueCacheFill_noTeam() throws InterruptedException {
        projectDataCache.getData(1L).setTeam(null);

        assertNull(projectDataCache.getData(1L).getTeam());
        assertNull(projectDataCache.getData(1L).getOwner());
        assertNull(issueDataCache.getData(1L).getTimeEntries());
        assertNull(issueDataCache.getData(1L).getOwner());

        assertEquals(1, projectDataCache.getData(Optional.empty()).size());
        assertEquals(1, issueDataCache.getData(Optional.empty()).size());
        assertEquals(1, timeEntryDataCache.getData(Optional.empty()).size());
        assertEquals(2, userDataCache.getData(Optional.empty()).size());

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        assertEquals(1, projectDataCache.getData(Optional.empty()).size());
        assertEquals(1, issueDataCache.getData(Optional.empty()).size());
        assertEquals(1, timeEntryDataCache.getData(Optional.empty()).size());
        assertEquals(2, userDataCache.getData(Optional.empty()).size());

        assertEquals(testProject.getName(), projectDataCache.getData(Optional.empty()).get(0).getName());
        assertEquals(1, projectDataCache.getData(Optional.empty()).get(0).getIssues().size());
        assertEquals(testIssue.id, projectDataCache.getData(Optional.empty()).get(0).getIssues().get(0).id);
        assertEquals(testIssue.getName(), issueDataCache.getData(Optional.empty()).get(0).getName());
        assertEquals(1, issueDataCache.getData(Optional.empty()).get(0).getTimeEntries().size());
        assertEquals(testTimeEntry.getComments(), timeEntryDataCache.getData(Optional.empty()).get(0).getComments());
        assertEquals(testProjectOwner.id, userDataCache.getData(testProjectOwner.id).id);
        assertEquals(testProjectOwner.getName(), userDataCache.getData(testProjectOwner.id).getName());
        assertEquals(testProjectTeamMember.id, userDataCache.getData(testProjectTeamMember.id).id);
        assertEquals(testProjectTeamMember.getName(), userDataCache.getData(testProjectTeamMember.id).getName());

        assertEquals(testProjectOwner.getName(), projectDataCache.getData(Optional.empty()).get(0).getOwner().getName());
        assertEquals(Collections.emptyList(), projectDataCache.getData(Optional.empty()).get(0).getTeam());
    }

    /*
     *  Given   some cached entities with no issues or time entries
     *  When    enqueue cache fill runs
     *  Then    the project will have no issues
     *  And     the cached entities will have no changes
     *  And     the cache does not contain other entities than the original ones
     */
    @Test
    public void enqueueCacheFill_noIssues() throws InterruptedException {
        timeEntryDataCache.removeData(testTimeEntry);
        issueDataCache.removeData(testIssue);

        assertNotNull(projectDataCache.getData(1L).getTeam());  // project comes with team.
        assertNull(projectDataCache.getData(1L).getOwner());
        assertNull(issueDataCache.getData(1L));

        assertEquals(1, projectDataCache.getData(Optional.empty()).size());
        assertEquals(0, issueDataCache.getData(Optional.empty()).size());
        assertEquals(0, timeEntryDataCache.getData(Optional.empty()).size());
        assertEquals(2, userDataCache.getData(Optional.empty()).size());

        assertNull(projectDataCache.getData(Optional.empty()).get(0).getIssues());

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        assertEquals(1, projectDataCache.getData(Optional.empty()).size());
        assertEquals(0, issueDataCache.getData(Optional.empty()).size());
        assertEquals(0, timeEntryDataCache.getData(Optional.empty()).size());
        assertEquals(2, userDataCache.getData(Optional.empty()).size());

        assertEquals(testProject.getName(), projectDataCache.getData(Optional.empty()).get(0).getName());

        assertEquals(0, projectDataCache.getData(Optional.empty()).get(0).getIssues().size());
        assertEquals(Collections.emptyList(), projectDataCache.getData(Optional.empty()).get(0).getIssues());
        assertEquals(0, issueDataCache.getData(Optional.empty()).size());

        assertEquals(testProjectOwner.getName(), projectDataCache.getData(Optional.empty()).get(0).getOwner().getName());
        assertEquals(testProjectOwner.getName(), userDataCache.getData(testProjectOwner.id).getName());
        assertEquals(testProjectOwner.id, userDataCache.getData(testProjectOwner.id).id);
        assertEquals(testProjectTeamMember.getName(), projectDataCache.getData(Optional.empty()).get(0).getTeam().get(0).getName());
        assertEquals(testProjectTeamMember.getName(), userDataCache.getData(testProjectTeamMember.id).getName());
        assertEquals(testProjectTeamMember.id, userDataCache.getData(testProjectTeamMember.id).id);
    }

    /*
     *  Given   cached entities with no time entries
     *  When    enqueue cache fill runs
     *  Then    the cached entities are linked
     *  And     the cached entities contents don't change (other than being linked)
     *  And     the cache does not contain other entities than the original ones
     */
    @Test
    public void enqueueCacheFill_noTimeEntries() throws InterruptedException {
        timeEntryDataCache.removeData(testTimeEntry);

        assertNotNull(projectDataCache.getData(1L).getTeam());  // project comes with team.
        assertNull(projectDataCache.getData(1L).getOwner());
        assertNull(issueDataCache.getData(1L).getTimeEntries());
        assertNull(issueDataCache.getData(1L).getOwner());
        assertNull(timeEntryDataCache.getData(1L));

        assertEquals(1, projectDataCache.getData(Optional.empty()).size());
        assertEquals(1, issueDataCache.getData(Optional.empty()).size());
        assertEquals(0, timeEntryDataCache.getData(Optional.empty()).size());
        assertEquals(2, userDataCache.getData(Optional.empty()).size());

        assertNull(issueDataCache.getData(Optional.empty()).get(0).getTimeEntries());

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        assertEquals(1, projectDataCache.getData(Optional.empty()).size());
        assertEquals(1, issueDataCache.getData(Optional.empty()).size());
        assertEquals(0, timeEntryDataCache.getData(Optional.empty()).size());
        assertEquals(2, userDataCache.getData(Optional.empty()).size());

        assertEquals(testProject.getName(), projectDataCache.getData(Optional.empty()).get(0).getName());
        assertEquals(1, projectDataCache.getData(Optional.empty()).get(0).getIssues().size());
        assertEquals(testIssue.id, projectDataCache.getData(Optional.empty()).get(0).getIssues().get(0).id);
        assertEquals(testIssue.getName(), issueDataCache.getData(Optional.empty()).get(0).getName());

//         FIXME: 23.05.2019
        assertNull(issueDataCache.getData(Optional.empty()).get(0).getTimeEntries());
//        assertEquals(0, issueDataCache.getData(Optional.empty()).get(0).getTimeEntries().size());
//        assertEquals(Collections.emptyList(), issueDataCache.getData(Optional.empty()).get(0).getTimeEntries());

        assertEquals(testProjectOwner.getName(), projectDataCache.getData(Optional.empty()).get(0).getOwner().getName());
        assertEquals(testProjectOwner.getName(), userDataCache.getData(testProjectOwner.id).getName());
        assertEquals(testProjectOwner.id, userDataCache.getData(testProjectOwner.id).id);
        assertEquals(testProjectTeamMember.getName(), projectDataCache.getData(Optional.empty()).get(0).getTeam().get(0).getName());
        assertEquals(testProjectTeamMember.getName(), userDataCache.getData(testProjectTeamMember.id).getName());
        assertEquals(testProjectTeamMember.id, userDataCache.getData(testProjectTeamMember.id).id);
    }

    /*
     *  Given   no projects in the cache
     *  When    enqueue cache fill runs
     *  Then    no projects will be added
     *  TODO: 23.05.2019: verify that the network service gets called?
     */
    @Test
    public void enqueueCacheFill_noProjects() throws InterruptedException {
        projectDataCache.removeData(testProject);
        assertEquals(0, projectDataCache.getData(Optional.empty()).size());

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        assertEquals(0, projectDataCache.getData(Optional.empty()).size());
    }

    /*
     *  Given   some entities in the cache that are not linked
     *              (one project, one issue, one time entry)
     *  When    the enqueue cache fill is called
     *  And     the worker is closed
     *  Then    the cache entities are not completely linked
     */
    @Test
    public void enqueueCacheFill_disposedBeforeBeginning() {

        assertNotNull(projectDataCache.getData(1L).getTeam());  // project comes with team.
        assertNull(projectDataCache.getData(1L).getOwner());
        assertNull(issueDataCache.getData(1L).getTimeEntries());
        assertNull(issueDataCache.getData(1L).getOwner());

        assertEquals(1, projectDataCache.getData(Optional.empty()).size());
        assertEquals(1, issueDataCache.getData(Optional.empty()).size());
        assertEquals(1, timeEntryDataCache.getData(Optional.empty()).size());
        assertEquals(2, userDataCache.getData(Optional.empty()).size());

        cacheWorker.enqueueCacheFill();
        cacheWorker.close();

        assertEquals(1, projectDataCache.getData(Optional.empty()).size());
        assertEquals(1, issueDataCache.getData(Optional.empty()).size());
        assertEquals(1, timeEntryDataCache.getData(Optional.empty()).size());
        assertEquals(2, userDataCache.getData(Optional.empty()).size());

        // on a super stellar fast machine, this can be flaky
        assertTrue(
                projectDataCache.getData(1L).getOwner() == null ||
                        issueDataCache.getData(1L).getTimeEntries() == null ||
                        issueDataCache.getData(1L).getOwner() == null);
    }

    /*
     *  Given   some entities in the cache that are already linked
     *              (one project containing one issue containing one time entry)
     *  When    the issues in the cache change
     *  And     enqueue cache fill runs
     *  Then    the project will have the issue list with new contents
     */
    @Test
    public void enqueueCacheFill_issueCacheUpdate() throws InterruptedException {

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        IssueData newIssue = new IssueData();
        newIssue.updateData(testIssue);
        newIssue.setOwner(null);
        newIssue.id = testIssue.id;
        newIssue.setOwnerId(4L);

        assertNotEquals(newIssue, issueDataCache.getData(Optional.empty()).get(0));

        issueDataCache.putData(newIssue, false);

        assertEquals(newIssue, issueDataCache.getData(Optional.empty()).get(0));

        assertEquals(testIssue.getOwnerId(), projectDataCache.getData(Optional.empty()).get(0).getIssues().get(0).getOwnerId());

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        assertEquals(newIssue.getOwnerId(), projectDataCache.getData(Optional.empty()).get(0).getIssues().get(0).getOwnerId());
    }

    /*
     *  Given   some entities in the cache that are already linked
     *              (one project containing one issue containing one time entry)
     *  When    a new issue is added to the cache
     *  And     enqueue cache fill runs
     *  Then    the project will have two issues
     */
    @Test
    public void enqueueCacheFill_addIssueToCache() throws InterruptedException {

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        IssueData newIssue = new IssueData();
        newIssue.updateData(testIssue);
        newIssue.id = 2L;

        assertNotEquals(newIssue, issueDataCache.getData(Optional.empty()).get(0));

        issueDataCache.putData(newIssue, false);

        assertEquals(newIssue, issueDataCache.getData(Optional.empty()).get(1));

        assertEquals(1, projectDataCache.getData(Optional.empty()).get(0).getIssues().size());

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        assertEquals(2, projectDataCache.getData(Optional.empty()).get(0).getIssues().size());
    }

    /*
     *  Given   some entities in the cache that are already linked
     *              (one project containing one issue containing one time entry)
     *  When    the time entry is modified
     *  And     enqueue cache fill runs
     *  Then    the project's issue will have the new time entry data
     *  And     the issue will only have one time entry
     */
    @Test
    public void enqueueCacheFill_timeEntryCacheUpdate() throws InterruptedException {

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        TimeEntryData newTimeEntry = new TimeEntryData();
        newTimeEntry.updateData(testTimeEntry);
        newTimeEntry.id = testTimeEntry.id;
        newTimeEntry.setComments("new time entry comments");

        assertNotEquals(newTimeEntry, timeEntryDataCache.getData(Optional.empty()).get(0));

        timeEntryDataCache.putData(newTimeEntry, false);

        assertEquals(newTimeEntry, timeEntryDataCache.getData(Optional.empty()).get(0));

        assertEquals(testTimeEntry.getComments(), issueDataCache.getData(Optional.empty()).get(0).getTimeEntries().get(0).getComments());

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        assertEquals(1, issueDataCache.getData(Optional.empty()).get(0).getTimeEntries().size());
        assertEquals(newTimeEntry.getComments(), issueDataCache.getData(Optional.empty()).get(0).getTimeEntries().get(0).getComments());
    }


    /*
     *  Given   some entities in the cache that are already linked
     *              (one project containing one issue containing one time entry)
     *  When    the time entry is modified
     *  And     enqueue cache fill runs
     *  Then    the issue will have two time entries
     */
    @Test
    public void enqueueCacheFill_addTimeEntryToCache() throws InterruptedException {

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        TimeEntryData newTimeEntry = new TimeEntryData();
        newTimeEntry.updateData(testTimeEntry);
        newTimeEntry.setComments("new time entry comments");
        newTimeEntry.id = 2L;

        assertNotEquals(newTimeEntry, timeEntryDataCache.getData(Optional.empty()).get(0));

        timeEntryDataCache.putData(newTimeEntry, false);

        assertEquals(2, timeEntryDataCache.getData(Optional.empty()).size());

        assertEquals(1, issueDataCache.getData(Optional.empty()).get(0).getTimeEntries().size());

        cacheWorker.enqueueCacheFill();
        Thread.sleep(waitTime);

        assertEquals(2, issueDataCache.getData(Optional.empty()).get(0).getTimeEntries().size());
    }
}