package org.rares.miner49er.persistence.storio.tables;

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
    public static final String ACTIVE_COLUMN = "active";

    private UserTable() {
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users ("
                + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,\n"
                + LAST_UPDATED_COLUMN + " INTEGER,\n"
                + NAME_COLUMN + " TEXT,\n"
                + PWD_COLUMN + " TEXT,\n"
                + EMAIL_COLUMN + " TEXT,\n"
                + PHOTO_COLUMN + " TEXT,\n"
                + API_KEY_COLUMN + " TEXT,\n"
                + ROLE_COLUMN + " INTEGER,\n"
                + ACTIVE_COLUMN + " INTEGER(1)" + " DEFAULT 1"
                + ");");
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion) {
    }
}
