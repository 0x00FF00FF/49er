package org.rares.miner49er.persistence.storio.tables;

import android.database.sqlite.SQLiteDatabase;

public final class IssueTable {

    public static final String NAME = "issues";
    public static final String ID_COLUMN = "_id";
    public static final String PROJECT_ID_COLUMN = "_project_id";
    public static final String OWNER_ID_COLUMN = "_user_id";
    public static final String DATE_ADDED_COLUMN = "date_added";
    public static final String DATE_DUE_COLUMN = "date_due";
    public static final String LAST_UPDATED_COLUMN = "last_updated";
    public static final String NAME_COLUMN = "issue_name";

    private IssueTable() {
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + NAME + " ("
                + ID_COLUMN + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
                + PROJECT_ID_COLUMN + " INTEGER NOT NULL " +
                    "REFERENCES " + ProjectsTable.TABLE_NAME + "(" + ProjectsTable.COLUMN_ID + ") ON DELETE CASCADE, "
                + OWNER_ID_COLUMN + " INTEGER,\n"
                + DATE_ADDED_COLUMN + " INTEGER,\n"
                + DATE_DUE_COLUMN + " INTEGER,\n"
                + LAST_UPDATED_COLUMN + " INTEGER,\n"
                + NAME_COLUMN + " TEXT);");
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion) {
    }
}
