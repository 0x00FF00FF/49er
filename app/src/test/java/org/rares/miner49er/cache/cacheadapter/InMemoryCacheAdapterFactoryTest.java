package org.rares.miner49er.cache.cacheadapter;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.entries.persistence.AsyncTimeEntryDataCacheAdapter;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.persistence.AsyncIssueDataCacheAdapter;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.persistence.AsyncProjectDataCacheAdapter;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.persistence.AsyncUserDataCacheAdapter;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class InMemoryCacheAdapterFactoryTest {

    /*
     *  Given   a ProjectData class
     *  When    ofType is called
     *  Then    it should return a GenericDao CacheAdapter of the class type.
     */
    @Test
    public void ofType_ProjectData() {
        AsyncGenericDao dao = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        assertTrue(dao instanceof AsyncProjectDataCacheAdapter);
    }

    /*
     *  Given   a IssueData class
     *  When    ofType is called
     *  Then    it should return a GenericDao CacheAdapter of the class type.
     */
    @Test
    public void ofType_IssueData() {
        AsyncGenericDao dao = InMemoryCacheAdapterFactory.ofType(IssueData.class);
        assertTrue(dao instanceof AsyncIssueDataCacheAdapter);
    }

    /*
     *  Given   a TimeEntryData class
     *  When    ofType is called
     *  Then    it should return a GenericDao CacheAdapter of the class type.
     */
    @Test
    public void ofType_TimeEntryData() {
        AsyncGenericDao dao = InMemoryCacheAdapterFactory.ofType(TimeEntryData.class);
        assertTrue(dao instanceof AsyncTimeEntryDataCacheAdapter);
    }

    /*
     *  Given   a UserData class
     *  When    ofType is called
     *  Then    it should return a GenericDao CacheAdapter of the class type.
     */
    @Test
    public void ofType_UserData() {
        AsyncGenericDao dao = InMemoryCacheAdapterFactory.ofType(UserData.class);
        assertTrue(dao instanceof AsyncUserDataCacheAdapter);
    }

    /*
     *  Given   an unknown class
     *  When    ofType is called
     *  Then    it should throw an UnsupportedOperationException.
     */
    @Test
    public void ofType_Unknown() {
        class TestAVM extends AbstractViewModel {

        }
        try {
            AsyncGenericDao dao = InMemoryCacheAdapterFactory.ofType(TestAVM.class);
        } catch (Exception exception) {
            assertTrue(exception instanceof UnsupportedOperationException);
        }

    }

}