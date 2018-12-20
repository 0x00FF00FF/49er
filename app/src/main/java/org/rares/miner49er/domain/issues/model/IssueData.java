package org.rares.miner49er.domain.issues.model;

import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.dao.AbstractViewModel;

import java.util.List;

/**
 * @author rares
 * @since 12.03.2018
 */

@Getter
@Setter
public class IssueData extends AbstractViewModel {

//    private long id;
    private Long projectId;
    private String name;
    //    private User owner;
    private long dateAdded;
    private long dateDue;
    private List<TimeEntryData> timeEntries;
    private int color;
//    List<User> assignedUsers;

    public String toString() {
        return name;// + " [" + id +"]";
    }

    public boolean compareContents(IssueData other) {
        return
                projectId.equals(other.getProjectId()) &&
                        (name == null ? "" : name).equals((other.getName() == null ? "" : other.name)) &&
                        color == other.color &&
                        dateAdded == other.dateAdded &&
//                owner.equals(other.getOwner()) &&
                        dateDue == other.dateDue;
    }
}
