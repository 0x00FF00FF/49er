package org.rares.miner49er.viewmodel;

import androidx.lifecycle.ViewModel;

public class HierarchyViewModel extends ViewModel {
  public long selectedProjectId = -1L;
  public long selectedIssueId = -1L;
  public long selectedTimeEntryId = -1L;

  public int scrollPositionProjects = 0;
  public int scrollPositionIssues = 0;
  public int scrollPositionTimeEntries = 0;
}
