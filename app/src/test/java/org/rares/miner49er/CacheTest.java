package org.rares.miner49er;

import org.joda.time.DateTimeZone;
import org.joda.time.tz.UTCProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rares.miner49er.cache.VMCache;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CacheTest {

    private VMCache cache = VMCache.INSTANCE;

    @Test
    public void testCacheUpdateTimeEntry() {
//        project:       2
//        issue:        10
//        time entry:   43

        List<ProjectData> projectDataList = cache.getCachedProjects();

        String before = logData(projectDataList);

        List<TimeEntryData> tedList = cache.getIssueTimeEntriesData(10L);

        assertFalse(tedList.isEmpty());

        TimeEntryData updatedTimeEntryData = tedList.get(tedList.size() - 1);

        updatedTimeEntryData.setHours(9);

        cache.updateTimeEntryData(updatedTimeEntryData);

        projectDataList = cache.getCachedProjects();

        String after = logData(projectDataList);

        assertNotEquals(before, after);


        ProjectData testProjectDataList = cache.getProjectData(2L);
        List<IssueData> testIssueDataList = testProjectDataList.getIssues();

        assertFalse(testIssueDataList.isEmpty());
        assertEquals(testIssueDataList.size(), 4);

        List<TimeEntryData> testTimeEntryDataList = testIssueDataList.get(2).getTimeEntries();

        assertFalse(testTimeEntryDataList.isEmpty());
        assertEquals(testTimeEntryDataList.size(), 4);

        TimeEntryData teData = tedList.get(3);

        assertEquals(updatedTimeEntryData.id, teData.id);
        assertEquals(updatedTimeEntryData.getHours(), teData.getHours());


        testIssueDataList = cache.getProjectIssuesData(2L).get();

        assertNotNull(testIssueDataList);

        for (IssueData idata : testIssueDataList) {
            if (idata.id.equals(updatedTimeEntryData.getIssueId())) {
                testTimeEntryDataList = idata.getTimeEntries();
                assertNotNull(testTimeEntryDataList);
                assertTrue(testTimeEntryDataList.contains(updatedTimeEntryData));
                break;
            }
        }


        testTimeEntryDataList = cache.getIssueTimeEntriesData(updatedTimeEntryData.getIssueId());

        assertNotNull(testTimeEntryDataList);
        assertFalse(testTimeEntryDataList.isEmpty());
        assertTrue(testTimeEntryDataList.contains(updatedTimeEntryData));


        TimeEntryData lruTeData = cache.getLruTimeEntryData(43L);

        assertNotNull(lruTeData);
        assertEquals(9, lruTeData.getHours());
        assertEquals(updatedTimeEntryData.id, lruTeData.id);

    }

    @Test
    public void testCacheUpdateIssueData() {
        // issue:   3
        // old name: "3_IssueName"
        // new name: "renamed issue data"

        String newName = "renamed issue data";

        assertNotNull(cache);
        assertNotNull(cache.getCachedProjects());
        assertNotNull(cache.getProjectIssuesData(0L));

        String before = logData(cache.getCachedProjects());

        List<IssueData> issueDataList = cache.getProjectIssuesData(0L).get();

        assertNotNull(issueDataList);
        assertFalse(issueDataList.isEmpty());
        assertTrue(issueDataList.size() > 3);

        IssueData updatedIssueData = issueDataList.get(3);
        updatedIssueData.setName(newName);

        cache.updateIssueData(updatedIssueData);

        String after = logData(cache.getCachedProjects());

        assertNotEquals(before, after);


        List<ProjectData> cachedProjects = cache.getCachedProjects();

        assertNotNull(cachedProjects);
        assertFalse(cachedProjects.isEmpty());

        List<IssueData> projectsIssues = cachedProjects.get(0).getIssues();

        assertNotNull(projectsIssues);
        assertFalse(projectsIssues.isEmpty());

        IssueData actualIssueData = projectsIssues.get(3);

        assertEquals(updatedIssueData, actualIssueData);


        issueDataList = cache.getProjectIssuesData(0L).get();

        assertNotNull(issueDataList);
        assertFalse(issueDataList.isEmpty());
        assertTrue(issueDataList.size() > 3);

        actualIssueData = issueDataList.get(3);

        assertEquals(updatedIssueData, actualIssueData);


        assertNotNull(cache.getLruIssueData(3L).get());

        assertEquals(updatedIssueData, cache.getLruIssueData(3L).get());
    }

    @Test
    public void testCacheUpdateProjectData() {
        assertNotNull(cache);
        assertNotNull(cache.getCachedProjects());
        assertTrue(cache.getCachedProjects().size() > 1);


        ProjectData oldProjectData = cache.getCachedProjects().get(1);
        ProjectData clonedProjectData = new ProjectData();

        clonedProjectData.id = oldProjectData.id;
        clonedProjectData.lastUpdated = oldProjectData.lastUpdated;

        clonedProjectData.updateData(oldProjectData);

        clonedProjectData.setName("new project name");

        cache.updateProjectData(clonedProjectData);

        List<ProjectData> projectDataList = cache.getCachedProjects();

        assertFalse(projectDataList.contains(oldProjectData));
        assertTrue(projectDataList.contains(clonedProjectData));
    }

    @Before
    public void setup() {
        DateTimeZone.setProvider(new UTCProvider());
        Collection<ProjectData> preparedData = prepareCacheData().values();
        ArrayList<ProjectData> cachedData = new ArrayList<>(preparedData);
        cache.updateCachedProjects(cachedData);
    }

    @After
    public void tearDown(){
        cache.clear();
    }

    private Map<Long, ProjectData> prepareCacheData() {

        Map<Long, ProjectData> projectDataMap = new HashMap<>();
        Map<Long, IssueData> issueDataMap = new HashMap<>();
        Map<Long, TimeEntryData> timeEntryDataMap = new HashMap<>();
        Map<Long, UserData> userDataMap = new HashMap<>();

        for (int i = 0; i < 4; i++) {
            ProjectData projectData = createProjectData(projectDataMap.size());

            UserData owner = createUserData(userDataMap.size());
            userDataMap.put(owner.id, owner);
            projectData.setOwner(owner);
            List<UserData> team = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                UserData userData = createUserData(userDataMap.size());
                userData.setRole(1);
                team.add(userData);
                userDataMap.put(userData.id, userData);
            }
            projectData.setTeam(team);

            List<IssueData> issueDataList = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                IssueData issueData = createIssueData(issueDataMap.size());
                issueData.setProjectId(projectData.getId());
                List<TimeEntryData> timeEntryDataList = new ArrayList<>();
                for (int k = 0; k < 4; k++) {
                    TimeEntryData timeEntryData = createTimeEntryData(timeEntryDataMap.size());
                    timeEntryData.setIssueId(issueData.getId());

                    UserData userData = projectData.getTeam().get(k % 2);
                    timeEntryData.setUserId(userData.getId());
                    timeEntryData.setUserName(userData.getName());
                    timeEntryData.setUserPhoto(userData.getPicture());

                    timeEntryDataList.add(timeEntryData);
                    timeEntryDataMap.put(timeEntryData.id, timeEntryData);
                }
                issueData.setTimeEntries(timeEntryDataList);
                issueDataList.add(issueData);
                issueDataMap.put(issueData.id, issueData);
            }
            projectData.setIssues(issueDataList);

            projectDataMap.put(projectData.id, projectData);
        }

        return projectDataMap;
    }

    private ProjectData createProjectData(long i) {
        ProjectData projectData = new ProjectData();
        projectData.setId(i);
        projectData.setLastUpdated(System.currentTimeMillis());
        projectData.setPicture(String.valueOf(i) + "_ProjectPicture");
        projectData.setIcon(String.valueOf(i) + "_ProjectIcon");
        projectData.setName(String.valueOf(i) + "_ProjectName");
        projectData.setDescription(String.valueOf(i) + "_ProjectDescription");

        return projectData;
    }

    private UserData createUserData(long i) {
        UserData userData = new UserData();
        userData.setId(i);
        userData.setLastUpdated(System.currentTimeMillis());
        userData.setName(String.valueOf(i) + "_UserName");
        userData.setEmail(String.valueOf(i) + "_UserEmail");
        userData.setPicture(String.valueOf(i) + "_UserPicture");
        userData.setApiKey(String.valueOf(i) + "_UserApiKey");

        return userData;
    }

    private IssueData createIssueData(long i) {
        IssueData issueData = new IssueData();
        issueData.setId(i);
        issueData.setLastUpdated(System.currentTimeMillis());
        issueData.setName(String.valueOf(i) + "_IssueName");
        issueData.setDateAdded(System.currentTimeMillis() - 10);

        return issueData;
    }

    private TimeEntryData createTimeEntryData(long i) {
        TimeEntryData timeEntryData = new TimeEntryData();
        timeEntryData.setId(i);
        timeEntryData.setLastUpdated(System.currentTimeMillis());
        timeEntryData.setWorkDate(System.currentTimeMillis());

        timeEntryData.setHours(8);
        timeEntryData.setComments(String.valueOf(i) + "_TimeEntryDataComments");

        return timeEntryData;
    }

    private String logData(List<ProjectData> projectDataList) {
        StringBuilder logBuilder = new StringBuilder();
        for (ProjectData pd : projectDataList) {
            logBuilder.append("\n");
            logBuilder.append("-");
            logBuilder.append(pd.toString());
            List<IssueData> issueDataList = pd.getIssues();
            if (issueDataList != null) {
                for (IssueData id : issueDataList) {
                    logBuilder.append("\n");
                    logBuilder.append("--");
                    logBuilder.append(id.toString());
                    List<TimeEntryData> timeEntryDataList = id.getTimeEntries();
                    if (timeEntryDataList != null) {
                        for (TimeEntryData ted : timeEntryDataList) {
                            logBuilder.append("\n");
                            logBuilder.append("---");
                            logBuilder.append(ted.toLongString());
                        }
                    }
                }
            }
        }
        return logBuilder.toString();
    }

}
