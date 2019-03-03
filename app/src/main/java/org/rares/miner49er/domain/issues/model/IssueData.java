package org.rares.miner49er.domain.issues.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.dao.AbstractViewModel;

import java.util.Collections;
import java.util.List;

/**
 * @author rares
 * @since 12.03.2018
 */

@Getter
@Setter
@ToString
public class IssueData extends AbstractViewModel {

    //    private long id;
//    private Long projectId;
    private String name;
    //    private User owner;
    private long dateAdded;
    private long dateDue;
    private List<TimeEntryData> timeEntries;
    private int color;

    //    List<User> assignedUsers;
/*
    public String toString() {
        return name;// + " [" + id +"]";
    }
*/
    public boolean compareContents(IssueData other) {
        return
                parentId.equals(other.getParentId()) &&
                        (name == null ? "" : name).equals((other.getName() == null ? "" : other.name)) &&
                        (timeEntries == null ? Collections.emptyList() : timeEntries).equals(other.timeEntries == null ? Collections.emptyList() : other.timeEntries) &&
                        color == other.color &&
                        dateAdded == other.dateAdded &&
//                owner.equals(other.getOwner()) &&
                        dateDue == other.dateDue;
    }

    public void updateData(IssueData newData) {
        id = newData.id;
        parentId = newData.parentId;
        name = newData.name;
        dateAdded = newData.dateAdded;
        dateDue = newData.dateDue;
        timeEntries = newData.timeEntries;
        color = newData.color;
    }
}
