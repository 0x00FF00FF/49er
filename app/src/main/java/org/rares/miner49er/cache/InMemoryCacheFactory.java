package org.rares.miner49er.cache;

import org.rares.miner49er.cache.adapter.IssueDataCacheAdapter;
import org.rares.miner49er.cache.adapter.ProjectDataCacheAdapter;
import org.rares.miner49er.cache.adapter.TimeEntryDataCacheAdapter;
import org.rares.miner49er.cache.adapter.UserDataCacheAdapter;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.GenericDAO;

/**
 * Responsible for returning the right DAO wrapper for given object. <br />
 * DAO wrappers are adapters that decorate cache operations over actual DAO objects. <br />
 * See
 * {@link ProjectDataCacheAdapter},
 * {@link IssueDataCacheAdapter},
 * {@link TimeEntryDataCacheAdapter},
 * {@link UserDataCacheAdapter}.
 */
public class InMemoryCacheFactory {

    private static final ProjectDataCacheAdapter projectAdapter = new ProjectDataCacheAdapter();
    private static final IssueDataCacheAdapter issueAdapter = new IssueDataCacheAdapter();
    private static final TimeEntryDataCacheAdapter timeEntryAdapter = new TimeEntryDataCacheAdapter();
    private static final UserDataCacheAdapter userAdapter = new UserDataCacheAdapter();

    /*
     * This is the "clean" version of the Factory, that uses objects as parameters
     * to know what type of DAO wrapper to return. The advantage is that its
     * implementation looks cleaner, but it sometimes needs no-purpose instantiation.
     * */

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
}
