package org.rares.miner49er.persistence.entity;


import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteType;
import lombok.Data;

import java.util.List;

@Data
@StorIOSQLiteType(table = "issues")
public class Issue {

    @StorIOSQLiteColumn(name = "_id", key = true)               long id;
    @StorIOSQLiteColumn(name = "_project_id")                   long projectId;
    @StorIOSQLiteColumn(name = "_user_id")                      long ownerId;
    @StorIOSQLiteColumn(name = "date_added")                    long dateAdded;
    @StorIOSQLiteColumn(name = "date_due")                      long dateDue;
    @StorIOSQLiteColumn(name = "last_updated")                  long lastUpdated;
    @StorIOSQLiteColumn(name = "name")                          String name;

    User owner;
    Project project;
    List<TimeEntry> timeEntries;
//    List<User> assignedUsers;
}
