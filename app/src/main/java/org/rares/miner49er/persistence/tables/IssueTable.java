package org.rares.miner49er.persistence.tables;

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
        db.execSQL("CREATE TABLE issues (_id INTEGER PRIMARY KEY,\n"
                        + "_project_id INTEGER,\n"
                        + "_user_id INTEGER,\n"
                        + "date_added INTEGER,\n"
                        + "date_due INTEGER,\n"
                        + "last_updated INTEGER,\n"
                        + "issue_name TEXT);");
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion) {
    }
}
