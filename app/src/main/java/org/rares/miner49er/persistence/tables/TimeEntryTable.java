package org.rares.miner49er.persistence.tables;

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
        db.execSQL("CREATE TABLE time_entries (_id INTEGER PRIMARY KEY,\n"
                        + "_issue_id INTEGER,\n"
                        + "_user_id INTEGER,\n"
                        + "work_date INTEGER,\n"
                        + "date_added INTEGER,\n"
                        + "last_updated INTEGER,\n"
                        + "hours INTEGER,\n"
                        + "comments TEXT);");
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion) {
    }
}
