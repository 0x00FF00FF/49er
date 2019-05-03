package org.rares.miner49er.persistence.entities;


import lombok.Data;

import java.util.List;

@Data
/*@StorIOSQLiteType(table = "projects")*/
public class Project {

    /*@StorIOSQLiteColumn(name = "_id", key = true)        */       Long id;
    /*@StorIOSQLiteColumn(name = "_user_id")               */       Long ownerId;
    /*@StorIOSQLiteColumn(name = "date_added")             */       long dateAdded;
    /*@StorIOSQLiteColumn(name = "last_updated")           */       long lastUpdated;
    /*@StorIOSQLiteColumn(name = "project_name")           */       String name;
    /*@StorIOSQLiteColumn(name = "project_description")    */       String description;
    /*@StorIOSQLiteColumn(name = "icon_path")              */       String icon;
    /*@StorIOSQLiteColumn(name = "picture_path")           */       String picture;
    /*@StorIOSQLiteColumn(name = "deleted")                */       int deleted;

    User owner;
    List<User> team;
    List<Issue> issues;
}
