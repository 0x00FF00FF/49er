package org.rares.miner49er.persistence.storio.tables;

import android.database.sqlite.SQLiteDatabase;

public final class IssueTable {

    public static final String NAME =                       "issues";
    public static final String ID_COLUMN =                  "_id";
    public static final String PROJECT_ID_COLUMN =          "_project_id";
    public static final String OWNER_ID_COLUMN =            "_user_id";
    public static final String DATE_ADDED_COLUMN =          "date_added";
    public static final String DATE_DUE_COLUMN =            "date_due";
    public static final String LAST_UPDATED_COLUMN =        "last_updated";
    public static final String NAME_COLUMN =                "issue_name";
    public static final String DELETED_COLUMN =             "deleted";

    public static final String T_ID_COLUMN =            NAME + "." + ID_COLUMN;
    public static final String T_PROJECT_ID_COLUMN =    NAME + "." + PROJECT_ID_COLUMN;
    public static final String T_OWNER_ID_COLUMN =      NAME + "." + OWNER_ID_COLUMN;
    public static final String T_DATE_ADDED_COLUMN =    NAME + "." + DATE_ADDED_COLUMN;
    public static final String T_DATE_DUE_COLUMN =      NAME + "." + DATE_DUE_COLUMN;
    public static final String T_LAST_UPDATED_COLUMN =  NAME + "." + LAST_UPDATED_COLUMN;
    public static final String T_NAME_COLUMN =          NAME + "." + NAME_COLUMN;
    public static final String T_DELETED_COLUMN =       NAME + "." + DELETED_COLUMN;

    public static final String A_ID_COLUMN =            NAME + "_" + ID_COLUMN;
    public static final String A_PROJECT_ID_COLUMN =    NAME + "_" + PROJECT_ID_COLUMN;
    public static final String A_OWNER_ID_COLUMN =      NAME + "_" + OWNER_ID_COLUMN;
    public static final String A_DATE_ADDED_COLUMN =    NAME + "_" + DATE_ADDED_COLUMN;
    public static final String A_DATE_DUE_COLUMN =      NAME + "_" + DATE_DUE_COLUMN;
    public static final String A_LAST_UPDATED_COLUMN =  NAME + "_" + LAST_UPDATED_COLUMN;
    public static final String A_NAME_COLUMN =          NAME + "_" + NAME_COLUMN;
    public static final String A_DELETED_COLUMN =       NAME + "_" + DELETED_COLUMN;

    public static final String COL_ALIAS = String.format("%s, %s, %s, %s, %s, %s, %s, %s",
            T_ID_COLUMN                 + " as " + A_ID_COLUMN,
            T_PROJECT_ID_COLUMN         + " as " + A_PROJECT_ID_COLUMN,
            T_OWNER_ID_COLUMN           + " as " + A_OWNER_ID_COLUMN,
            T_DATE_ADDED_COLUMN         + " as " + A_DATE_ADDED_COLUMN,
            T_DATE_DUE_COLUMN           + " as " + A_DATE_DUE_COLUMN,
            T_LAST_UPDATED_COLUMN       + " as " + A_LAST_UPDATED_COLUMN,
            T_NAME_COLUMN               + " as " + A_NAME_COLUMN,
            T_DELETED_COLUMN            + " as " + A_DELETED_COLUMN);

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
                + NAME_COLUMN + " TEXT,\n"
                + DELETED_COLUMN + " INTEGER);");
    }
}
