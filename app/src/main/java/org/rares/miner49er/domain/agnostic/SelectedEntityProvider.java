package org.rares.miner49er.domain.agnostic;

import org.rares.miner49er.persistence.dao.AbstractViewModel;

/**
 * Interface to simplify getting
 * the selected entity (project/
 * issue/time entry).
 */
public interface SelectedEntityProvider {
  int ET_PROJECT = 1;
  int ET_ISSUE = 2;
  int ET_TIME_ENTRY = 3;
  int ET_USER = 4;

  int getEntityType();
//  ObjectIdHolder getSelectedEntity();
  AbstractViewModel getSelectedEntity();

  /**
   * Get the latest version of the
   * [selected] entity from the sync
   * server.
   */
  void updateEntity();
}
