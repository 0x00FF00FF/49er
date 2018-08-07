package org.rares.miner49er.persistence.entities;


import lombok.Data;



@Data
/*@StorIOSQLiteType(table = "time_entries")*/
public class TimeEntry {

    /*@StorIOSQLiteColumn(name = "_id", key = true)       */        int id;
    /*@StorIOSQLiteColumn(name = "_issue_id")             */        int issueId;
    /*@StorIOSQLiteColumn(name = "_user_id")              */        int userId;
    /*@StorIOSQLiteColumn(name = "work_date")             */        long workDate;
    /*@StorIOSQLiteColumn(name = "date_added")            */        long dateAdded;
    /*@StorIOSQLiteColumn(name = "last_updated")          */        long lastUpdated;
    /*@StorIOSQLiteColumn(name = "hours")                 */        int hours;
    /*@StorIOSQLiteColumn(name = "comments")              */        String comments;

    Issue issue;
    User user;
}
