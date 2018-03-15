package org.rares.miner49er.entries.model;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import lombok.Data;

/**
 * @author rares
 * @since 14.03.2018
 */

@Data
public class TimeEntryData implements Comparable<TimeEntryData> {

    private int id;
    private long dateAdded;
    private long date;
    private String comment;
    private String authorName;
    @IntRange(from=0, to=10)
    private int hours;

    @Override
    public int compareTo(@NonNull TimeEntryData otherTimeEntry) {
        if (date > otherTimeEntry.getDate()) {
            return 1;
        }
        if (date < otherTimeEntry.getDate()) {
            return -1;
        }
        return 0;
    }

    public boolean deepEquals(@NonNull TimeEntryData otherTimeEntry) {
        return
                id == otherTimeEntry.getId() &&
                        hours == otherTimeEntry.getHours() &&
                        dateAdded == otherTimeEntry.getDateAdded() &&
                        otherTimeEntry.getComment().equals(comment) &&
                        otherTimeEntry.getAuthorName().equals(authorName);
    }
}
