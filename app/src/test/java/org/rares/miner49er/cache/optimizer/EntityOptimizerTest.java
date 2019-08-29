package org.rares.miner49er.cache.optimizer;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.reactivex.Single;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.rares.miner49er.cache.optimizer.EntityOptimizer.DbUpdateFinishedListener;
import org.rares.miner49er.fakes.GenericDaoFake;
import org.rares.miner49er.persistence.dao.GenericEntityDao;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EntityOptimizerTest {

    private final long napTime = 100;

    private EntityOptimizer entityOptimizer;

    @Mock
    private GenericEntityDao<Project> projectDao;
    @Mock
    private GenericEntityDao<Issue> issueDao;
    @Mock
    private GenericEntityDao<TimeEntry> timeEntryDao;
    @Mock
    private GenericEntityDao<User> userDao;

    private User teamMember1;
    private User teamMember2;
    private User projectOwner1;
    private User projectOwner2;

    private Project project1;
    private Project project2;

    private Issue issue1;
    private Issue issue2;
    private Issue issue3;
    private Issue issue4;

    private TimeEntry timeEntry1;
    private TimeEntry timeEntry2;
    private TimeEntry timeEntry3;
    private TimeEntry timeEntry4;
    private TimeEntry timeEntry5;
    private TimeEntry timeEntry6;
    private TimeEntry timeEntry7;
    private TimeEntry timeEntry8;

    private List<User> users;
    private List<Project> projects;
    private List<Issue> issues;
    private List<TimeEntry> timeEntries;

    // sets up 2 projects with 2 issues each with 2 time entries each,
    // 2 team members present in each project,
    // 1 project owner for each project
    // linked as they would come from a getProjects API call
    // * getProjects will only get project data in the future
    // * will not contain owner, issues or time entries data
    @Before
    public void setUp() {
        projectOwner1 = new User();
        projectOwner1.setName("Dark Faker");
        projectOwner1.setPhoto("");
        projectOwner1.setEmail("fake.user.1@emailindustry.net");
        projectOwner1.setPwd("nice_try");
        projectOwner1.setRole(1);
        projectOwner1.setApiKey("A_P_I___K_E_Y");
        projectOwner1.setId(1L);
        projectOwner1.setActive(1);

        projectOwner2 = new User();
        projectOwner2.setName("Master Joda");
        projectOwner2.setPhoto("");
        projectOwner2.setEmail("fake.user.2@emailindustry.net");
        projectOwner2.setPwd("nice_try");
        projectOwner2.setRole(1);
        projectOwner2.setApiKey("A_P_I___K_E_Y");
        projectOwner2.setId(2L);
        projectOwner2.setActive(1);

        teamMember1 = new User();
        teamMember1.setName("Will Helm");
        teamMember1.setPhoto("");
        teamMember1.setEmail("fake.user.2@emailindustry.net");
        teamMember1.setPwd("nice_try");
        teamMember1.setRole(1);
        teamMember1.setApiKey("A_P_I___K_E_Y");
        teamMember1.setId(3L);
        teamMember1.setActive(1);

        teamMember2 = new User();
        teamMember2.setName("Steven Cliche");
        teamMember2.setPhoto("");
        teamMember2.setEmail("fake.user.2@emailindustry.net");
        teamMember2.setPwd("nice_try");
        teamMember2.setRole(1);
        teamMember2.setApiKey("A_P_I___K_E_Y");
        teamMember2.setId(4L);
        teamMember2.setActive(1);

        timeEntry1 = new TimeEntry();
        timeEntry1.setId(1L);
        timeEntry1.setComments("time entry 1 comments");
        timeEntry1.setDateAdded(System.currentTimeMillis());
        timeEntry1.setDeleted(0);
        timeEntry1.setHours(8);
        timeEntry1.setIssueId(1L);
        timeEntry1.setUserId(teamMember1.getId());
        timeEntry1.setWorkDate(System.currentTimeMillis());
        timeEntry1.setLastUpdated(System.currentTimeMillis());

        timeEntry2 = new TimeEntry();
        timeEntry2.setId(2L);
        timeEntry2.setComments("time entry 2 comments");
        timeEntry2.setDateAdded(System.currentTimeMillis());
        timeEntry2.setDeleted(0);
        timeEntry2.setHours(8);
        timeEntry2.setIssueId(1L);
        timeEntry2.setUserId(teamMember2.getId());
        timeEntry2.setWorkDate(System.currentTimeMillis());
        timeEntry2.setLastUpdated(System.currentTimeMillis());

        timeEntry3 = new TimeEntry();
        timeEntry3.setId(3L);
        timeEntry3.setComments("time entry 3 comments");
        timeEntry3.setDateAdded(System.currentTimeMillis());
        timeEntry3.setDeleted(0);
        timeEntry3.setHours(8);
        timeEntry3.setIssueId(2L);
        timeEntry3.setUserId(teamMember1.getId());
        timeEntry3.setWorkDate(System.currentTimeMillis());
        timeEntry3.setLastUpdated(System.currentTimeMillis());

        timeEntry4 = new TimeEntry();
        timeEntry4.setId(4L);
        timeEntry4.setComments("time entry 4 comments");
        timeEntry4.setDateAdded(System.currentTimeMillis());
        timeEntry4.setDeleted(0);
        timeEntry4.setHours(8);
        timeEntry4.setIssueId(2L);
        timeEntry4.setUserId(teamMember2.getId());
        timeEntry4.setWorkDate(System.currentTimeMillis());
        timeEntry4.setLastUpdated(System.currentTimeMillis());

        timeEntry5 = new TimeEntry();
        timeEntry5.setId(5L);
        timeEntry5.setComments("time entry 5 comments");
        timeEntry5.setDateAdded(System.currentTimeMillis());
        timeEntry5.setDeleted(0);
        timeEntry5.setHours(8);
        timeEntry5.setIssueId(3L);
        timeEntry5.setUserId(teamMember1.getId());
        timeEntry5.setWorkDate(System.currentTimeMillis());
        timeEntry5.setLastUpdated(System.currentTimeMillis());

        timeEntry6 = new TimeEntry();
        timeEntry6.setId(6L);
        timeEntry6.setComments("time entry 6 comments");
        timeEntry6.setDateAdded(System.currentTimeMillis());
        timeEntry6.setDeleted(0);
        timeEntry6.setHours(8);
        timeEntry6.setIssueId(3L);
        timeEntry6.setUserId(teamMember2.getId());
        timeEntry6.setWorkDate(System.currentTimeMillis());
        timeEntry6.setLastUpdated(System.currentTimeMillis());

        timeEntry7 = new TimeEntry();
        timeEntry7.setId(7L);
        timeEntry7.setComments("time entry 7 comments");
        timeEntry7.setDateAdded(System.currentTimeMillis());
        timeEntry7.setDeleted(0);
        timeEntry7.setHours(8);
        timeEntry7.setIssueId(4L);
        timeEntry7.setUserId(teamMember1.getId());
        timeEntry7.setWorkDate(System.currentTimeMillis());
        timeEntry7.setLastUpdated(System.currentTimeMillis());

        timeEntry8 = new TimeEntry();
        timeEntry8.setId(8L);
        timeEntry8.setComments("time entry 8 comments");
        timeEntry8.setDateAdded(System.currentTimeMillis());
        timeEntry8.setDeleted(0);
        timeEntry8.setHours(8);
        timeEntry8.setIssueId(4L);
        timeEntry8.setUserId(teamMember2.getId());
        timeEntry8.setWorkDate(System.currentTimeMillis());
        timeEntry8.setLastUpdated(System.currentTimeMillis());

        issue1 = new Issue();
        issue1.setId(1L);
        issue1.setDateAdded(System.currentTimeMillis());
        issue1.setDeleted(0);
        issue1.setLastUpdated(System.currentTimeMillis());
        issue1.setName("issue 1");
        issue1.setOwnerId(teamMember1.getId());
        issue1.setProjectId(1L);
        List<TimeEntry> teList = new ArrayList<>();
        teList.add(timeEntry1);
        teList.add(timeEntry2);
        issue1.setTimeEntries(teList);

        issue2 = new Issue();
        issue2.setId(2L);
        issue2.setDateAdded(System.currentTimeMillis());
        issue2.setDeleted(0);
        issue2.setLastUpdated(System.currentTimeMillis());
        issue2.setName("issue 2");
        issue2.setOwnerId(teamMember2.getId());
        issue2.setProjectId(1L);
        teList = new ArrayList<>();
        teList.add(timeEntry3);
        teList.add(timeEntry4);
        issue2.setTimeEntries(teList);

        issue3 = new Issue();
        issue3.setId(3L);
        issue3.setDateAdded(System.currentTimeMillis());
        issue3.setDeleted(0);
        issue3.setLastUpdated(System.currentTimeMillis());
        issue3.setName("issue 3");
        issue3.setOwnerId(teamMember1.getId());
        issue3.setProjectId(2L);
        teList = new ArrayList<>();
        teList.add(timeEntry5);
        teList.add(timeEntry6);
        issue3.setTimeEntries(teList);

        issue4 = new Issue();
        issue4.setId(4L);
        issue4.setDateAdded(System.currentTimeMillis());
        issue4.setDeleted(0);
        issue4.setLastUpdated(System.currentTimeMillis());
        issue4.setName("issue 4");
        issue4.setOwnerId(teamMember2.getId());
        issue4.setProjectId(2L);
        teList = new ArrayList<>();
        teList.add(timeEntry7);
        teList.add(timeEntry8);
        issue4.setTimeEntries(teList);

        project1 = new Project();
        project1.setDateAdded(System.currentTimeMillis());
        project1.setDeleted(0);
        project1.setDescription("project 1 description");
        project1.setIcon("");
        project1.setPicture("");
        project1.setId(1L);
        project1.setLastUpdated(System.currentTimeMillis());
        project1.setName("project 1");
        project1.setOwner(projectOwner1);
        List<User> team = new ArrayList<>();
        team.add(teamMember1);
        team.add(teamMember2);
        project1.setTeam(team);
        List<Issue> issues = new ArrayList<>();
        issues.add(issue1);
        issues.add(issue2);
        project1.setIssues(issues);

        project2 = new Project();
        project2.setDateAdded(System.currentTimeMillis());
        project2.setDeleted(0);
        project2.setDescription("project 2 description");
        project2.setIcon("");
        project2.setPicture("");
        project2.setId(2L);
        project2.setLastUpdated(System.currentTimeMillis());
        project2.setName("project 2");
        project2.setOwner(projectOwner2);
        team = new ArrayList<>();
        team.add(teamMember1);
        team.add(teamMember2);
        project2.setTeam(team);
        issues = new ArrayList<>();
        issues.add(issue3);
        issues.add(issue4);
        project2.setIssues(issues);

        this.projects = new ArrayList<>();

        setUpMocks();
    }

    private void setUpMocks() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(userDao.insert(Matchers.anyListOf(User.class))).thenReturn(Single.just(true));
        Mockito.when(projectDao.insert(Matchers.anyListOf(Project.class))).thenReturn(Single.just(true));
        Mockito.when(issueDao.insert(Matchers.anyListOf(Issue.class))).thenReturn(Single.just(true));
        Mockito.when(timeEntryDao.insert(Matchers.anyListOf(TimeEntry.class))).thenReturn(Single.just(true));

        entityOptimizer = new EntityOptimizer.Builder()
                .timeEntriesDao(timeEntryDao)
                .projectsDao(projectDao)
                .issuesDao(issueDao)
                .userDao(userDao)
                .build();

        projects.add(project1);
        projects.add(project2);
    }

    /*
     *  Given   2 projects with 2 issues and 2x2 time entries
     *  When    EntityOptimizer accepts them
     *  Then    they will be processed
     *  And     a successful result will be sent to the listener (true)
     */
    @Test
    public void test_entityOptimizer_happy() {
        DbUpdateFinishedListener listener = (r,c)->Assert.assertTrue(r);
        entityOptimizer.addDbUpdateFinishedListener(listener);
        entityOptimizer.accept(projects);
    }

    /*
     *  Given   2 projects with 2 issues and 2x2 time entries
     *  When    EntityOptimizer accepts them
     *  Then    they will be processed
     *  And     an unsuccessful result will be sent to the listener (false)
     */
    @Test
    public void test_entityOptimizer_unsuccessfulInsert() {
        Mockito.when(projectDao.insert(Matchers.anyListOf(Project.class))).thenReturn(Single.just(false));
        DbUpdateFinishedListener listener = (r,c)->Assert.assertFalse(r);
        entityOptimizer.addDbUpdateFinishedListener(listener);
        entityOptimizer.accept(projects);
    }

    /*
     *  Given   2 projects with 2 issues and 2x2 time entries
     *  When    EntityOptimizer accepts them
     *  Then    they will be processed
     *  And     they will be sent to respective dao implementations for insertion
     */
    @Test
    public void test_entityOptimizer_successfulProcess() throws InterruptedException {
        userDao = new GenericDaoFake<>();
        projectDao = new GenericDaoFake<>();
        issueDao = new GenericDaoFake<>();
        timeEntryDao = new GenericDaoFake<>();

        ((GenericDaoFake<User>) userDao).booleanToReturn = true;
        ((GenericDaoFake<Project>) projectDao).booleanToReturn = true;
        ((GenericDaoFake<Issue>) issueDao).booleanToReturn = true;
        ((GenericDaoFake<TimeEntry>) timeEntryDao).booleanToReturn = true;

        entityOptimizer = new EntityOptimizer.Builder()
                .timeEntriesDao(timeEntryDao)
                .projectsDao(projectDao)
                .issuesDao(issueDao)
                .userDao(userDao)
                .build();

        entityOptimizer.accept(projects);

        Thread.sleep(napTime);

        List<User> users = ((GenericDaoFake<User>) userDao).list;
        List<Project> projects = ((GenericDaoFake<Project>) projectDao).list;
        List<Issue> issues = ((GenericDaoFake<Issue>) issueDao).list;
        List<TimeEntry> timeEntries = ((GenericDaoFake<TimeEntry>) timeEntryDao).list;

        assertEquals(4, users.size());
        assertEquals(2, projects.size());
        assertEquals(4, issues.size());
        assertEquals(8, timeEntries.size());

        assertTrue(users.contains(teamMember1));
        assertTrue(users.contains(teamMember2));
        assertTrue(users.contains(projectOwner1));
        assertTrue(users.contains(projectOwner2));

        assertTrue(projects.contains(project1));
        assertTrue(projects.contains(project2));

        assertTrue(issues.contains(issue1));
        assertTrue(issues.contains(issue2));
        assertTrue(issues.contains(issue3));
        assertTrue(issues.contains(issue4));

        assertTrue(timeEntries.contains(timeEntry1));
        assertTrue(timeEntries.contains(timeEntry2));
        assertTrue(timeEntries.contains(timeEntry3));
        assertTrue(timeEntries.contains(timeEntry4));
        assertTrue(timeEntries.contains(timeEntry5));
        assertTrue(timeEntries.contains(timeEntry6));
        assertTrue(timeEntries.contains(timeEntry7));
        assertTrue(timeEntries.contains(timeEntry8));
    }

    /*
     *  Given   a project list with no projects
     *  When    EntityOptimizer accepts the list
     *  Then    they will be processed
     *  And     the dao implementations will not receive anything to insert
     */
    @Test
    public void test_entityOptimizer_noProjects() {
        userDao = new GenericDaoFake<>();
        projectDao = new GenericDaoFake<>();
        issueDao = new GenericDaoFake<>();
        timeEntryDao = new GenericDaoFake<>();

        ((GenericDaoFake<User>) userDao).booleanToReturn = true;
        ((GenericDaoFake<Project>) projectDao).booleanToReturn = true;
        ((GenericDaoFake<Issue>) issueDao).booleanToReturn = true;
        ((GenericDaoFake<TimeEntry>) timeEntryDao).booleanToReturn = true;

        entityOptimizer = new EntityOptimizer.Builder()
                .timeEntriesDao(timeEntryDao)
                .projectsDao(projectDao)
                .issuesDao(issueDao)
                .userDao(userDao)
                .build();

        entityOptimizer.accept(new ArrayList<>());

        List<User> users = ((GenericDaoFake<User>) userDao).list;
        List<Project> projects = ((GenericDaoFake<Project>) projectDao).list;
        List<Issue> issues = ((GenericDaoFake<Issue>) issueDao).list;
        List<TimeEntry> timeEntries = ((GenericDaoFake<TimeEntry>) timeEntryDao).list;

        assertEquals(0, users.size());
        assertEquals(0, projects.size());
        assertEquals(0, issues.size());
        assertEquals(0, timeEntries.size());
    }

    /*
     *  Given   a project list with a project with no data
     *  When    EntityOptimizer accepts the list
     *  Then    they will be processed
     *  And     the dao implementations will not receive anything to insert
     */
    @Test
    public void test_entityOptimizer_badProjectData() {
        userDao = new GenericDaoFake<>();
        projectDao = new GenericDaoFake<>();
        issueDao = new GenericDaoFake<>();
        timeEntryDao = new GenericDaoFake<>();

        ((GenericDaoFake<User>) userDao).booleanToReturn = true;
        ((GenericDaoFake<Project>) projectDao).booleanToReturn = true;
        ((GenericDaoFake<Issue>) issueDao).booleanToReturn = true;
        ((GenericDaoFake<TimeEntry>) timeEntryDao).booleanToReturn = true;

        entityOptimizer = new EntityOptimizer.Builder()
                .timeEntriesDao(timeEntryDao)
                .projectsDao(projectDao)
                .issuesDao(issueDao)
                .userDao(userDao)
                .build();

        Project emptyProject = new Project();
        projects.clear();
        projects.add(emptyProject);
        entityOptimizer.accept(projects);

        List<User> users = ((GenericDaoFake<User>) userDao).list;
        List<Project> projects = ((GenericDaoFake<Project>) projectDao).list;
        List<Issue> issues = ((GenericDaoFake<Issue>) issueDao).list;
        List<TimeEntry> timeEntries = ((GenericDaoFake<TimeEntry>) timeEntryDao).list;

        assertEquals(0, users.size());
        assertEquals(0, projects.size());
        assertEquals(0, issues.size());
        assertEquals(0, timeEntries.size());
    }

    /*
     *  This scenario should not happen, but might, so the EntityOptimizer is prepared
     *
     *  Given   2 projects, 2 issues and 2x2 time entries
     *  And     the projects have issues in their respective list of issues
     *  And     the issues do not have projectId set
     *  And     the projects don't have ownerId set, but have respective owners
     *  And     the issues have time entries in their respective list of time entries
     *  And     the time entries do not have issueId set
     *  And     the time entries do not have userId set, but have users
     *  When    EntityOptimizer accepts them
     *  Then    they will be processed and sent to their respective dao implementations
     *              with the correct links made and correct parent ids
     *  And     a successful result will be sent to the listener (true)
     */
    @Test
    public void test_entityOptimizer_happy_linkedMetadata() throws InterruptedException {
        project1.setOwnerId(null);
        project2.setOwnerId(null);
        project1.setOwner(projectOwner1);
        project2.setOwner(projectOwner2);

        issue1.setOwnerId(null);
        issue2.setOwnerId(null);
        issue3.setOwnerId(null);
        issue4.setOwnerId(null);
        issue1.setOwner(teamMember1);
        issue2.setOwner(teamMember2);
        issue3.setOwner(teamMember1);
        issue4.setOwner(teamMember2);
        issue1.setProjectId(null);
        issue2.setProjectId(null);
        issue3.setProjectId(null);
        issue4.setProjectId(null);

        timeEntry1.setUserId(null);
        timeEntry2.setUserId(null);
        timeEntry3.setUserId(null);
        timeEntry4.setUserId(null);
        timeEntry5.setUserId(null);
        timeEntry6.setUserId(null);
        timeEntry7.setUserId(null);
        timeEntry8.setUserId(null);
        timeEntry1.setUser(teamMember1);
        timeEntry2.setUser(teamMember2);
        timeEntry3.setUser(teamMember1);
        timeEntry4.setUser(teamMember2);
        timeEntry5.setUser(teamMember1);
        timeEntry6.setUser(teamMember2);
        timeEntry7.setUser(teamMember1);
        timeEntry8.setUser(teamMember2);
        timeEntry1.setIssueId(null);
        timeEntry2.setIssueId(null);
        timeEntry3.setIssueId(null);
        timeEntry4.setIssueId(null);
        timeEntry5.setIssueId(null);
        timeEntry6.setIssueId(null);
        timeEntry7.setIssueId(null);
        timeEntry8.setIssueId(null);

        userDao = new GenericDaoFake<>();
        projectDao = new GenericDaoFake<>();
        issueDao = new GenericDaoFake<>();
        timeEntryDao = new GenericDaoFake<>();

        ((GenericDaoFake<User>) userDao).booleanToReturn = true;
        ((GenericDaoFake<Project>) projectDao).booleanToReturn = true;
        ((GenericDaoFake<Issue>) issueDao).booleanToReturn = true;
        ((GenericDaoFake<TimeEntry>) timeEntryDao).booleanToReturn = true;

        entityOptimizer = new EntityOptimizer.Builder()
                .timeEntriesDao(timeEntryDao)
                .projectsDao(projectDao)
                .issuesDao(issueDao)
                .userDao(userDao)
                .build();

        DbUpdateFinishedListener listener = (r,c)->Assert.assertTrue(r);
        entityOptimizer.addDbUpdateFinishedListener(listener);
        entityOptimizer.accept(projects);

        Thread.sleep(napTime);

        List<User> users = ((GenericDaoFake<User>) userDao).list;
        List<Project> projects = ((GenericDaoFake<Project>) projectDao).list;
        List<Issue> issues = ((GenericDaoFake<Issue>) issueDao).list;
        List<TimeEntry> timeEntries = ((GenericDaoFake<TimeEntry>) timeEntryDao).list;

        assertEquals(4, users.size());
        assertEquals(2, projects.size());
        assertEquals(4, issues.size());
        assertEquals(8, timeEntries.size());

        assertTrue(users.contains(teamMember1));
        assertTrue(users.contains(teamMember2));
        assertTrue(users.contains(projectOwner1));
        assertTrue(users.contains(projectOwner2));

        assertTrue(projects.contains(project1));
        assertTrue(projects.contains(project2));

        assertTrue(issues.contains(issue1));
        assertTrue(issues.contains(issue2));
        assertTrue(issues.contains(issue3));
        assertTrue(issues.contains(issue4));

        assertTrue(timeEntries.contains(timeEntry1));
        assertTrue(timeEntries.contains(timeEntry2));
        assertTrue(timeEntries.contains(timeEntry3));
        assertTrue(timeEntries.contains(timeEntry4));
        assertTrue(timeEntries.contains(timeEntry5));
        assertTrue(timeEntries.contains(timeEntry6));
        assertTrue(timeEntries.contains(timeEntry7));
        assertTrue(timeEntries.contains(timeEntry8));

        Project p1, p2;
        if (projects.get(0).getId().equals(project1.getId())) {
            p1 = projects.get(0);
            p2 = projects.get(1);
        } else {
            p1 = projects.get(1);
            p2 = projects.get(0);
        }

        assertTrue(p1.getIssues().contains(issue1));
        assertTrue(p1.getIssues().contains(issue2));
        assertTrue(p2.getIssues().contains(issue3));
        assertTrue(p2.getIssues().contains(issue4));

        Issue i1, i2, i3, i4;
        if (p1.getIssues().get(0).getId().equals(issue1.getId())) {
            i1 = p1.getIssues().get(0);
            i2 = p1.getIssues().get(1);
        } else {
            i1 = p1.getIssues().get(0);
            i2 = p1.getIssues().get(1);
        }

        if (p2.getIssues().get(0).getId().equals(issue3.getId())) {
            i3 = p2.getIssues().get(0);
            i4 = p2.getIssues().get(1);
        } else {
            i3 = p2.getIssues().get(0);
            i4 = p2.getIssues().get(1);
        }

        assertTrue(i1.getTimeEntries().contains(timeEntry1));
        assertTrue(i1.getTimeEntries().contains(timeEntry2));
        assertTrue(i2.getTimeEntries().contains(timeEntry3));
        assertTrue(i2.getTimeEntries().contains(timeEntry4));

        assertTrue(i3.getTimeEntries().contains(timeEntry5));
        assertTrue(i3.getTimeEntries().contains(timeEntry6));
        assertTrue(i4.getTimeEntries().contains(timeEntry7));
        assertTrue(i4.getTimeEntries().contains(timeEntry8));

        assertEquals(teamMember1.getId(), timeEntry1.getUserId());
        assertEquals(teamMember2.getId(), timeEntry2.getUserId());
        assertEquals(teamMember1.getId(), timeEntry3.getUserId());
        assertEquals(teamMember2.getId(), timeEntry4.getUserId());
        assertEquals(teamMember1.getId(), timeEntry5.getUserId());
        assertEquals(teamMember2.getId(), timeEntry6.getUserId());
        assertEquals(teamMember1.getId(), timeEntry7.getUserId());
        assertEquals(teamMember2.getId(), timeEntry8.getUserId());

        assertEquals(issue1.getId(), timeEntry1.getIssueId());
        assertEquals(issue1.getId(), timeEntry2.getIssueId());
        assertEquals(issue2.getId(), timeEntry3.getIssueId());
        assertEquals(issue2.getId(), timeEntry4.getIssueId());
        assertEquals(issue3.getId(), timeEntry5.getIssueId());
        assertEquals(issue3.getId(), timeEntry6.getIssueId());
        assertEquals(issue4.getId(), timeEntry7.getIssueId());
        assertEquals(issue4.getId(), timeEntry8.getIssueId());

        assertEquals(project1.getId(), i1.getProjectId());
        assertEquals(project1.getId(), i2.getProjectId());
        assertEquals(project2.getId(), i3.getProjectId());
        assertEquals(project2.getId(), i4.getProjectId());

        assertEquals(teamMember1.getId(), i1.getOwnerId());
        assertEquals(teamMember2.getId(), i2.getOwnerId());
        assertEquals(teamMember1.getId(), i3.getOwnerId());
        assertEquals(teamMember2.getId(), i4.getOwnerId());
    }

    /*
     *  This scenario should not happen, but might, so the EntityOptimizer is prepared
     *
     *  Given   2 projects, 2 issues and 2x2 time entries
     *  And     the projects have issues in their respective list of issues
     *  And     the issues do not have projectId set
     *  And     the projects don't have ownerId set, but have respective owners
     *  And     the issues have time entries in their respective list of time entries
     *  And     the issues have users THAT ARE NOT in the project team
     *  And     the time entries do not have issueId set
     *  And     the time entries do not have userId set, but have users THAT ARE NOT in
     *              the project team
     *  When    EntityOptimizer accepts them
     *  Then    they will be processed and sent to their respective dao implementations
     *              with the correct links made and correct parent ids
     *  And     the ghost users will be inserted in the database, but not as team members
     *  And     a successful result will be sent to the listener (true)
     */
    @Test
    public void test_entityOptimizer_otherUsers() throws InterruptedException {

        User ghostUser = new User();
        ghostUser.setId(22L);
        ghostUser.setName("McGhostin");
        ghostUser.setPhoto("");
        ghostUser.setEmail("ghost.user.1@emailindustry.net");
        ghostUser.setPwd("decent_try");
        ghostUser.setRole(1);
        ghostUser.setApiKey("A_P_I___K_E_Y");
        ghostUser.setActive(1);

        User ghostUser2 = new User();
        ghostUser2.setId(23L);
        ghostUser2.setName("Chris T. Maspast");
        ghostUser2.setPhoto("");
        ghostUser2.setEmail("ghost.user.2@emailindustry.net");
        ghostUser2.setPwd("not_even_trying");
        ghostUser2.setRole(1);
        ghostUser2.setApiKey("A_P_I___K_E_Y");
        ghostUser2.setActive(1);

        project1.setOwnerId(null);
        project2.setOwnerId(null);
        project1.setOwner(projectOwner1);
        project2.setOwner(projectOwner2);

        issue1.setOwnerId(null);
        issue2.setOwnerId(null);
        issue3.setOwnerId(null);
        issue4.setOwnerId(null);
        issue1.setOwner(ghostUser);
        issue2.setOwner(teamMember2);
        issue3.setOwner(teamMember1);
        issue4.setOwner(teamMember2);
        issue1.setProjectId(null);
        issue2.setProjectId(null);
        issue3.setProjectId(null);
        issue4.setProjectId(null);

        timeEntry1.setUserId(null);
        timeEntry2.setUserId(null);
        timeEntry3.setUserId(null);
        timeEntry4.setUserId(null);
        timeEntry5.setUserId(null);
        timeEntry6.setUserId(null);
        timeEntry7.setUserId(null);
        timeEntry8.setUserId(null);
        timeEntry1.setUser(teamMember1);
        timeEntry2.setUser(teamMember2);
        timeEntry3.setUser(teamMember1);
        timeEntry4.setUser(teamMember2);
        timeEntry5.setUser(teamMember1);
        timeEntry6.setUser(teamMember2);
        timeEntry7.setUser(teamMember1);
        timeEntry8.setUser(ghostUser2);
        timeEntry1.setIssueId(null);
        timeEntry2.setIssueId(null);
        timeEntry3.setIssueId(null);
        timeEntry4.setIssueId(null);
        timeEntry5.setIssueId(null);
        timeEntry6.setIssueId(null);
        timeEntry7.setIssueId(null);
        timeEntry8.setIssueId(null);

        userDao = new GenericDaoFake<>();
        projectDao = new GenericDaoFake<>();
        issueDao = new GenericDaoFake<>();
        timeEntryDao = new GenericDaoFake<>();

        ((GenericDaoFake<User>) userDao).booleanToReturn = true;
        ((GenericDaoFake<Project>) projectDao).booleanToReturn = true;
        ((GenericDaoFake<Issue>) issueDao).booleanToReturn = true;
        ((GenericDaoFake<TimeEntry>) timeEntryDao).booleanToReturn = true;

        entityOptimizer = new EntityOptimizer.Builder()
                .timeEntriesDao(timeEntryDao)
                .projectsDao(projectDao)
                .issuesDao(issueDao)
                .userDao(userDao)
                .build();

        DbUpdateFinishedListener listener = (r,c)->Assert.assertTrue(r);
        entityOptimizer.addDbUpdateFinishedListener(listener);
        entityOptimizer.accept(projects);

        Thread.sleep(napTime);

        List<User> users = ((GenericDaoFake<User>) userDao).list;
        List<Project> projects = ((GenericDaoFake<Project>) projectDao).list;
        List<Issue> issues = ((GenericDaoFake<Issue>) issueDao).list;
        List<TimeEntry> timeEntries = ((GenericDaoFake<TimeEntry>) timeEntryDao).list;

        assertEquals(6, users.size());
        assertEquals(2, projects.size());
        assertEquals(4, issues.size());
        assertEquals(8, timeEntries.size());

        assertTrue(users.contains(teamMember1));
        assertTrue(users.contains(teamMember2));
        assertTrue(users.contains(projectOwner1));
        assertTrue(users.contains(projectOwner2));

        assertTrue(projects.contains(project1));
        assertTrue(projects.contains(project2));

        assertTrue(issues.contains(issue1));
        assertTrue(issues.contains(issue2));
        assertTrue(issues.contains(issue3));
        assertTrue(issues.contains(issue4));

        assertTrue(timeEntries.contains(timeEntry1));
        assertTrue(timeEntries.contains(timeEntry2));
        assertTrue(timeEntries.contains(timeEntry3));
        assertTrue(timeEntries.contains(timeEntry4));
        assertTrue(timeEntries.contains(timeEntry5));
        assertTrue(timeEntries.contains(timeEntry6));
        assertTrue(timeEntries.contains(timeEntry7));
        assertTrue(timeEntries.contains(timeEntry8));

        Project p1, p2;
        if (projects.get(0).getId().equals(project1.getId())) {
            p1 = projects.get(0);
            p2 = projects.get(1);
        } else {
            p1 = projects.get(1);
            p2 = projects.get(0);
        }

        assertTrue(p1.getIssues().contains(issue1));
        assertTrue(p1.getIssues().contains(issue2));
        assertTrue(p2.getIssues().contains(issue3));
        assertTrue(p2.getIssues().contains(issue4));

        Issue i1, i2, i3, i4;
        if (p1.getIssues().get(0).getId().equals(issue1.getId())) {
            i1 = p1.getIssues().get(0);
            i2 = p1.getIssues().get(1);
        } else {
            i1 = p1.getIssues().get(0);
            i2 = p1.getIssues().get(1);
        }

        if (p2.getIssues().get(0).getId().equals(issue3.getId())) {
            i3 = p2.getIssues().get(0);
            i4 = p2.getIssues().get(1);
        } else {
            i3 = p2.getIssues().get(0);
            i4 = p2.getIssues().get(1);
        }

        assertTrue(i1.getTimeEntries().contains(timeEntry1));
        assertTrue(i1.getTimeEntries().contains(timeEntry2));
        assertTrue(i2.getTimeEntries().contains(timeEntry3));
        assertTrue(i2.getTimeEntries().contains(timeEntry4));

        assertTrue(i3.getTimeEntries().contains(timeEntry5));
        assertTrue(i3.getTimeEntries().contains(timeEntry6));
        assertTrue(i4.getTimeEntries().contains(timeEntry7));
        assertTrue(i4.getTimeEntries().contains(timeEntry8));

        assertEquals(teamMember1.getId(), timeEntry1.getUserId());
        assertEquals(teamMember2.getId(), timeEntry2.getUserId());
        assertEquals(teamMember1.getId(), timeEntry3.getUserId());
        assertEquals(teamMember2.getId(), timeEntry4.getUserId());
        assertEquals(teamMember1.getId(), timeEntry5.getUserId());
        assertEquals(teamMember2.getId(), timeEntry6.getUserId());
        assertEquals(teamMember1.getId(), timeEntry7.getUserId());
        assertEquals(ghostUser2.getId(), timeEntry8.getUserId());

        assertEquals(issue1.getId(), timeEntry1.getIssueId());
        assertEquals(issue1.getId(), timeEntry2.getIssueId());
        assertEquals(issue2.getId(), timeEntry3.getIssueId());
        assertEquals(issue2.getId(), timeEntry4.getIssueId());
        assertEquals(issue3.getId(), timeEntry5.getIssueId());
        assertEquals(issue3.getId(), timeEntry6.getIssueId());
        assertEquals(issue4.getId(), timeEntry7.getIssueId());
        assertEquals(issue4.getId(), timeEntry8.getIssueId());

        assertEquals(project1.getId(), i1.getProjectId());
        assertEquals(project1.getId(), i2.getProjectId());
        assertEquals(project2.getId(), i3.getProjectId());
        assertEquals(project2.getId(), i4.getProjectId());

        assertEquals(ghostUser.getId(), i1.getOwnerId());
        assertEquals(teamMember2.getId(), i2.getOwnerId());
        assertEquals(teamMember1.getId(), i3.getOwnerId());
        assertEquals(teamMember2.getId(), i4.getOwnerId());
    }
}