package org.rares.miner49er.persistence.tables;

import android.database.sqlite.SQLiteDatabase;

public final class UserTable {

    public static final String NAME = "users";
    public static final String ID_COLUMN = "_id";
    public static final String LAST_UPDATED_COLUMN = "last_updated";
    public static final String NAME_COLUMN = "user_name";
    public static final String PWD_COLUMN = "password";
    public static final String EMAIL_COLUMN = "email";
    public static final String PHOTO_COLUMN = "photo_path";
    public static final String API_KEY_COLUMN = "api_key";
    public static final String ROLE_COLUMN = "role";

    private UserTable() {
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (_id INTEGER PRIMARY KEY,\n"
                        + "last_updated INTEGER,\n"
                        + "user_name TEXT,\n"
                        + "password TEXT,\n"
                        + "email TEXT,\n"
                        + "photo_path TEXT,\n"
                        + "api_key TEXT,\n"
                        + "role INTEGER);");
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion) {
    }
}
