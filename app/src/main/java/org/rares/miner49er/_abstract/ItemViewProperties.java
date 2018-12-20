package org.rares.miner49er._abstract;

import lombok.Data;
import org.rares.miner49er.domain.entries.adapter.TimeEntryViewProperties;
import org.rares.miner49er.domain.issues.ui.viewholder.IssuesViewProperties;
import org.rares.miner49er.domain.projects.adapter.ProjectViewProperties;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.TimeEntry;

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
    private long id;
    private String name;

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
