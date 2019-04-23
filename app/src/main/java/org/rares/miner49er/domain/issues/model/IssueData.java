package org.rares.miner49er.domain.issues.model;

import android.util.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AbstractViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        boolean timeEntriesEqual = deepEquals(timeEntries, other.timeEntries);

        boolean ownerEqual = false;
        if (owner == null && other.owner == null) {
            ownerEqual = true;
        }
        if (owner != null && other.owner != null) {
            ownerEqual = owner.equals(other.getOwner());
        }

        return
                parentId.equals(other.getParentId()) &&
                        (name == null ? "" : name).equals((other.getName() == null ? "" : other.name)) &&
                        timeEntriesEqual &&
                        color == other.color &&
                        deleted == other.deleted &&
                        dateAdded == other.dateAdded &&
                        ownerEqual &&
                        ownerId == other.ownerId &&
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

    public IssueData clone(boolean shallow) {
        return shallow ? shallowClone() : deepClone();
    }

    private IssueData shallowClone() {
        try {
            return (IssueData) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "clone: operation not supported.", e);
        }
        IssueData issueData = new IssueData();
        issueData.updateData(this);
        return issueData;
    }

    private IssueData deepClone() {
        IssueData cloned = new IssueData();
        cloned.updateData(this);
        List<TimeEntryData> entries = cloned.timeEntries;
        if (entries != null) {
            List<TimeEntryData> clonedTimeEntries = new ArrayList<>();
            for (int i = 0; i < entries.size(); i++) {
                clonedTimeEntries.add(entries.get(i).clone());
            }
            cloned.timeEntries = clonedTimeEntries;
        }
        return cloned;
    }

    private boolean deepEquals(List<TimeEntryData> list1, List<TimeEntryData> list2) {
        if (list1 == null && list2 != null || list1 != null && list2 == null) {
            return false;
        }
        if (list1 == null && list2 == null) {
            return true;
        }
        if (list1.size() == list2.size()) {
            Comparator<TimeEntryData> c = (te1, te2) -> te1.id > te2.id ? 1 : te1.id.equals(te2.id) ? 0 : -1;
            Collections.sort(list1, c);
            Collections.sort(list2, c);
            for (int i = 0; i < list1.size(); i++) {
                if (!list1.get(i).compareContents(list2.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
