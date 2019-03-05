package org.rares.miner49er.domain.entries.model;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.util.TextUtils;

/**
 * @author rares
 * @since 14.03.2018
 */

@Getter
@Setter
public class TimeEntryData extends AbstractViewModel {

    //    private long id;
//    private Long issueId;
    private long dateAdded;
    private long workDate;
    private String comments;

    private Long userId;
    private String userName;
    private String userPhoto;
    private int color;

    @IntRange(from = 0, to = 10)
    private int hours;

    public boolean compareContents(@NonNull TimeEntryData otherTimeEntry) {
        return id.equals(otherTimeEntry.id) &&
                parentId.equals(otherTimeEntry.parentId) &&
                hours == otherTimeEntry.hours &&
                workDate == otherTimeEntry.workDate &&
                dateAdded == otherTimeEntry.dateAdded &&
                color == otherTimeEntry.color &&
                (otherTimeEntry.comments == null ? "" : otherTimeEntry.comments).equals(comments == null ? "" : comments) &&
                (otherTimeEntry.userPhoto == null ? "" : otherTimeEntry.userPhoto).equals(userPhoto == null ? "" : userPhoto) &&
                (otherTimeEntry.userName == null ? "" : otherTimeEntry.userName).equals(userName == null ? "" : userName);
    }

    public String toLongString() {
        DateTime dateTime = new DateTime(getWorkDate());
        String pattern = "dd MMM" + (dateTime.year().get() < DateTime.now().year().get() ? " yyyy" : "");
        String entryDate = dateTime.toString(pattern);
        return getUserName() + " | " + getHours() + " | " + entryDate + " | " + comments;
    }

    public String toString() {
        DateTime dateTime = new DateTime(getWorkDate());
        String pattern = "dd MMM" + (dateTime.year().get() < DateTime.now().year().get() ? " yyyy" : "");
        String entryDate = dateTime.toString(pattern);
        return TextUtils.extractInitials(getUserName()) + " | " + getHours() + " | " + entryDate;
    }

    public void updateData(TimeEntryData newData) {
        parentId = newData.parentId;
        dateAdded = newData.dateAdded;
        workDate = newData.workDate;
        comments = newData.comments;
        userId = newData.userId;
        userName = newData.userName;
        userPhoto = newData.userPhoto;
        color = newData.color;
        hours = newData.hours;

    }
}
