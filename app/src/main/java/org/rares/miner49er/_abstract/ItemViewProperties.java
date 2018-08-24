package org.rares.miner49er._abstract;

import lombok.Data;
import org.rares.miner49er.entries.adapter.TimeEntryViewProperties;
import org.rares.miner49er.issues.adapter.IssuesViewProperties;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.projects.adapter.ProjectViewProperties;

/**
 * @author rares
 * @since 02.03.2018
 */

// this is in fact a DTO used to transfer
// properties from parent to child domains
// TODO: 7/9/18 rename and clean up child classes that are not used anymore
@Data
public abstract class ItemViewProperties {

    private int itemBgColor;
    private int id;

    public static ItemViewProperties create(Class c) {
        if (c.equals(TimeEntry.class)) {
            return new TimeEntryViewProperties();
        }
        if (c.equals(Issue.class)) {
            return new IssuesViewProperties();
        }
        return new ProjectViewProperties();
    }
}
