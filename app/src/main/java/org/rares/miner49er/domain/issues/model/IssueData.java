package org.rares.miner49er.domain.issues.model;

import android.util.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.users.model.UserData;
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
public class IssueData extends AbstractViewModel implements Cloneable {

    private static final String TAG = IssueData.class.getSimpleName();

    private String name;
    private UserData owner;
    private long ownerId;
    private long dateAdded;
    private long dateDue;
    private List<TimeEntryData> timeEntries;
    private int color;

    public boolean compareContents(IssueData other) {
        return
                parentId.equals(other.getParentId()) &&
                        (name == null ? "" : name).equals((other.getName() == null ? "" : other.name)) &&
                        (timeEntries == null ? Collections.emptyList() : timeEntries).equals(other.timeEntries == null ? Collections.emptyList() : other.timeEntries) &&
                        color == other.color &&
                        deleted == other.deleted &&
                        dateAdded == other.dateAdded &&
                        owner.equals(other.getOwner()) &&
                        ownerId==other.ownerId &&
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
        owner = newData.owner;
        ownerId = newData.ownerId;
        deleted = newData.deleted;
    }

    public IssueData clone()  {
        // shallow copy should be enough
        // if not, use update data
        try {
            return (IssueData) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "clone: operation not supported.", e);
        }
        IssueData issueData = new IssueData();
        issueData.updateData(this);
        return issueData;
    }
}
