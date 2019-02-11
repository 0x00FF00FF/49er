package org.rares.miner49er.cache;

import androidx.collection.LongSparseArray;
import androidx.collection.LruCache;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.ui.custom.functions.Optional;

import java.util.ArrayList;
import java.util.List;

/**
 * <h6>* * ONE CACHE TO RULE THEM ALL * *</h6>
 * <p>
 * The cache is used as a copy of the database. <br />
 * It contains: <br/>
 * * the list of projects,<br/>
 * * the list of all issues, indexed on projectId<br/>
 * * the list of all time entries, indexed on issueId<br/>
 * * a last recently used cache for issues<br/>
 * * a lru cache for time entries<br/>
 * </p>
 * <p>
 *     Supports the following operations: get/add/update/remove/clear.
 * </p>
 */
public enum VMCache {

    // TODO: 2/11/19 Limit the cache to <n> entries.
    // TODO: 2/11/19 Auto update the cache after <t> seconds.

    INSTANCE;

    private LongSparseArray<ProjectData> cachedProjects = new LongSparseArray<>();
    private LongSparseArray<List<IssueData>> cachedProjectIssues = new LongSparseArray<>();
    private LongSparseArray<List<TimeEntryData>> cachedIssueTimeEntries = new LongSparseArray<>();

    private LruCache<Long, IssueData> lruIssues = new LruCache<>(100);
    private LruCache<Long, TimeEntryData> lruTimeEntries = new LruCache<>(100);

    public long cacheLastUpdate = 0L;
    private final long cacheBuffer = 3600_000L;

    public void updateCachedProjects(List<ProjectData> projects) {

        for (ProjectData projectData : projects) {
            cachedProjects.append(projectData.id, projectData);
            List<IssueData> projectIssues = projectData.getIssues();
            updateCachedIssues(projectData.id, projectIssues);
            for (IssueData issue : projectIssues) {
                updateCachedTimeEntries(issue.id, issue.getTimeEntries());
            }
        }
    }

    public void clear() {
        cachedProjects.clear();
        cachedProjectIssues.clear();
        cachedIssueTimeEntries.clear();
        lruIssues.evictAll();
        lruTimeEntries.evictAll();
    }

    public List<ProjectData> getCachedProjects() {
        List<ProjectData> projectDataList = new ArrayList<>();
        for (int i = 0, arraySize = cachedProjects.size(); i < arraySize; i++) {
            projectDataList.add(cachedProjects.valueAt(i));
        }
        return projectDataList;
    }

    public void updateProjectData(ProjectData projectData) {
        List<ProjectData> pdl = new ArrayList<>();
        pdl.add(projectData);
        updateCachedProjects(pdl);
    }

    public ProjectData getProjectData(Long id) {
        return cachedProjects.get(id);
    }

    public void removeProjectData(ProjectData projectData) {
        for (IssueData id : projectData.getIssues()) {
            removeIssueData(id, true);
        }
//        cachedData.remove(projectData);
        cachedProjects.remove(projectData.id);

        cachedProjectIssues.remove(projectData.id);
    }

    public void updateCachedIssues(Long projectId, List<IssueData> issues) {
        cachedProjectIssues.put(projectId, issues);
    }

    public void updateCachedTimeEntries(Long issueId, List<TimeEntryData> timeEntries) {
        cachedIssueTimeEntries.put(issueId, timeEntries);
    }

    /**
     * @param issueData data to be removed from cache
     * @param topDown   true means that the issue data
     *                  will be searched in all cache
     *                  trees and will be removed.
     *                  false means that the data will
     *                  be erased from the related and
     *                  downwards trees (issue data +
     *                  related time entries)
     */
    public void removeIssueData(IssueData issueData, boolean topDown) {

        if (!topDown) {
            List<IssueData> issues = cachedProjectIssues.get(issueData.getProjectId());
            if (issues != null) {
                for (IssueData idata : issues) {
                    if (idata.getId().equals(issueData.getId())) {
                        issues.remove(idata);
                        break;
                    }
                }
            }
        }

        lruIssues.remove(issueData.id);
        cachedIssueTimeEntries.remove(issueData.id);

        for (TimeEntryData ted : issueData.getTimeEntries()) {
            removeTimeEntryData(ted, true);
        }
    }

    public Optional<List<IssueData>> getProjectIssuesData(Long projectId) {
        return Optional.of(cachedProjectIssues.get(projectId));
    }

    public List<TimeEntryData> getIssueTimeEntriesData(Long issueId) {
        return cachedIssueTimeEntries.get(issueId);
    }

    public TimeEntryData getLruTimeEntryData(Long timeEntryId) {
        return lruTimeEntries.get(timeEntryId);
    }

    public Optional<IssueData> getLruIssueData(Long issueId) {
        return Optional.ofNullable(lruIssues.get(issueId));
    }

    public void updateIssueData(IssueData issueData) {

        List<IssueData> issueDataList = cachedProjectIssues.get(issueData.getProjectId());
        if (issueDataList != null) {
            for (IssueData idata : issueDataList) {
                if (idata.getId().equals(issueData.getId())) {
                    idata.updateData(issueData);
                    break;
                }
            }
        }

        ProjectData projectData = cachedProjects.get(issueData.getProjectId());
        if (projectData != null) {
            issueDataList = projectData.getIssues();
            if (issueDataList != null) {
                for (IssueData iData : issueDataList) {
                    if (iData.getId().equals(issueData.getId())) {
                        iData.updateData(issueData);
                        break;
                    }
                }
            }
        }

        IssueData iData = lruIssues.get(issueData.getId());
        if (iData != null) {
            iData.updateData(issueData);
        } else {
            lruIssues.put(issueData.id, issueData);
        }

    }

    public void updateTimeEntryData(TimeEntryData data) {

        long projectId = -1;
        firstFor:
        for (int i = 0; i < cachedProjectIssues.size(); i++) {
            List<IssueData> issueDataList = cachedProjectIssues.get(i);
            if (issueDataList != null) {
                for (IssueData issueData : issueDataList) {
                    if (issueData.getId().equals(data.getIssueId())) {
                        projectId = issueData.getProjectId();
                        List<TimeEntryData> timeEntryDataList = issueData.getTimeEntries();
                        if (timeEntryDataList != null) {
                            for (TimeEntryData ted : timeEntryDataList) {
                                if (ted.getId().equals(data.getId())) {
                                    ted.updateData(data);
                                    break firstFor;
                                }
                            }
                        }
                    }
                }
            }
        }

        ProjectData projectData = cachedProjects.get(projectId);
        if (projectData != null) {
            List<IssueData> issueDataList = projectData.getIssues();
            if (issueDataList != null) {
                firstFor:
                for (IssueData issueData : issueDataList) {
                    if (issueData.getId().equals(data.getIssueId())) {
                        List<TimeEntryData> timeEntryDataList = issueData.getTimeEntries();
                        if (timeEntryDataList != null) {
                            for (TimeEntryData ted : timeEntryDataList) {
                                if (ted.getId().equals(data.getId())) {
                                    ted.updateData(data);
                                    break firstFor;
                                }
                            }
                        }
                    }
                }
            }
        }

        IssueData issueData = lruIssues.get(data.getIssueId());
        if (issueData != null) {
            List<TimeEntryData> timeEntryDataList = issueData.getTimeEntries();
            if (timeEntryDataList != null) {
                for (TimeEntryData ted : timeEntryDataList) {
                    if (ted.getId().equals(data.getId())) {
                        ted.updateData(data);
                        break;
                    }
                }
            }
        }

        List<TimeEntryData> timeEntryDataList = cachedIssueTimeEntries.get(data.getIssueId());
        if (timeEntryDataList != null) {
            for (TimeEntryData ted : timeEntryDataList) {
                if (ted.id.equals(data.id)) {
                    ted.updateData(data);
                    break;
                }
            }
        }

        lruTimeEntries.put(data.id, data);
    }

    public void updateLruTimeEntryData(TimeEntryData data) {
        lruTimeEntries.put(data.id, data);
    }

    public void removeTimeEntryData(TimeEntryData data, boolean topDown) {

        if (!topDown) {
            long projectId = -1;
            firstFor:
            for (int i = 0; i < cachedProjectIssues.size(); i++) {
                List<IssueData> issueDataList = cachedProjectIssues.get(i);
                if (issueDataList != null) {
                    for (IssueData issueData : issueDataList) {
                        if (issueData.getId().equals(data.getIssueId())) {
                            projectId = issueData.getProjectId();
                            List<TimeEntryData> timeEntryDataList = issueData.getTimeEntries();
                            if (timeEntryDataList != null) {
                                for (TimeEntryData ted : timeEntryDataList) {
                                    if (ted.getId().equals(data.getId())) {
                                        timeEntryDataList.remove(ted);
                                        break firstFor;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ProjectData projectData = cachedProjects.get(projectId);
            if (projectData != null) {
                List<IssueData> issueDataList = projectData.getIssues();
                if (issueDataList != null) {
                    firstFor:
                    for (IssueData issueData : issueDataList) {
                        if (issueData.getId().equals(data.getIssueId())) {
                            List<TimeEntryData> timeEntryDataList = issueData.getTimeEntries();
                            if (timeEntryDataList != null) {
                                for (TimeEntryData ted : timeEntryDataList) {
                                    if (ted.getId().equals(data.getId())) {
                                        timeEntryDataList.remove(ted);
                                        break firstFor;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            IssueData issueData = lruIssues.get(data.getIssueId());
            if (issueData != null) {
                List<TimeEntryData> timeEntryDataList = issueData.getTimeEntries();
                if (timeEntryDataList != null) {
                    for (TimeEntryData ted : timeEntryDataList) {
                        if (ted.getId().equals(data.getId())) {
                            timeEntryDataList.remove(ted);
                            break;
                        }
                    }
                }
            }
        }

        List<TimeEntryData> timeEntryDataList = cachedIssueTimeEntries.get(data.getIssueId());
        if (timeEntryDataList != null) {
            for (TimeEntryData ted : timeEntryDataList) {
                if (ted.getId().equals(data.getId())) {
                    timeEntryDataList.remove(ted);
                    break;
                }
            }
        }

        lruTimeEntries.remove(data.id);
    }
}
