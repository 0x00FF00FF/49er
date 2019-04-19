package org.rares.miner49er.persistence.entities;


import lombok.Data;



@Data
/*@StorIOSQLiteType(table = "users")*/
public class User {

    /*@StorIOSQLiteColumn(name = "_id", key = true)       */        Long id;
    /*@StorIOSQLiteColumn(name = "last_updated")          */        long lastUpdated;
    /*@StorIOSQLiteColumn(name = "user_name")             */        String name;
    /*@StorIOSQLiteColumn(name = "password")              */        String pwd;
    /*@StorIOSQLiteColumn(name = "email")                 */        String email;
    /*@StorIOSQLiteColumn(name = "photo_path")            */        String photo;
    /*@StorIOSQLiteColumn(name = "api_key")               */        String apiKey;
    /*@StorIOSQLiteColumn(name = "role")                  */        int role;
    /*@StorIOSQLiteColumn(name = "active")                */        int active;
}
