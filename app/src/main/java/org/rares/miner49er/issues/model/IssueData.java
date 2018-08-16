package org.rares.miner49er.issues.model;

import lombok.Data;
import org.rares.miner49er.entries.model.TimeEntryData;

import java.util.List;

/**
 * @author rares
 * @since 12.03.2018
 */

@Data
public class IssueData {

    private int id;
    private int projectId;
    private String name;
    //    private User owner;
    private long dateAdded;
    private long dateDue;
    private List<TimeEntryData> timeEntries;
//    List<User> assignedUsers;

    public String toString() {
        return name;// + " [" + id +"]";
    }

    public boolean compareContents(IssueData other) {
        return projectId == other.getProjectId() &&
                name.equals(other.getName()) &&
                dateAdded == other.dateAdded &&
//                owner.equals(other.getOwner()) &&
                dateDue == other.dateDue;
    }
}
