package org.rares.miner49er.persistence.storio.tables;

import android.database.sqlite.SQLiteDatabase;
import com.pushtorefresh.storio3.sqlite.queries.Query;

public class ProjectsTable {
    private static final String TAG = "ProjectsTable";

    /* /!\ remember to update the storio resolvers */

    public static final String TABLE_NAME =                         "projects";

    public static final String COLUMN_ID =                          "_id";
    public static final String COLUMN_USER_ID =                     "_user_id";
    public static final String COLUMN_DATE_ADDED =                  "date_added";
    public static final String COLUMN_DATE_LAST_UPDATED =           "last_updated";
    public static final String COLUMN_PROJECT_NAME =                "project_name";
    public static final String COLUMN_PROJECT_DESCRIPTION =         "project_description";
    public static final String COLUMN_ICON_PATH =                   "icon_path";
    public static final String COLUMN_PICTURE_PATH =                "picture_path";
    public static final String COLUMN_DELETED =                     "deleted";
    public static final String COLUMN_OBJECT_ID =                   "objectId";
    public static final String COLUMN_ARCHIVED =                    "archived";

    public static final String T_COLUMN_ID =                          TABLE_NAME + "." + COLUMN_ID;
    public static final String T_COLUMN_USER_ID =                     TABLE_NAME + "." + COLUMN_USER_ID;
    public static final String T_COLUMN_DATE_ADDED =                  TABLE_NAME + "." + COLUMN_DATE_ADDED;
    public static final String T_COLUMN_DATE_LAST_UPDATED =           TABLE_NAME + "." + COLUMN_DATE_LAST_UPDATED;
    public static final String T_COLUMN_PROJECT_NAME =                TABLE_NAME + "." + COLUMN_PROJECT_NAME;
    public static final String T_COLUMN_PROJECT_DESCRIPTION =         TABLE_NAME + "." + COLUMN_PROJECT_DESCRIPTION;
    public static final String T_COLUMN_ICON_PATH =                   TABLE_NAME + "." + COLUMN_ICON_PATH;
    public static final String T_COLUMN_PICTURE_PATH =                TABLE_NAME + "." + COLUMN_PICTURE_PATH;
    public static final String T_COLUMN_DELETED =                     TABLE_NAME + "." + COLUMN_DELETED;
    public static final String T_COLUMN_OBJECT_ID =                   TABLE_NAME + "." + COLUMN_OBJECT_ID;
    public static final String T_COLUMN_ARCHIVED =                    TABLE_NAME + "." + COLUMN_ARCHIVED;

    public static final String A_COLUMN_ID =                          TABLE_NAME + "_" + COLUMN_ID;
    public static final String A_COLUMN_USER_ID =                     TABLE_NAME + "_" + COLUMN_USER_ID;
    public static final String A_COLUMN_DATE_ADDED =                  TABLE_NAME + "_" + COLUMN_DATE_ADDED;
    public static final String A_COLUMN_DATE_LAST_UPDATED =           TABLE_NAME + "_" + COLUMN_DATE_LAST_UPDATED;
    public static final String A_COLUMN_PROJECT_NAME =                TABLE_NAME + "_" + COLUMN_PROJECT_NAME;
    public static final String A_COLUMN_PROJECT_DESCRIPTION =         TABLE_NAME + "_" + COLUMN_PROJECT_DESCRIPTION;
    public static final String A_COLUMN_ICON_PATH =                   TABLE_NAME + "_" + COLUMN_ICON_PATH;
    public static final String A_COLUMN_PICTURE_PATH =                TABLE_NAME + "_" + COLUMN_PICTURE_PATH;
    public static final String A_COLUMN_DELETED =                     TABLE_NAME + "_" + COLUMN_DELETED;
    public static final String A_COLUMN_OBJECT_ID =                   TABLE_NAME + "_" + COLUMN_OBJECT_ID;
    public static final String A_COLUMN_ARCHIVED =                    TABLE_NAME + "_" + COLUMN_ARCHIVED;

    public static final String COL_ALIAS = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
            T_COLUMN_ID                     + " as " + A_COLUMN_ID,
            T_COLUMN_USER_ID                + " as " + A_COLUMN_USER_ID,
            T_COLUMN_DATE_ADDED             + " as " + A_COLUMN_DATE_ADDED,
            T_COLUMN_DATE_LAST_UPDATED      + " as " + A_COLUMN_DATE_LAST_UPDATED,
            T_COLUMN_PROJECT_NAME           + " as " + A_COLUMN_PROJECT_NAME,
            T_COLUMN_PROJECT_DESCRIPTION    + " as " + A_COLUMN_PROJECT_DESCRIPTION,
            T_COLUMN_ICON_PATH              + " as " + A_COLUMN_ICON_PATH,
            T_COLUMN_PICTURE_PATH           + " as " + A_COLUMN_PICTURE_PATH,
            T_COLUMN_DELETED                + " as " + A_COLUMN_DELETED,
            T_COLUMN_OBJECT_ID              + " as " + A_COLUMN_OBJECT_ID,
            T_COLUMN_ARCHIVED               + " as " + A_COLUMN_ARCHIVED
    );

    public static void createTable(SQLiteDatabase db) {
        String createInstruction =
                "CREATE TABLE " +
                        TABLE_NAME + "(" +
                        COLUMN_ID                           + " INTEGER NOT NULL UNIQUE PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_ID                      + " INTEGER NOT NULL REFERENCES " + UserTable.NAME + "(" + UserTable.ID_COLUMN + "), " +
                        COLUMN_DATE_ADDED                   + " INTEGER NOT NULL, " +
                        COLUMN_DATE_LAST_UPDATED            + " INTEGER NOT NULL, " +
                        COLUMN_PROJECT_NAME                 + " TEXT NOT NULL, " +
                        COLUMN_PROJECT_DESCRIPTION          + " TEXT, " +
                        COLUMN_ICON_PATH                    + " TEXT NOT NULL, " +
                        COLUMN_PICTURE_PATH                 + " TEXT NOT NULL, " +
                        COLUMN_DELETED                      + " INTEGER, " +
                        COLUMN_OBJECT_ID                    + " TEXT UNIQUE NOT NULL, " +
                        COLUMN_ARCHIVED                     + " INTEGER " +
                        ");";
//        Log.i(TAG, "createTable: " + createInstruction);
        db.execSQL(createInstruction);
    }

    public static Query AllProjectsQuery = Query.builder().table(ProjectsTable.TABLE_NAME).build();
}