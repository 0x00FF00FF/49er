package org.rares.miner49er.persistence.entity;


import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio3.sqlite.annotations.StorIOSQLiteType;
import lombok.Data;



@Data
@StorIOSQLiteType(table = "users")
public class User {

    @StorIOSQLiteColumn(name = "_id", key = true)               long id;
    @StorIOSQLiteColumn(name = "last_updated")                  long lastUpdated;
    @StorIOSQLiteColumn(name = "user_name")                     String name;
    @StorIOSQLiteColumn(name = "password")                      String pwd;
    @StorIOSQLiteColumn(name = "email")                         String email;
    @StorIOSQLiteColumn(name = "photo_path")                    String photo;
    @StorIOSQLiteColumn(name = "api_key")                       String apiKey;
    @StorIOSQLiteColumn(name = "role")                          int role;
}
