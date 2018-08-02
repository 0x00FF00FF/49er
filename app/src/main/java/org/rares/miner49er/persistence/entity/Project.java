package org.rares.miner49er.persistence.entity;


import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteType;
import lombok.Data;

import java.util.List;

@Data
@StorIOSQLiteType(table = "projects")
public class Project {

    @StorIOSQLiteColumn(name = "_id", key = true)               long id;
    @StorIOSQLiteColumn(name = "_user_id")                      long ownerId;
    @StorIOSQLiteColumn(name = "date_added")                    long dateAdded;
    @StorIOSQLiteColumn(name = "last_updated")                  long lastUpdated;
    @StorIOSQLiteColumn(name = "project_name")                  String name;
    @StorIOSQLiteColumn(name = "project_description")           String description;
    @StorIOSQLiteColumn(name = "icon_path")                     String icon;
    @StorIOSQLiteColumn(name = "picture_path")                  String picture;

    User owner;
    List<User> team;
    List<Issue> issues;
}
