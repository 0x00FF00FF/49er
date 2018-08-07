package org.rares.miner49er.persistence.tables;

import android.database.sqlite.SQLiteDatabase;

public final class ProjectTable {

    public static final String NAME = "projects";
    public static final String ID_COLUMN = "_id";
    public static final String OWNER_ID_COLUMN = "_user_id";
    public static final String DATE_ADDED_COLUMN = "date_added";
    public static final String LAST_UPDATED_COLUMN = "last_updated";
    public static final String NAME_COLUMN = "project_name";
    public static final String DESCRIPTION_COLUMN = "project_description";
    public static final String ICON_COLUMN = "icon_path";
    public static final String PICTURE_COLUMN = "picture_path";

    private ProjectTable() {
    }

    public static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE projects (_id INTEGER PRIMARY KEY,\n"
                        + "_user_id INTEGER,\n"
                        + "date_added INTEGER,\n"
                        + "last_updated INTEGER,\n"
                        + "project_name TEXT,\n"
                        + "project_description TEXT,\n"
                        + "icon_path TEXT,\n"
                        + "picture_path TEXT);");
    }

    public static void updateTable(SQLiteDatabase db, int oldVersion) {
    }
}
