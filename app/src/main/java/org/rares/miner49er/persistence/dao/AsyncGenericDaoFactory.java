package org.rares.miner49er.persistence.dao;

import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.entries.persistence.AsyncTimeEntriesDao;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.persistence.AsyncIssuesDao;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.persistence.AsyncProjectsDao;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.persistence.AsyncUsersDao;

public class AsyncGenericDaoFactory {
    private static AsyncGenericDao<ProjectData> projectsDAO = null;
    private static AsyncGenericDao<UserData> usersDAO = null;
    private static AsyncGenericDao<IssueData> issuesDAO = null;
    private static AsyncGenericDao<TimeEntryData> timeEntriesDAO = null;

    /*
        if this "if ladder" is not nice enough,
        another idea would be to not use classes,
        but objects (for parameters) and create
        a method for each type of DAO and just
        call the right method (overload).
     */

    @SuppressWarnings("unchecked")
    public static <T extends AbstractViewModel> AsyncGenericDao<T> ofType(Class<T> c) {
        if (c.equals(ProjectData.class)) {
            if (projectsDAO == null) {
                projectsDAO = AsyncProjectsDao.getInstance();
            }
            return (AsyncGenericDao<T>) projectsDAO;
        }
        if (c.equals(UserData.class)) {
            if (usersDAO == null) {
                usersDAO = AsyncUsersDao.getInstance();
            }
            return (AsyncGenericDao<T>) usersDAO;
        }
        if (c.equals(IssueData.class)) {
            if (issuesDAO == null) {
                issuesDAO = AsyncIssuesDao.getInstance();
            }
            return (AsyncGenericDao<T>) issuesDAO;
        }
        if (c.equals(TimeEntryData.class)) {
            if (timeEntriesDAO == null) {
                timeEntriesDAO = AsyncTimeEntriesDao.getInstance();
            }
            return (AsyncGenericDao<T>) timeEntriesDAO;
        }

        throw new UnsupportedOperationException("No existing DAO was found for " + c.getSimpleName() + ".");
    }

    private AsyncGenericDaoFactory() {
        throw new IllegalStateException("No instances allowed.");
    }

}
