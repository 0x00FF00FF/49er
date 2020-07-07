package org.rares.miner49er.cache;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.pushtorefresh.storio3.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ProjectDataCacheTest {
    private ViewModelCache cache;
    private Cache<IssueData> ic;
    private ProjectDataCache pc;

    @Before
    public void setUp() {
        if (cache != null) {
            cache.close();
        }
        cache = new ViewModelCache();
        ic = cache.getCache(IssueData.class);
        pc = (ProjectDataCache) cache.getCache(ProjectData.class);
    }

    /*
     *  Given   a clean cache
     *  And     a list of projects bigger than the current cache size
     *  When    putData(list, boolean) is called,
     *  Then    all items are stored
     */
    @Test
    public void testEnlargeProjectsCacheList() {
        assertCacheEmpty();

        final int initialCacheHash = cache.getProjectsLruCache().hashCode();                        // *
        final int initialCacheSize = cache.getProjectsLruCache().maxSize();                         // *

        List<ProjectData> projects = new ArrayList<>();
        for (long i = 0; i < initialCacheSize + 1; i++) {
            ProjectData project = new ProjectData();
            project.id = i + 1;
            project.setName("project " + i);
            projects.add(project);
        }

        pc.putData(projects, false);

        assertEquals(initialCacheSize + 1, pc.getSize());

        assertNotEquals(initialCacheHash, cache.getProjectsLruCache().hashCode());                  // *

        assertEquals(initialCacheSize + 1, cache.getProjectsLruCache().maxSize());          // *

        final int newSize = cache.getProjectsLruCache().size();                                     // *
        for (long i = 1; i <= newSize; i++) {   // '<=' because projects ids start from 1
            assertNotNull(pc.getData(i));                                                           // *
        }

        // * - details that are not particular to the ProjectDataCache implementation, but rather to ViewModelCache
    }

    /*
     *  Given   a full project cache,
     *  When    a new projectData is added by calling putData(projectData, boolean),
     *  Then    the size of the project cache will be increased and all the data will be present
     */
    @Test
    public void testEnlargeProjectsCache() {
        assertCacheEmpty();

        final String projectNamePrefix = "project ";
        final int cacheIncrease = 10;
        final int initialCacheHash = cache.getProjectsLruCache().hashCode();
        final int initialCacheSize = cache.getProjectsLruCache().maxSize();

        List<ProjectData> projects = new ArrayList<>();
        for (long i = 0; i < initialCacheSize; i++) {
            ProjectData project = new ProjectData();
            project.id = i + 1;
            project.setName(projectNamePrefix + project.id);
            projects.add(project);
        }

        pc.putData(projects, false);
        assertEquals(initialCacheSize, cache.getProjectsLruCache().maxSize());

        ProjectData extraProject = new ProjectData();
        extraProject.setId(initialCacheSize + 1L);
        extraProject.setName(projectNamePrefix + extraProject.id);
        pc.putData(extraProject, false);

        assertNotEquals(initialCacheHash, cache.getProjectsLruCache().hashCode());
        assertEquals(initialCacheSize + cacheIncrease, cache.getProjectsLruCache().maxSize());

        for (long i = 1; i < initialCacheSize + 1; i++) {
            assertNotNull(pc.getData(i));
        }

        assertNull(pc.getData(initialCacheSize + 2L));
    }

    /*
     *  Given   a list of ProjectData
     *  When    the ProjectDataCache#putData(List, Predicate, boolean)}
     *              method is called
     *  Then    it has no effect on the cache
     */
    @Test
    public void testPutDataPredicate() {
        assertCacheEmpty();

        List<ProjectData> projects = new ArrayList<>();
        ProjectData projectData = new ProjectData();
        projectData.id = 1L;
        projects.add(projectData);

        assertEquals(0, pc.getData(Optional.empty()).size());
        pc.putData(projects, p -> true, false);
        assertEquals(0, pc.getData(Optional.empty()).size());
    }

    /*
     *  Given   a clean cache
     *  And     a list of ProjectData,
     *  When    putData(List<ProjectData> list, boolean link) is called,
     *  Then    the list is stored into the cache
     */
    @Test
    public void testPutProjectDataList() {
        assertCacheEmpty();

        List<ProjectData> projects = new ArrayList<>();
        ProjectData project1 = new ProjectData();
        project1.setName("project 1");
        project1.id = 1L;

        ProjectData project2 = new ProjectData();
        project2.setName("project 2");
        project2.id = 2L;

        projects.add(project1);
        projects.add(project2);

        pc.putData(projects, false);

        List<ProjectData> cachedProjects = getAllProjectsFromCache();
        assertEquals(2, cachedProjects.size());
        assertTrue(cachedProjects.contains(project1));
        assertTrue(cachedProjects.contains(project2));
    }

    /*
     *  Given   a clean cache
     *  And     one ProjectData,
     *  When    putData(ProjectData projectData, boolean link) is called,
     *  Then    the ProjectData is stored into the cache
     */
    @Test
    public void testPutProjectData() {
        assertCacheEmpty();

        ProjectData project1 = new ProjectData();
        project1.setName("project 1");
        project1.id = 1L;

        pc.putData(project1, false);

        List<ProjectData> cachedProjects = getAllProjectsFromCache();
        assertEquals(1, cachedProjects.size());
        assertTrue(cachedProjects.contains(project1));
    }

    /*
     *  Given   one cached ProjectData,
     *  When    putData(ProjectData projectData, boolean link)
     *              is called with new data for the cached projectData,
     *  Then    the cached ProjectData is updated
     *  And     the new contents are available in the cache
     */
    @Test
    public void testUpdateProjectData() {
        assertCacheEmpty();

        ProjectData project1 = new ProjectData();
        project1.setName("project 1");
        project1.id = 1L;

        pc.putData(project1, false);

        List<ProjectData> cachedProjects = getAllProjectsFromCache();
        assertEquals(1, cachedProjects.size());
        assertTrue(cachedProjects.contains(project1));

        ProjectData project2 = new ProjectData();
        project2.setName("updated project 1");
        project2.id = 1L;

        pc.putData(project2, false);

        cachedProjects = getAllProjectsFromCache();
        assertEquals(1, cachedProjects.size());
        assertEquals("updated project 1", cachedProjects.get(0).getName());
    }

    /*
     *  Given   two cached Projects,
     *  When    removeData(ProjectData) is called,
     *  Then    one of them is removed from the cache,
     *  And     only one of them is still available
     */
    @Test
    public void testDeleteProjectData() {
        assertCacheEmpty();

        List<ProjectData> projects = new ArrayList<>();
        ProjectData project1 = new ProjectData();
        project1.setName("project 1");
        project1.id = 1L;

        ProjectData project2 = new ProjectData();
        project2.setName("project 2");
        project2.id = 2L;

        projects.add(project1);
        projects.add(project2);

        pc.putData(projects, false);

        List<ProjectData> cachedProjects = getAllProjectsFromCache();
        assertEquals(2, cachedProjects.size());
        assertTrue(cachedProjects.contains(project1));
        assertTrue(cachedProjects.contains(project2));

        pc.removeData(project2);
        cachedProjects = getAllProjectsFromCache();
        assertEquals(1, cachedProjects.size());
        assertTrue(cachedProjects.contains(project1));
    }

    /*
     *  Given   one cached ProjectData,
     *  When    getData(Long id) is called,
     *  Then    the ProjectData is returned
     */
    @Test
    public void testGetProjectDataById() {
        assertCacheEmpty();

        ProjectData project1 = new ProjectData();
        project1.setName("project 1");
        project1.id = 1L;

        pc.putData(project1, false);

        ProjectData cachedProject = pc.getData(1L);
        assertNotNull(cachedProject);
        assertEquals(1L, (long) cachedProject.id);
    }


    /*
     *  Given   a list of cached projects,
     *  When    getSize() is called,
     *  Then    it will return the number of
     *              projects that are available
     *              in the cache
     */
    @Test
    public void testGetSize() {
        assertCacheEmpty();

        List<ProjectData> projects = new ArrayList<>();
        ProjectData project1 = new ProjectData();
        project1.setName("project 1");
        project1.id = 1L;

        ProjectData project2 = new ProjectData();
        project2.setName("project 2");
        project2.id = 2L;

        projects.add(project1);
        projects.add(project2);

        pc.putData(projects, false);

        List<ProjectData> cachedProjects = getAllProjectsFromCache();
        assertEquals(2, cachedProjects.size());
        assertEquals(2, pc.getSize());
        assertTrue(cachedProjects.contains(project1));
        assertTrue(cachedProjects.contains(project2));
    }

    /*
     *  Given   a list of projects,
     *  When    getData(Optional<Long> parentId) is called,
     *  Then    all the projects will be returned,
     *              no matter what parent id is supplied
     */
    @Test
    public void getCachedProjectsByParentId() {
        assertCacheEmpty();

        List<ProjectData> projects = new ArrayList<>();
        ProjectData project1 = new ProjectData();
        project1.setName("project 1");
        project1.id = 1L;

        ProjectData project2 = new ProjectData();
        project2.setName("project 2");
        project2.id = 2L;

        projects.add(project1);
        projects.add(project2);

        pc.putData(projects, false);

        List<ProjectData> cachedProjects = pc.getData(Optional.of(4L));
        assertEquals(2, pc.getSize());
        assertTrue(cachedProjects.contains(project1));
        assertTrue(cachedProjects.contains(project2));

        cachedProjects = pc.getData(Optional.empty());
        assertEquals(2, pc.getSize());
        assertTrue(cachedProjects.contains(project1));
        assertTrue(cachedProjects.contains(project2));
    }


    private List<ProjectData> getAllProjectsFromCache() {
        return pc.getData(Optional.empty());
    }

    private void assertCacheEmpty() {
        assertEquals(0, pc.getData(Optional.empty()).size());
        assertEquals(0, ic.getData(Optional.empty()).size());
    }
}