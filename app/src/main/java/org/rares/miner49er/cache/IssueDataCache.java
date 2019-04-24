package org.rares.miner49er.cache;

import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.functions.Predicate;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IssueDataCache implements Cache<IssueData> {

    private ViewModelCache cache = ViewModelCache.getInstance();
    private LruCache<Long, IssueData> issuesCache = cache.getIssuesLruCache();
    private LruCache<Long, ProjectData> projectsCache = cache.getProjectsLruCache();

    @Override
    public void putData(List<IssueData> list, Predicate<IssueData> ptCondition, boolean link) {

    }

    @Override
    public void putData(List<IssueData> list, boolean link) {
        for (IssueData issueData : list) {
            putData(issueData, link);
        }
    }

    @Override
    public void putData(IssueData issue, boolean link) {
        if (link) {
            synchronized (projectsCache.get(issue.parentId)) {
                ProjectData projectData = projectsCache.get(issue.parentId);
                if (projectData != null) {
                    List<IssueData> issues = projectData.getIssues();
                    if (issues != null) {

                        boolean found = false;
                        for (IssueData issueData : issues) {
                            if (issueData.id.equals(issue.id)) {
                                issueData.updateData(issue);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            issues.add(issue);
                        }
                    } else {
                        issues = new ArrayList<>();
                        issues.add(issue);
                        projectData.setIssues(issues);
                    }
                }
            }
        }
        issuesCache.put(issue.id, issue);
        cache.sendEvent(CACHE_EVENT_UPDATE_ISSUE);
    }

    @Override
    public void removeData(IssueData issue) {
        synchronized (projectsCache.get(issue.parentId)) {
            ProjectData projectData = projectsCache.get(issue.parentId);
            if (projectData != null) {
                List<IssueData> projectIssues = projectData.getIssues();
                if (projectIssues != null) {
                    for (int i = 0; i < projectIssues.size(); i++) {
                        IssueData issueData = projectIssues.get(i);
                        if (issueData.id.equals(issue.id)) {
                            projectIssues.remove(i);
                            break;
                        }
                    }
                }
            }
        }
        issuesCache.remove(issue.id);
        cache.sendEvent(CACHE_EVENT_REMOVE_ISSUE);
    }

    @Override
    public IssueData getData(Long id) {
        return issuesCache.get(id);
    }

    @Override
    public List<IssueData> getData(Optional<Long> parentId) {
        if (parentId.isPresent()) {
            ProjectData projectData = projectsCache.get(parentId.get());
            if (projectData != null) {
                List<IssueData> issues = projectData.getIssues();
                if (issues == null) {
//                    return Collections.emptyList();
                    return null;
                }
                Collections.sort(issues, (id1, id2) -> id1.id.compareTo(id2.id));
                Collections.reverse(issues);
//                for (int i = 0; i < issues.size(); i++) {
//                    IssueData issueData = issues.get(i);
//                    Log.i(IssueDataCache.class.getSimpleName(), "getData: " + issueData.getName() + " " + issueData.id);
//                }
                return issues;
            }
        }
        return new ArrayList<>(issuesCache.snapshot().values());
    }

    @Override
    public int getSize() {
        return issuesCache.size();
    }
}
