package org.rares.miner49er.util;

import android.util.Log;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.util.List;

public class PermissionsUtil {
    private static AsyncGenericDao<UserData> usersDao = InMemoryCacheAdapterFactory.ofType(UserData.class);
    private static AsyncGenericDao<ProjectData> projectsDao = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
    private static AsyncGenericDao<IssueData> issuesDao = InMemoryCacheAdapterFactory.ofType(IssueData.class);
    private static AsyncGenericDao<TimeEntryData> entriesDao = InMemoryCacheAdapterFactory.ofType(TimeEntryData.class);
    private static final String TAG = "PermissionsUtil";

    private static UserData loggedInUser = ViewModelCacheSingleton.getInstance().loggedInUser;
    public static boolean isResponsible = false;

    public static boolean canRemoveProject(ProjectData projectData) {
        return loggedInUser.id.equals(projectData.getOwner().id) || isResponsible;
    }

    public static boolean canEditProject(ProjectData projectData) {
//        Thread.dumpStack();
        Log.i(TAG, "canEditProject: [" + loggedInUser + "][" + projectData + "]");
        return loggedInUser.id.equals(projectData.getOwner().id) || isResponsible; // FIXME project owner null
    }

    public static boolean canAddProject() {
        return true;
    }

    public static boolean canAddIssue(Long id) {
        ProjectData projectData = projectsDao.get(id, true).blockingGet().get();        //
        return canAddIssue(projectData);
    }

    public static boolean canAddIssue(ProjectData projectData) {
        List<UserData> projectTeam = projectData.getTeam();
        if (projectTeam != null) {
            for (UserData user : projectTeam) {
                if (loggedInUser.id.equals(user.id)) {
                    return true;
                }
            }
        }
        return isResponsible;
    }

    public static boolean canEditIssue(IssueData issueData) {
        return loggedInUser.id.equals(issueData.getOwnerId()) || isResponsible;
    }

    public static boolean canRemoveIssue(IssueData issueData) {
        return loggedInUser.id.equals(issueData.getOwnerId()) || isResponsible;
    }

    public static boolean canAddTimeEntry(IssueData issueData) {
        ProjectData projectData = projectsDao.get(issueData.parentId, true).blockingGet().get();
        List<UserData> projectTeam = projectData.getTeam();
        if (projectTeam != null) {
            for (UserData user : projectTeam) {
                if (loggedInUser.id.equals(user.id)) {
                    return true;
                }
            }
        }
        return isResponsible;
    }

    public static boolean canEditTimeEntry(TimeEntryData timeEntryData) {
        return loggedInUser.id.equals(timeEntryData.getUserId()) || isResponsible;
    }

    public static boolean canRemoveTimeEntry(TimeEntryData timeEntryData) {
        return loggedInUser.id.equals(timeEntryData.getUserId()) || isResponsible;
    }
}
