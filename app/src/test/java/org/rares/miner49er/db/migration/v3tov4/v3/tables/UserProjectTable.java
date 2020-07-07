package org.rares.miner49er.db.migration.v3tov4.v3.tables;

import android.database.sqlite.SQLiteDatabase;

public class UserProjectTable {
    public static final String NAME = "users_projects";
    public static final String ID_COLUMN = "_id";
    public static final String USER_ID_COLUMN = "_user_id";
    public static final String PROJECT_ID_COLUMN = "_project_id";

    private UserProjectTable() {
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + NAME + " (" + ID_COLUMN + " INTEGER NOT NULL UNIQUE PRIMARY KEY AUTOINCREMENT,\n " +
                        USER_ID_COLUMN + " INTEGER NOT NULL REFERENCES " + UserTable.NAME + "(" + UserTable.ID_COLUMN + "), \n " +
                        PROJECT_ID_COLUMN + " INTEGER NOT NULL REFERENCES " + ProjectsTable.TABLE_NAME + "(" + ProjectsTable.COLUMN_ID + ") ON DELETE CASCADE " +
                        ");"
        );
    }
}
