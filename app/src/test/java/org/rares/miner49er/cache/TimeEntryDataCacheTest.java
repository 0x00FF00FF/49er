package org.rares.miner49er.cache;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TimeEntryDataCacheTest {
    private ViewModelCache cache = ViewModelCache.getInstance();
    //    private Cache<TimeEntryData> tc = cache.getCache(TimeEntryData.class);
    private TimeEntryDataCache tc = (TimeEntryDataCache) cache.getCache(TimeEntryData.class);
    private Cache<IssueData> ic = cache.getCache(IssueData.class);
    private Cache<ProjectData> pc = cache.getCache(ProjectData.class);

    @Before
    public void setUp() {
        cache.clear();
    }

    /*
     *  Given   a list of TimeEntryData
     *  When    the TimeEntryDataCache#putData(List, Predicate, boolean)}
     *              method is called
     *  Then    it has no effect on the cache
     */
    @Test
    public void testPutDataPredicate() {
        ProjectData projectData = new ProjectData();
        projectData.setId(1L);
        IssueData issueData = new IssueData();
        issueData.parentId = 1L;
        issueData.id = 1L;
        List<TimeEntryData> entries = new ArrayList<>();
        TimeEntryData timEntryData = new TimeEntryData();
        timEntryData.id = 1L;
        entries.add(timEntryData);

        assertEquals(0, pc.getSize());
        assertEquals(0, ic.getSize());
        assertEquals(0, tc.getSize());
        pc.putData(projectData, true);
        ic.putData(issueData, true);
        tc.putData(entries, p -> true, false);
        assertEquals(1, pc.getSize());
        assertEquals(1, ic.getSize());
        assertEquals(0, tc.getSize());
    }

    /*
     *  Given   a list of TimeEntryData
     *  When    the TimeEntryDataCache#putData(List, false) method is called
     *  Then    the cache will contain the time entry and issue data in the list
     *  And     the time entry data is not linked with its parent defined by parentId
     */
    @Test
    public void testPutDataList_noLink() {
        ProjectData projectData = new ProjectData();
        projectData.id = 1L;
        IssueData issueData = new IssueData();
        issueData.setId(1L);
        issueData.parentId = projectData.id;
        List<TimeEntryData> entries = new ArrayList<>();
        TimeEntryData timeEntryData = new TimeEntryData();
        timeEntryData.id = 1L;
        timeEntryData.parentId = issueData.id;
        TimeEntryData timeEntryData1 = new TimeEntryData();
        timeEntryData1.updateData(timeEntryData);
        timeEntryData1.id = 2L;
        entries.add(timeEntryData);
        entries.add(timeEntryData1);

        assertEquals(0, pc.getSize());
        assertEquals(0, ic.getSize());
        assertEquals(0, tc.getSize());

        pc.putData(projectData, true);
        ic.putData(issueData, true);

        assertTrue(pc.getData(Optional.empty()).contains(projectData));
        assertTrue(ic.getData(Optional.empty()).contains(issueData));

        tc.putData(entries, false);

        assertEquals(2, tc.getSize());

        assertEquals(timeEntryData.id, tc.getData(timeEntryData.id).id);
        assertEquals(timeEntryData1.id, tc.getData(timeEntryData1.id).id);

        assertNull(ic.getData(issueData.id).getTimeEntries());
    }

    /*
     *  Given   IssueData already present in the cache
     *  When    the TimeEntryDataCache#putData(List, true) method is called
     *  Then    the cache will contain the time entry data in the list
     *  And     the time entry data is linked with its parent defined by parentId
     */
    @Test
    public void testPutDataList_link() {
        ProjectData projectData = new ProjectData();
        projectData.id = 1L;
        IssueData issueData = new IssueData();
        issueData.setId(1L);
        issueData.parentId = projectData.id;
        List<TimeEntryData> timeEntries = new ArrayList<>();
        TimeEntryData timeEntryData = new TimeEntryData();
        timeEntryData.id = 1L;
        timeEntryData.parentId = issueData.id;
        TimeEntryData timeEntryData1 = new TimeEntryData();
        timeEntryData1.updateData(timeEntryData);
        timeEntryData1.id = 2L;
        timeEntries.add(timeEntryData);
        timeEntries.add(timeEntryData1);

        assertEquals(0, pc.getSize());
        assertEquals(0, ic.getSize());
        assertEquals(0, tc.getSize());

        pc.putData(projectData, true);
        ic.putData(issueData, true);

        assertTrue(ic.getData(Optional.empty()).contains(issueData));

        tc.putData(timeEntries, true);

        assertEquals(2, tc.getSize());

        assertEquals(timeEntryData.id, tc.getData(timeEntryData.id).id);
        assertEquals(timeEntryData1.id, tc.getData(timeEntryData1.id).id);

        assertNotNull(ic.getData(issueData.id).getTimeEntries());
        assertEquals(2, ic.getData(issueData.id).getTimeEntries().size());

        assertTrue(ic.getData(issueData.id).getTimeEntries().contains(timeEntryData));
        assertTrue(ic.getData(issueData.id).getTimeEntries().contains(timeEntryData1));
    }

    /*
     *  Given   IssueData and TimeEntryData already present in the cache
     *  When    the TimeEntryDataCache#putData(List, true) method is called
     *  And     the list contains updated data (time entry comments)
     *  Then    the cache will contain the new time entry data in the list
     *  And     the time entry data is linked with its parent defined by parentId
     */
    @Test
    public void testPutDataList_linkUpdateData() {
        String initial = "initial comments";
        String modified = "modified comments";
        ProjectData projectData = new ProjectData();
        projectData.id = 1L;

        IssueData issueData = new IssueData();
        issueData.setId(1L);
        issueData.parentId = projectData.id;

        TimeEntryData timeEntryData = new TimeEntryData();
        timeEntryData.id = 1L;
        timeEntryData.setComments(initial);
        timeEntryData.parentId = issueData.id;

        TimeEntryData timeEntryData1 = new TimeEntryData();
        timeEntryData1.updateData(timeEntryData);
        timeEntryData1.id = 1L;
        timeEntryData1.setComments(modified);

        assertEquals(0, tc.getSize());
        assertEquals(0, ic.getSize());
        assertEquals(0, pc.getSize());

        pc.putData(projectData, false);
        ic.putData(issueData, true);
        tc.putData(timeEntryData, true);

        assertTrue(ic.getData(Optional.empty()).contains(issueData));
        assertTrue(ic.getData(issueData.id).getTimeEntries().contains(timeEntryData));
        assertEquals(1, tc.getSize());
        assertEquals(initial, ic.getData(issueData.id).getTimeEntries().get(0).getComments());

        tc.putData(timeEntryData1, true);

        assertEquals(1, tc.getSize());
        assertEquals(timeEntryData.id, tc.getData(timeEntryData.id).id);
        assertEquals(modified, tc.getData(timeEntryData.id).getComments());

        assertNotNull(ic.getData(issueData.id).getTimeEntries());
        assertEquals(1, ic.getData(issueData.id).getTimeEntries().size());

        assertTrue(ic.getData(issueData.id).getTimeEntries().contains(timeEntryData));
        assertEquals(modified, ic.getData(issueData.id).getTimeEntries().get(0).getComments());
    }

    /*
     *  Given   IssueData, TimeEntryData already present in the cache
     *  And     IssueData initially has Collections.emptyList as entries
     *  When    the TimeEntryDataCache#putData(list, true) method is called
     *  And     the list contains new data
     *  Then    the cache will also contain the new time entry data in the list
     *  And     the time entry data is linked with its parent defined by parentId
     */
    @Test
    public void testPutDataList_linkAddData() {
        ProjectData projectData = new ProjectData();
        projectData.id = 1L;

        IssueData issueData = new IssueData();
        issueData.setId(1L);
        issueData.parentId = projectData.id;
        issueData.setTimeEntries(Collections.emptyList());

        TimeEntryData timeEntryData = new TimeEntryData();
        timeEntryData.id = 1L;
        timeEntryData.parentId = issueData.id;

        TimeEntryData timeEntryData1 = new TimeEntryData();
        timeEntryData1.updateData(timeEntryData);
        timeEntryData1.id = 2L;

        assertEquals(0, pc.getSize());
        assertEquals(0, ic.getSize());
        assertEquals(0, tc.getSize());

        pc.putData(projectData, false);
        ic.putData(issueData, true);
        tc.putData(timeEntryData, true);

        assertTrue(ic.getData(Optional.empty()).contains(issueData));
        assertTrue(ic.getData(issueData.id).getTimeEntries().contains(timeEntryData));
        assertEquals(1, tc.getSize());
        assertEquals(1, ic.getData(issueData.id).getTimeEntries().size());

        tc.putData(timeEntryData1, true);

        assertEquals(2, tc.getSize());

        assertNotNull(ic.getData(issueData.id).getTimeEntries());
        assertEquals(2, ic.getData(issueData.id).getTimeEntries().size());

        assertTrue(ic.getData(issueData.id).getTimeEntries().contains(timeEntryData));
        assertTrue(ic.getData(issueData.id).getTimeEntries().contains(timeEntryData1));
    }

    /*
     *  Given   a time entry data in the cache
     *  When    removeData is called
     *  Then    the time entry data will not be in the cache anymore
     */
    @Test
    public void testRemoveData() {
        ProjectData projectData = new ProjectData();
        projectData.id = 1L;
        IssueData issueData = new IssueData();
        issueData.setId(1L);
        issueData.parentId = projectData.id;
        TimeEntryData timeEntryData = new TimeEntryData();
        timeEntryData.id = 1L;
        timeEntryData.parentId = issueData.id;

        TimeEntryData timeEntryData1 = new TimeEntryData();
        timeEntryData1.updateData(timeEntryData);
        timeEntryData1.id = 1L;

        assertEquals(0, pc.getSize());
        assertEquals(0, ic.getSize());
        assertEquals(0, tc.getSize());

        pc.putData(projectData, false);
        ic.putData(issueData, true);
        tc.putData(timeEntryData, true);

        assertTrue(ic.getData(Optional.empty()).contains(issueData));
        assertTrue(ic.getData(issueData.id).getTimeEntries().contains(timeEntryData));
        assertEquals(1, tc.getSize());
        assertEquals(1, ic.getData(issueData.id).getTimeEntries().size());

        tc.removeData(timeEntryData1);

        assertEquals(0, tc.getSize());
        assertEquals(0, ic.getData(issueData.id).getTimeEntries().size());
    }

    /*
     *  Given   two time entry data in the cache
     *  When    getData is called with no parent id
     *  Then    all time entry data will be returned
     */
    @Test
    public void testGetAllData() {
        ProjectData projectData = new ProjectData();
        projectData.id = 1L;
        IssueData issueData = new IssueData();
        issueData.setId(1L);
        issueData.parentId = projectData.id;
        List<TimeEntryData> timeEntries = new ArrayList<>();
        TimeEntryData timeEntryData = new TimeEntryData();
        timeEntryData.id = 1L;
        timeEntryData.parentId = issueData.id;
        TimeEntryData timeEntryData1 = new TimeEntryData();
        timeEntryData1.updateData(timeEntryData);
        timeEntryData1.id = 2L;
        timeEntries.add(timeEntryData);
        timeEntries.add(timeEntryData1);

        assertEquals(0, pc.getSize());
        assertEquals(0, ic.getSize());
        assertEquals(0, tc.getSize());

        pc.putData(projectData, false);
        ic.putData(issueData, true);

        assertTrue(ic.getData(Optional.empty()).contains(issueData));

        tc.putData(timeEntries, false);

        assertEquals(2, tc.getSize());
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData));
        assertTrue(tc.getData(Optional.empty()).contains(timeEntryData1));

        assertEquals(timeEntryData.id, tc.getData(timeEntryData.id).id);
        assertEquals(timeEntryData1.id, tc.getData(timeEntryData1.id).id);
    }

    /*
     *  Given   two timeEntry data in the cache, one linked, one not
     *  When    getData is called with parent id
     *  Then    only the time entry data for that parent id is returned
     */
    @Test
    public void testGetProjectIssueData() {
        ProjectData projectData = new ProjectData();
        projectData.id = 1L;
        IssueData issueData = new IssueData();
        issueData.setId(1L);
        issueData.parentId = projectData.id;
        List<TimeEntryData> entries = new ArrayList<>();
        TimeEntryData timeEntryData = new TimeEntryData();
        timeEntryData.id = 1L;
        timeEntryData.parentId = issueData.id;
        TimeEntryData timeEntryData1 = new TimeEntryData();
        timeEntryData1.updateData(timeEntryData);
        timeEntryData1.id = 2L;
        timeEntryData1.parentId = 2L;
        entries.add(timeEntryData);
        entries.add(timeEntryData1);

        assertEquals(0, pc.getSize());
        assertEquals(0, ic.getSize());
        assertEquals(0, tc.getSize());

        pc.putData(projectData, false);
        ic.putData(issueData, true);

        assertTrue(ic.getData(Optional.empty()).contains(issueData));

        tc.putData(entries, true);

        assertEquals(2, tc.getSize());
        assertEquals(1, ic.getData(issueData.id).getTimeEntries().size());
        assertTrue(tc.getData(Optional.of(issueData.id)).contains(timeEntryData));
        assertFalse(tc.getData(Optional.of(issueData.id)).contains(timeEntryData1));

        assertEquals(timeEntryData.id, tc.getData(timeEntryData.id).id);
        assertEquals(timeEntryData1.id, tc.getData(timeEntryData1.id).id);
    }

    /*
     *  Given   a cached issue with null time entries
     *  When    getData with the [parent] project id is called
     *  Then    null will be returned
     */
    @Test
    public void testGetIssueNullEntries() {
        IssueData issueData = new IssueData();
        issueData.setId(1L);
        issueData.parentId = 1L;

        assertEquals(0, ic.getSize());

        ic.putData(issueData, false);

        assertEquals(1, ic.getSize());

        assertNull(tc.getData(Optional.of(issueData.id)));
    }

    /*
     *  Given   a cached issue with no time entries
     *  When    4 threads are putting time entry data [1000] at the same time
     *  Then    all the time entry data is cached after all threads finish
     *  (1 insert + repeat put 1000 times)
     */
    @Test
    public void multiThreaded_testPut_x1000() {
        final int numberOfIterations = 1000;
        final int numberOfTimeEntries = 1000;
        final long parentId = 1;
        final int numberOfThreads = 4;

        IssueData issueData = new IssueData();
        issueData.setId(parentId);
        issueData.parentId = 1L;

        List<TimeEntryData> entries = new ArrayList<>();
        for (long i = 1; i <= numberOfTimeEntries; i++) {
            TimeEntryData timeEntryData = new TimeEntryData();
            timeEntryData.id = i;
            timeEntryData.parentId = parentId;
            entries.add(timeEntryData);
        }

        assertEquals(0, ic.getSize());

        ic.putData(issueData, false);

        assertEquals(1, ic.getSize());
        assertEquals(0, tc.getSize());

        for (int i = 0; i < 1 + numberOfIterations; i++) {
            //noinspection ResultOfMethodCallIgnored
            Flowable.fromIterable(entries)
                    .parallel(numberOfThreads)
                    .runOn(Schedulers.computation())
                    .map(timeEntry -> {
                        tc.putData(timeEntry, true);
                        return timeEntry;
                    })
                    .sequential()
                    .count()
                    .blockingGet();

            assertEquals(numberOfTimeEntries, tc.getSize());
            assertEquals(numberOfTimeEntries, ic.getData(parentId).getTimeEntries().size());
        }
    }


    /*
     *  Given   a cached issue with no time entries
     *  When    4 threads are putting timeEntry data [1000] at the same time
     *  Then    all the timeEntry data is cached after all threads finish
     *  (1000 times clear + insert)
     */
    @Test
    public void multiThreaded_testInsert_x1000() {
        final int numberOfIterations = 1000;
        final int numberOfTimeEntries = 1000;
        final long parentId = 1;
        final int numberOfThreads = 4;

        IssueData issueData = new IssueData();
        issueData.setId(parentId);
        issueData.setParentId(1L);

        List<TimeEntryData> entries = new ArrayList<>();
        for (long i = 1; i <= numberOfTimeEntries; i++) {
            TimeEntryData timeEntryData = new TimeEntryData();
            timeEntryData.id = i;
            timeEntryData.parentId = parentId;
            entries.add(timeEntryData);
        }

        assertEquals(0, ic.getSize());
        issueData.setTimeEntries(null);
        ic.putData(issueData, false);

        assertEquals(1, ic.getSize());
        assertEquals(0, tc.getSize());

        // insert data
        for (int i = 0; i < numberOfIterations; i++) {
            //noinspection ResultOfMethodCallIgnored
            Flowable.fromIterable(entries)
                    .parallel(numberOfThreads)
                    .runOn(Schedulers.computation())
                    .map(timeEntry -> {
                        tc.putData(timeEntry, true);
                        return timeEntry;
                    })
                    .sequential()
                    .count()
                    .blockingGet();
            assertEquals(entries.size(), tc.getSize());
            assertEquals(entries.size(), ic.getData(issueData.id).getTimeEntries().size());
        }
    }

    /*
     *  Given   a time entry data with parentId= null
     *  When    putData(timeEntry, boolean) is called
     *  Then    the time entry will not be added to the cache
     *              because orphaned time entries are not allowed
     */
    @Test
    public void testOrphanedTimeEntry() {
        TimeEntryData timeEntryData = new TimeEntryData();
        timeEntryData.setId(1L);

        assertEquals(0, tc.getSize());

        tc.putData(timeEntryData, false);

        assertEquals(0, tc.getSize());
    }
}