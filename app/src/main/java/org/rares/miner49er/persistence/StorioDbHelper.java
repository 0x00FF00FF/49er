package org.rares.miner49er.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.rares.miner49er.persistence.tables.IssueTable;
import org.rares.miner49er.persistence.tables.ProjectsTable;
import org.rares.miner49er.persistence.tables.TimeEntryTable;
import org.rares.miner49er.persistence.tables.UserTable;

public class StorioDbHelper extends SQLiteOpenHelper {

    private static final String TAG = StorioDbHelper.class.getSimpleName();

    public StorioDbHelper(Context context) {
        super(context, "49er.db", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        UserTable.createTable(db);
        TimeEntryTable.createTable(db);
        IssueTable.createTable(db);
        ProjectsTable.createTable(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "SHOULD UPGRADE" );
    }
}
