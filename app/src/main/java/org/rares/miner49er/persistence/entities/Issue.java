package org.rares.miner49er.persistence.entities;


import lombok.Data;

import java.util.List;

@Data
// commented out the generation annotations because
// we already have the classes generated and copied
// to the 'resolvers' package for further editing

/*@StorIOSQLiteType(table = "issues")*/
public class Issue implements ObjectIdHolder  {

    /*@StorIOSQLiteColumn(name = "_id", key = true)     */          Long id;
    /*@StorIOSQLiteColumn(name = "_project_id")         */          Long projectId;
    /*@StorIOSQLiteColumn(name = "_user_id")            */          Long ownerId;
    /*@StorIOSQLiteColumn(name = "date_added")          */          long dateAdded;
    /*@StorIOSQLiteColumn(name = "date_due")            */          long dateDue;
    /*@StorIOSQLiteColumn(name = "last_updated")        */          long lastUpdated;
    /*@StorIOSQLiteColumn(name = "issue_name")          */          String name;
    /*@StorIOSQLiteColumn(name = "deleted")             */          int deleted;
    /*@StorIOSQLiteColumn(name = "objectId")            */          String objectId;

    User owner;
    Project project;
    List<TimeEntry> timeEntries;
//    List<User> assignedUsers;

}
