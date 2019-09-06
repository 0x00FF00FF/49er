package org.rares.miner49er.persistence.entities;


import lombok.Data;



@Data
/*@StorIOSQLiteType(table = "time_entries")*/
public class TimeEntry  implements ObjectIdHolder {

    /*@StorIOSQLiteColumn(name = "_id", key = true)       */        Long id;
    /*@StorIOSQLiteColumn(name = "_issue_id")             */        Long issueId;
    /*@StorIOSQLiteColumn(name = "_user_id")              */        Long userId;
    /*@StorIOSQLiteColumn(name = "work_date")             */        long workDate;
    /*@StorIOSQLiteColumn(name = "date_added")            */        long dateAdded;
    /*@StorIOSQLiteColumn(name = "last_updated")          */        long lastUpdated;
    /*@StorIOSQLiteColumn(name = "hours")                 */        int hours;
    /*@StorIOSQLiteColumn(name = "comments")              */        String comments;
    /*@StorIOSQLiteColumn(name = "deleted")               */        int deleted;
    /*@StorIOSQLiteColumn(name = "objectId")              */        String objectId;

    Issue issue;
    User user;
}
