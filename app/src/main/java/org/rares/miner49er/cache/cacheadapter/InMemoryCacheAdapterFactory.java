package org.rares.miner49er.cache.cacheadapter;

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

/**
 * Responsible for returning the right DAO wrapper for given class. <br />
 * DAO wrappers are adapters that decorate cache operations over actual DAO objects. <br />
 * See
 * {@link AbstractAsyncCacheAdapter} and related classes.
 */
public class InMemoryCacheAdapterFactory {

    private static AsyncGenericDao<ProjectData> projectsCacheAdapter = null;
    private static AsyncGenericDao<UserData> usersCacheAdapter = null;
    private static AsyncGenericDao<IssueData> issuesCacheAdapter = null;
    private static AsyncGenericDao<TimeEntryData> timeEntriesCacheAdapter = null;

    private static final String TAG = InMemoryCacheAdapterFactory.class.getSimpleName();

    /*
     * This is the "clean" version of the Factory, that uses objects as parameters
     * to know what type of DAO wrapper to return. The advantage is that its
     * implementation looks cleaner, but it sometimes needs no-purpose instantiation.
     * see GenericDaoFactory for the memory efficient implementation.
     *

        public static GenericDAO<ProjectData> from(ProjectData data) {
            return projectAdapter;
        }
        public static GenericDAO<IssueData> from(IssueData data) {
            return issueAdapter;
        }
        public static GenericDAO<TimeEntryData> from(TimeEntryData data) {
            return timeEntryAdapter;
        }
        public static GenericDAO<UserData> from(UserData data) {
            return userAdapter;
        }
     */

    @SuppressWarnings("unchecked")
    public static <T extends AbstractViewModel> AsyncGenericDao<T> ofType(Class<T> cls) {
        if (ProjectData.class.equals(cls)) {
            return (AsyncGenericDao<T>) getProjectsCacheAdapter();
        }
        if (IssueData.class.equals(cls)) {
            return (AsyncGenericDao<T>) getIssuesCacheAdapter();
        }
        if (TimeEntryData.class.equals(cls)) {
            return (AsyncGenericDao<T>) getTimeEntriesCacheAdapter();
        }
        if (UserData.class.equals(cls)) {
            return (AsyncGenericDao<T>) getUsersCacheAdapter();
        }

        throw new UnsupportedOperationException("No existing cache adapter was found for " + cls.getSimpleName() + ".");
    }

    private static AsyncGenericDao<ProjectData> getProjectsCacheAdapter() {
        if (projectsCacheAdapter == null) {
            projectsCacheAdapter = new AsyncProjectDataCacheAdapter();
        }
        return projectsCacheAdapter;
    }

    private static AsyncGenericDao<UserData> getUsersCacheAdapter() {
        if (usersCacheAdapter == null) {
            usersCacheAdapter = new AsyncUserDataCacheAdapter();
        }
        return usersCacheAdapter;
    }

    private static AsyncGenericDao<IssueData> getIssuesCacheAdapter() {
        if (issuesCacheAdapter == null) {
            issuesCacheAdapter = new AsyncIssueDataCacheAdapter();
        }
        return issuesCacheAdapter;
    }

    private static AsyncGenericDao<TimeEntryData> getTimeEntriesCacheAdapter() {
        if (timeEntriesCacheAdapter == null) {
            timeEntriesCacheAdapter = new AsyncTimeEntryDataCacheAdapter();
        }
        return timeEntriesCacheAdapter;
    }
}
