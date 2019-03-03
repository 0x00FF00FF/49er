package org.rares.miner49er.persistence.storio.tables;

import android.database.sqlite.SQLiteDatabase;

public final class TimeEntryTable {

    public static final String NAME =                       "time_entries";
    public static final String ID_COLUMN =                  "_id";
    public static final String ISSUE_ID_COLUMN =            "_issue_id";
    public static final String USER_ID_COLUMN =             "_user_id";
    public static final String WORK_DATE_COLUMN =           "work_date";
    public static final String DATE_ADDED_COLUMN =          "date_added";
    public static final String LAST_UPDATED_COLUMN =        "last_updated";
    public static final String HOURS_COLUMN =               "hours";
    public static final String COMMENTS_COLUMN =            "comments";

    public static final String T_ID_COLUMN =                NAME + "." + ID_COLUMN;
    public static final String T_ISSUE_ID_COLUMN =          NAME + "." + ISSUE_ID_COLUMN;
    public static final String T_USER_ID_COLUMN =           NAME + "." + USER_ID_COLUMN;
    public static final String T_WORK_DATE_COLUMN =         NAME + "." + WORK_DATE_COLUMN;
    public static final String T_DATE_ADDED_COLUMN =        NAME + "." + DATE_ADDED_COLUMN;
    public static final String T_LAST_UPDATED_COLUMN =      NAME + "." + LAST_UPDATED_COLUMN;
    public static final String T_HOURS_COLUMN =             NAME + "." + HOURS_COLUMN;
    public static final String T_COMMENTS_COLUMN =          NAME + "." + COMMENTS_COLUMN;

    public static final String A_ID_COLUMN =                NAME + "_" + ID_COLUMN;
    public static final String A_ISSUE_ID_COLUMN =          NAME + "_" + ISSUE_ID_COLUMN;
    public static final String A_USER_ID_COLUMN =           NAME + "_" + USER_ID_COLUMN;
    public static final String A_WORK_DATE_COLUMN =         NAME + "_" + WORK_DATE_COLUMN;
    public static final String A_DATE_ADDED_COLUMN =        NAME + "_" + DATE_ADDED_COLUMN;
    public static final String A_LAST_UPDATED_COLUMN =      NAME + "_" + LAST_UPDATED_COLUMN;
    public static final String A_HOURS_COLUMN =             NAME + "_" + HOURS_COLUMN;
    public static final String A_COMMENTS_COLUMN =          NAME + "_" + COMMENTS_COLUMN;

    public static final String COL_ALIAS = String.format("%s, %s, %s, %s, %s, %s, %s, %s",
            T_ID_COLUMN                + " as " + A_ID_COLUMN,
            T_ISSUE_ID_COLUMN          + " as " + A_ISSUE_ID_COLUMN,
            T_USER_ID_COLUMN           + " as " + A_USER_ID_COLUMN,
            T_WORK_DATE_COLUMN         + " as " + A_WORK_DATE_COLUMN,
            T_DATE_ADDED_COLUMN        + " as " + A_DATE_ADDED_COLUMN,
            T_LAST_UPDATED_COLUMN      + " as " + A_LAST_UPDATED_COLUMN,
            T_HOURS_COLUMN             + " as " + A_HOURS_COLUMN,
            T_COMMENTS_COLUMN          + " as " + A_COMMENTS_COLUMN);

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
