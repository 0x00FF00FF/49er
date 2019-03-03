package org.rares.miner49er.domain.issues.persistence;

import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.functions.Predicate;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.SimpleCache;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IssueDataCache implements Cache<IssueData> {

    private SimpleCache cache = SimpleCache.getInstance();
    private LruCache<Long, IssueData> issuesCache = cache.getIssuesCache();
    private LruCache<Long, ProjectData> projectsCache = cache.getProjectsCache();

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
            ProjectData projectData = projectsCache.get(issue.parentId);
            if (projectData != null) {
                List<IssueData> issues = projectsCache.get(projectData.id).getIssues();//projectIssuesCache.get(projectData);
                if (issues != null) {

                    boolean found = false;
                    for (IssueData issueData : issues) {
                        if (issueData.id.equals(issue.id)) {
//                      perhaps_not_needed: updating the same object?
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
        issuesCache.put(issue.id, issue);
        cache.sendEvent();
    }

    @Override
    public void removeData(IssueData issue) {
        ProjectData projectData = projectsCache.get(issue.parentId);
        if (projectData != null) {
            List<IssueData> projectIssues = projectData.getIssues();
            if (projectIssues != null) {
                for (IssueData issueData : projectIssues) {
                    if (issueData.id.equals(issue.id)) {
                        projectIssues.remove(issueData);
                        break;
                    }
                }
            }
        }
        issuesCache.remove(issue.id);
        cache.sendEvent();
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
                return issues == null ? Collections.emptyList() : issues;
            }
        }
        return new ArrayList<>(issuesCache.snapshot().values());
    }
}
