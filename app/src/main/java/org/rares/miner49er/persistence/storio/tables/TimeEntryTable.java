package org.rares.miner49er.persistence.storio.tables;

import android.database.sqlite.SQLiteDatabase;

public final class TimeEntryTable {

    public static final String NAME = "time_entries";
    public static final String ID_COLUMN = "_id";
    public static final String ISSUE_ID_COLUMN = "_issue_id";
    public static final String USER_ID_COLUMN = "_user_id";
    public static final String WORK_DATE_COLUMN = "work_date";
    public static final String DATE_ADDED_COLUMN = "date_added";
    public static final String LAST_UPDATED_COLUMN = "last_updated";
    public static final String HOURS_COLUMN = "hours";
    public static final String COMMENTS_COLUMN = "comments";

    private TimeEntryTable() {
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE time_entries (" + ID_COLUMN + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
                + ISSUE_ID_COLUMN + " INTEGER NOT NULL " +
                    "REFERENCES " + IssueTable.NAME + "(" + IssueTable.ID_COLUMN + ") ON DELETE CASCADE, "
                + USER_ID_COLUMN + " INTEGER,\n"
                + WORK_DATE_COLUMN + " INTEGER,\n"
                + DATE_ADDED_COLUMN + " INTEGER,\n"
                + LAST_UPDATED_COLUMN + " INTEGER,\n"
                + HOURS_COLUMN + " INTEGER,\n"
                + COMMENTS_COLUMN + " TEXT);");
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion) {
    }
}
