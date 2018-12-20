package org.rares.miner49er.persistence.dao;

import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.entries.repository.TimeEntriesDAO;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.repository.IssuesDAO;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.persistence.ProjectsDAO;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.persistence.UsersDAO;

public class GenericDaoFactory {
    private static GenericDAO<ProjectData> projectsDAO = null;
    private static GenericDAO<UserData> usersDAO = null;
    private static GenericDAO<IssueData> issuesDAO = null;
    private static GenericDAO<TimeEntryData> timeEntriesDAO = null;

    /*
        if this "if ladder" is not nice enough,
        another idea would be to not use classes,
        but objects (for parameters) and create
        a method for each type of DAO and just
        call the right method (overload).
     */

    @SuppressWarnings("unchecked")
    public static <T extends AbstractViewModel> GenericDAO<T> ofType(Class<T> c) {
        if (c.equals(ProjectData.class)) {
            if (projectsDAO == null) {
                projectsDAO = ProjectsDAO.newInstance();
            }
            return (GenericDAO<T>) projectsDAO;
        }
        if (c.equals(UserData.class)) {
            if (usersDAO == null) {
                usersDAO = UsersDAO.newInstance();
            }
            return (GenericDAO<T>) usersDAO;
        }
        if (c.equals(IssueData.class)) {
            if (issuesDAO == null) {
                issuesDAO = IssuesDAO.newInstance();
            }
            return (GenericDAO<T>) issuesDAO;
        }
        if (c.equals(TimeEntryData.class)) {
            if (timeEntriesDAO == null) {
                timeEntriesDAO = TimeEntriesDAO.newInstance();
            }
            return (GenericDAO<T>) timeEntriesDAO;
        }
        return null;
    }

    private GenericDaoFactory() {
        throw new IllegalStateException("No instances allowed.");
    }

}
