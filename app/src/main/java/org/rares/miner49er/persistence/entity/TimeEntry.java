package org.rares.miner49er.persistence.entity;


import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteType;
import lombok.Data;



@Data
@StorIOSQLiteType(table = "time_entries")
public class TimeEntry {

    @StorIOSQLiteColumn(name = "_id", key = true)               long id;
    @StorIOSQLiteColumn(name = "_issue_id")                     long issueId;
    @StorIOSQLiteColumn(name = "_user_id")                      long userId;
    @StorIOSQLiteColumn(name = "work_date")                     long workDate;
    @StorIOSQLiteColumn(name = "date_added")                    long dateAdded;
    @StorIOSQLiteColumn(name = "last_updated")                  long lastUpdated;
    @StorIOSQLiteColumn(name = "hours")                         int hours;
    @StorIOSQLiteColumn(name = "comments")                      String comments;

    Issue issue;
    User user;
}
