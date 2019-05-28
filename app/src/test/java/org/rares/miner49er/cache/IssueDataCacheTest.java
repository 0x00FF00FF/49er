package org.rares.miner49er.cache;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class IssueDataCacheTest {
    private ViewModelCache cache = ViewModelCache.getInstance();
    private Cache<IssueData> ic = cache.getCache(IssueData.class);
    private Cache<ProjectData> pc = cache.getCache(ProjectData.class);

    @Before
    public void setUp() {
        cache.clear();
    }

    /*
     *  Given   a list of IssueData
     *  When    the IssueDataCache#putData(List, Predicate, boolean)}
     *              method is called
     *  Then    it has no effect on the cache
     */
    @Test
    public void testPutDataPredicate() {
        List<IssueData> issues = new ArrayList<>();
        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issues.add(issueData);

        assertEquals(0, ic.getData(Optional.empty()).size());
        ic.putData(issues, p -> true, false);
        assertEquals(0, ic.getData(Optional.empty()).size());
    }

    /*
     *  Given   a list of IssueData
     *  When    the IssueDataCache#putData(List, false) method is called
     *  Then    the cache will contain the issue data in the list
     *  And     the issue data is not linked with its parent defined by parentId
     */
    @Test
    public void testPutDataList_noLink() {
        ProjectData parent = new ProjectData();
        parent.setId(1L);
        List<IssueData> issues = new ArrayList<>();
        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issueData.parentId = parent.id;
        IssueData issueData1 = new IssueData();
        issueData1.updateData(issueData);
        issueData1.id = 2L;
        issues.add(issueData);
        issues.add(issueData1);

        assertEquals(0, pc.getData(Optional.empty()).size());
        assertEquals(0, ic.getData(Optional.empty()).size());

        pc.putData(parent, false);

        assertTrue(pc.getData(Optional.empty()).contains(parent));

        ic.putData(issues, false);

        assertEquals(2, ic.getData(Optional.empty()).size());

        assertEquals(issueData.id, ic.getData(issueData.id).id);
        assertEquals(issueData1.id, ic.getData(issueData1.id).id);

        assertNull(pc.getData(parent.id).getIssues());
    }

    /*
     *  Given   ProjectData, IssueData already present in the cache
     *  When    the IssueDataCache#putData(List, true) method is called
     *  Then    the cache will contain the issue data in the list
     *  And     the issue data is linked with its parent defined by parentId
     */
    @Test
    public void testPutDataList_link() {
        ProjectData parent = new ProjectData();
        parent.setId(1L);
        List<IssueData> issues = new ArrayList<>();
        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issueData.parentId = parent.id;
        IssueData issueData1 = new IssueData();
        issueData1.updateData(issueData);
        issueData1.id = 2L;
        issues.add(issueData);
        issues.add(issueData1);

        assertEquals(0, pc.getData(Optional.empty()).size());
        assertEquals(0, ic.getData(Optional.empty()).size());

        pc.putData(parent, false);

        assertTrue(pc.getData(Optional.empty()).contains(parent));

        ic.putData(issues, true);

        assertEquals(2, ic.getData(Optional.empty()).size());

        assertEquals(issueData.id, ic.getData(issueData.id).id);
        assertEquals(issueData1.id, ic.getData(issueData1.id).id);

        assertNotNull(pc.getData(parent.id).getIssues());
        assertEquals(2, pc.getData(parent.id).getIssues().size());

        assertTrue(pc.getData(parent.id).getIssues().contains(issueData));
        assertTrue(pc.getData(parent.id).getIssues().contains(issueData1));
    }

    /*
     *  Given   ProjectData and IssueData already present in the cache
     *  When    the IssueDataCache#putData(List, true) method is called
     *  And     the list contains updated data (issue name)
     *  Then    the cache will contain the new issue data in the list
     *  And     the issue data is linked with its parent defined by parentId
     */
    @Test
    public void testPutDataList_linkUpdateData() {
        String initial = "initialName";
        String modified = "modified";
        ProjectData parent = new ProjectData();
        parent.setId(1L);
        List<IssueData> issues = new ArrayList<>();
        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issueData.setName(initial);
        issueData.parentId = parent.id;
        IssueData issueData1 = new IssueData();
        issueData1.updateData(issueData);
        issueData1.id = 1L;
        issueData1.setName(modified);
        issues.add(issueData);

        assertEquals(0, pc.getData(Optional.empty()).size());
        assertEquals(0, ic.getData(Optional.empty()).size());

        pc.putData(parent, false);
        ic.putData(issueData, true);

        assertTrue(pc.getData(Optional.empty()).contains(parent));
        assertTrue(pc.getData(parent.id).getIssues().contains(issueData));
        assertEquals(1, ic.getData(Optional.empty()).size());
        assertEquals(initial, pc.getData(parent.id).getIssues().get(0).getName());

        issues.clear();
        issues.add(issueData1);

        ic.putData(issues, true);

        assertEquals(1, ic.getData(Optional.empty()).size());
        assertEquals(issueData.id, ic.getData(issueData.id).id);
        assertEquals(modified, ic.getData(issueData.id).getName());

        assertNotNull(pc.getData(parent.id).getIssues());
        assertEquals(1, pc.getData(parent.id).getIssues().size());

        assertTrue(pc.getData(parent.id).getIssues().contains(issueData));
        assertEquals(modified, pc.getData(parent.id).getIssues().get(0).getName());
    }

    /*
     *  Given   ProjectData, IssueData already present in the cache
     *  And     ProjectData initially has Collections.emptyList as issues
     *  When    the IssueDataCache#putData(list, true) method is called
     *  And     the list contains new data
     *  Then    the cache will also contain the new issue data in the list
     *  And     the issue data is linked with its parent defined by parentId
     */
    @Test
    public void testPutDataList_linkAddData() {
        ProjectData parent = new ProjectData();
        parent.setId(1L);
        parent.setIssues(Collections.emptyList());
        List<IssueData> issues = new ArrayList<>();
        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issueData.parentId = parent.id;
        IssueData issueData1 = new IssueData();
        issueData1.updateData(issueData);
        issueData1.id = 2L;
        issues.add(issueData);

        assertEquals(0, pc.getData(Optional.empty()).size());
        assertEquals(0, ic.getData(Optional.empty()).size());

        pc.putData(parent, false);
        ic.putData(issueData, true);

        assertTrue(pc.getData(Optional.empty()).contains(parent));
        assertTrue(pc.getData(parent.id).getIssues().contains(issueData));
        assertEquals(1, ic.getData(Optional.empty()).size());
        assertEquals(1, pc.getData(parent.id).getIssues().size());

        issues.clear();
        issues.add(issueData1);

        ic.putData(issues, true);

        assertEquals(2, ic.getData(Optional.empty()).size());

        assertNotNull(pc.getData(parent.id).getIssues());
        assertEquals(2, pc.getData(parent.id).getIssues().size());

        assertTrue(pc.getData(parent.id).getIssues().contains(issueData));
        assertTrue(pc.getData(parent.id).getIssues().contains(issueData1));
    }

    /*
     *  Given   an issue data in the cache
     *  When    removeData is called
     *  Then    the issue data will not be in the cache anymore
     */
    @Test
    public void testRemoveData() {
        ProjectData parent = new ProjectData();
        parent.setId(1L);
        parent.setIssues(Collections.emptyList());
        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issueData.parentId = parent.id;

        IssueData issueData1 = new IssueData();
        issueData1.updateData(issueData);
        issueData1.id = 1L;

        assertEquals(0, pc.getSize());
        assertEquals(0, ic.getSize());

        pc.putData(parent, false);
        ic.putData(issueData, true);

        assertTrue(pc.getData(Optional.empty()).contains(parent));
        assertTrue(pc.getData(parent.id).getIssues().contains(issueData));
        assertEquals(1, ic.getSize());
        assertEquals(1, pc.getData(parent.id).getIssues().size());

        ic.removeData(issueData1);

        assertEquals(0, ic.getSize());
        assertEquals(0, pc.getData(parent.id).getIssues().size());
    }

    /*
     *  Given   two issue data in the cache
     *  When    getData is called with no parent id
     *  Then    all issue data will be returned
     */
    @Test
    public void testGetAllData() {
        ProjectData parent = new ProjectData();
        parent.setId(1L);
        List<IssueData> issues = new ArrayList<>();
        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issueData.parentId = parent.id;
        IssueData issueData1 = new IssueData();
        issueData1.updateData(issueData);
        issueData1.id = 2L;
        issues.add(issueData);
        issues.add(issueData1);

        assertEquals(0, pc.getData(Optional.empty()).size());
        assertEquals(0, ic.getData(Optional.empty()).size());

        pc.putData(parent, false);

        assertTrue(pc.getData(Optional.empty()).contains(parent));

        ic.putData(issues, false);

        assertEquals(2, ic.getData(Optional.empty()).size());

        assertEquals(issueData.id, ic.getData(issueData.id).id);
        assertEquals(issueData1.id, ic.getData(issueData1.id).id);
    }

    /*
     *  Given   two issue data in the cache, one linked to the parent and one not
     *  When    getData is called with parent id
     *  Then    all respective issue data will be returned (1 issue)
     *  And     the issue data not linked to the project will not be in the returned list (1 issue)
     */
    @Test
    public void testGetProjectIssueData() {
        ProjectData parent = new ProjectData();
        parent.setId(1L);
        List<IssueData> issues = new ArrayList<>();
        IssueData issueData = new IssueData();
        issueData.id = 1L;
        issueData.parentId = parent.id;
        IssueData issueData1 = new IssueData();
        issueData1.updateData(issueData);
        issueData1.id = 2L;
        issueData1.parentId = 2L;
        issues.add(issueData);
        issues.add(issueData1);

        assertEquals(0, pc.getData(Optional.empty()).size());
        assertEquals(0, ic.getData(Optional.empty()).size());

        pc.putData(parent, false);

        assertTrue(pc.getData(Optional.empty()).contains(parent));

        ic.putData(issues, true);

        assertEquals(2, ic.getSize());
        assertEquals(1, pc.getData(parent.id).getIssues().size());
        assertTrue(ic.getData(Optional.of(parent.id)).contains(issueData));
        assertFalse(ic.getData(Optional.of(parent.id)).contains(issueData1));

        assertEquals(issueData.id, ic.getData(issueData.id).id);
        assertEquals(issueData1.id, ic.getData(issueData1.id).id);
    }

    /*
     *  Given   a cached project with null issues
     *  When    getData with the [parent] project id is called
     *  Then    null will be returned
     */
    @Test
    public void testGetProjectNullIssues() {
        ProjectData parent = new ProjectData();
        parent.setId(1L);

        assertEquals(0, pc.getSize());

        pc.putData(parent, false);

        assertEquals(1, pc.getSize());

        assertNull(ic.getData(Optional.of(parent.id)));
    }

    /*
     *  Given   a cached project with no issues
     *  When    4 threads are putting issue data [1000] at the same time
     *  Then    all the issue data is cached after all threads finish
     *  (1 insert + repeat put 1000 times)
     */
    @Test
    public void multiThreaded_testPut_x1000() {
        final int numberOfIterations = 1000;
        final int numberOfIssues = 1000;
        final long parentId = 1;
        final int numberOfThreads = 4;

        ProjectData parent = new ProjectData();
        parent.setId(parentId);

        List<IssueData> issues = new ArrayList<>();
        for (long i = 1; i <= numberOfIssues; i++) {
            IssueData issueData = new IssueData();
            issueData.id = i;
            issueData.parentId = parentId;
            issues.add(issueData);
        }

        assertEquals(0, pc.getSize());

        pc.putData(parent, false);

        assertEquals(1, pc.getSize());
        assertEquals(0, ic.getSize());

        for (int i = 0; i < 1 + numberOfIterations; i++) {
            //noinspection ResultOfMethodCallIgnored
            Flowable.fromIterable(issues)
                    .parallel(numberOfThreads)
                    .runOn(Schedulers.computation())
                    .map(issue -> {
                        ic.putData(issue, true);
                        return issue;
                    })
                    .sequential()
                    .count()
                    .blockingGet();

            assertEquals(numberOfIssues, ic.getSize());
            assertEquals(numberOfIssues, pc.getData(parentId).getIssues().size());
        }
    }


    /*
     *  Given   a cached project with no issues
     *  When    4 threads are putting issue data [1000] at the same time
     *  Then    all the issue data is cached after all threads finish
     *  (1000 times insert)
     */
    @Test
    public void multiThreaded_testInsert_x1000() {
        final int numberOfIterations = 1000;
        final int numberOfIssues = 1000;
        final long parentId = 1;
        final int numberOfThreads = 4;

        ProjectData parent = new ProjectData();
        parent.setId(parentId);

        List<IssueData> issues = new ArrayList<>();
        for (long i = 1; i <= numberOfIssues; i++) {
            IssueData issueData = new IssueData();
            issueData.id = i;
            issueData.parentId = parentId;
            issues.add(issueData);
        }

        assertEquals(0, pc.getSize());
        parent.setIssues(null);
        pc.putData(parent, false);

        assertEquals(1, pc.getSize());
        assertEquals(0, ic.getSize());

        // insert data
        for (int i = 0; i < numberOfIterations; i++) {
            //noinspection ResultOfMethodCallIgnored
            Flowable.fromIterable(issues)
                    .parallel(numberOfThreads)
                    .runOn(Schedulers.computation())
                    .map(issue -> {
                        ic.putData(issue, true);
                        return issue;
                    })
                    .sequential()
                    .count()
                    .blockingGet();
            assertEquals(issues.size(), ic.getSize());
            assertEquals(issues.size(), pc.getData(parent.id).getIssues().size());
        }
    }

    /*
     *  Given   a not cached issue with null parent id,
     *  When    put(issue, true) is called
     *  Then    the issue is skipped because no orphaned entities are allowed
     */
    @Test
    public void orphanedIssue() {
        IssueData issueData = new IssueData();
        issueData.setId(1L);

        assertEquals(0, ic.getSize());

        ic.putData(issueData, true);

        assertEquals(0, ic.getSize());
    }
}