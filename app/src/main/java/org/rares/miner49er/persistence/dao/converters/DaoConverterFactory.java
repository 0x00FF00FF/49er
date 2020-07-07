package org.rares.miner49er.persistence.dao.converters;

import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.entries.persistence.TimeEntryConverter;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.persistence.IssueConverter;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.persistence.ProjectConverter;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.persistence.UserConverter;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;

public class DaoConverterFactory {

    private static DaoConverter<Project, ProjectData> projectConverter = null;
    private static DaoConverter<Issue, IssueData> issueConverter = null;
    private static DaoConverter<TimeEntry, TimeEntryData> timeEntryConverter = null;
    private static DaoConverter<User, UserData> userConverter = null;

    @SuppressWarnings("unchecked")
    public static <DM, VM> DaoConverter<DM, VM> of(Class<DM> dmClass, Class<VM> vmClass) {

        if (Project.class.equals(dmClass) && ProjectData.class.equals(vmClass)) {
            return (DaoConverter<DM, VM>) getProjectConverter();
        }
        if (Issue.class.equals(dmClass) && IssueData.class.equals(vmClass)) {
            return (DaoConverter<DM, VM>) getIssueConverter();
        }
        if (TimeEntry.class.equals(dmClass) && TimeEntryData.class.equals(vmClass)) {
            return (DaoConverter<DM, VM>) getTimeEntryConverter();
        }
        if (User.class.equals(dmClass) && UserData.class.equals(vmClass)) {
            return (DaoConverter<DM, VM>) getUserConverter();
        }

        throw new UnsupportedOperationException(
                String.format("No existing converter was found for %s and %s.",
                        dmClass.getSimpleName(),
                        vmClass.getSimpleName()));
    }

    private static DaoConverter<Project, ProjectData> getProjectConverter() {
        if (projectConverter == null) {
            projectConverter = new ProjectConverter();
        }
        return projectConverter;
    }

    private static DaoConverter<Issue, IssueData> getIssueConverter() {
        if (issueConverter == null) {
            issueConverter = new IssueConverter();
        }
        return issueConverter;
    }

    private static DaoConverter<TimeEntry, TimeEntryData> getTimeEntryConverter() {
        if (timeEntryConverter == null) {
            timeEntryConverter= new TimeEntryConverter();
        }
        return timeEntryConverter;
    }

    private static DaoConverter<User, UserData> getUserConverter() {
        if (userConverter == null) {
            userConverter = new UserConverter();
        }
        return userConverter;
    }
}
