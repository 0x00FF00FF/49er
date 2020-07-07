package org.rares.miner49er.viewmodel;

import androidx.lifecycle.ViewModel;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;

public class HierarchyViewModel extends ViewModel {
  public long selectedProjectId = -1L;
  public long selectedIssueId = -1L;
  public long selectedTimeEntryId = -1L;

  public int scrollPositionProjects = 0;
  public int scrollPositionIssues = 0;
  public int scrollPositionTimeEntries = 0;

  public Class getSelectedType() {
    long selected = getSelectedId();
    if (selected == -1) {
      return null;
    }
    if (selected == selectedTimeEntryId) {
      return TimeEntryData.class;
    }
    if (selected == selectedIssueId) {
      return IssueData.class;
    }
    if (selected == selectedProjectId) {
      return ProjectData.class;
    }
    return null;
  }

  public long getSelectedId() {
    if (selectedTimeEntryId > -1) {
      return selectedTimeEntryId;
    }
    if (selectedIssueId > -1) {
      return selectedIssueId;
    }
    return selectedProjectId;
  }
}
