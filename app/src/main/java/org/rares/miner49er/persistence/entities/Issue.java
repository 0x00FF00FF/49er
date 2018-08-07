package org.rares.miner49er.persistence.entities;


import lombok.Data;

import java.util.List;

@Data
// commented out the generation annotations because
// we already have the classes generated and copied
// to the 'resolvers' package for further editing

/*@StorIOSQLiteType(table = "issues")*/
public class Issue {

    /*@StorIOSQLiteColumn(name = "_id", key = true)     */          int id;
    /*@StorIOSQLiteColumn(name = "_project_id")         */          int projectId;
    /*@StorIOSQLiteColumn(name = "_user_id")            */          int ownerId;
    /*@StorIOSQLiteColumn(name = "date_added")          */          long dateAdded;
    /*@StorIOSQLiteColumn(name = "date_due")            */          long dateDue;
    /*@StorIOSQLiteColumn(name = "last_updated")        */          long lastUpdated;
    /*@StorIOSQLiteColumn(name = "issue_name")          */          String name;

    User owner;
    Project project;
    List<TimeEntry> timeEntries;
//    List<User> assignedUsers;

}
