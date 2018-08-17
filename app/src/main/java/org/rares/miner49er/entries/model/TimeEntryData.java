package org.rares.miner49er.entries.model;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import lombok.Data;
import org.joda.time.DateTime;
import org.rares.miner49er.util.TextUtils;

/**
 * @author rares
 * @since 14.03.2018
 */

@Data
public class TimeEntryData {

    private long id;
    private long issueId;
    private long dateAdded;
    private long workDate;
    private String comments;

    private long userId;
    private String userName;
    private String userPhoto;

    @IntRange(from = 0, to = 10)
    private int hours;

    public boolean compareContents(@NonNull TimeEntryData otherTimeEntry) {
        return id == otherTimeEntry.getId() &&
                issueId == otherTimeEntry.getIssueId() &&
                hours == otherTimeEntry.getHours() &&
                workDate == otherTimeEntry.getWorkDate() &&
                dateAdded == otherTimeEntry.getDateAdded() &&
                (otherTimeEntry.getComments() == null ? "" : otherTimeEntry.getComments()).equals(comments == null ? "" : comments) &&
                (otherTimeEntry.getUserPhoto() == null ? "" : otherTimeEntry.getUserPhoto()).equals(userPhoto == null ? "" : userPhoto) &&
                (otherTimeEntry.getUserName() == null ? "" : otherTimeEntry.getUserName()).equals(userName == null ? "" : userName);
    }

    public String toString() {
        DateTime dateTime = new DateTime(getWorkDate());
        String pattern = "dd MMM" + (dateTime.year().get() < DateTime.now().year().get() ? " yyyy" : "");
        String entryDate = dateTime.toString(pattern);
        return TextUtils.extractInitials(getUserName()) + " | " + getHours() + " | " + entryDate;
    }
}
