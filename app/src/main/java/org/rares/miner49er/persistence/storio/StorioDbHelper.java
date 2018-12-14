package org.rares.miner49er.persistence.storio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.rares.miner49er.persistence.storio.tables.IssueTable;
import org.rares.miner49er.persistence.storio.tables.ProjectsTable;
import org.rares.miner49er.persistence.storio.tables.TimeEntryTable;
import org.rares.miner49er.persistence.storio.tables.UserProjectTable;
import org.rares.miner49er.persistence.storio.tables.UserTable;

public class StorioDbHelper extends SQLiteOpenHelper {

    private static final String TAG = StorioDbHelper.class.getSimpleName();

    public StorioDbHelper(Context context) {
        super(context, "49er.db", null, 2);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        UserTable.createTable(db);
        TimeEntryTable.createTable(db);
        IssueTable.createTable(db);
        ProjectsTable.createTable(db);
        UserProjectTable.createTable(db);
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
        if (oldVersion == 1) {
            Log.w(TAG, "onUpgrade: " +
                    "from version [" + oldVersion + "] " +
                    "to version [" + newVersion + "] " +
                    "--- creating new UserProject Table.");
            UserProjectTable.createTable(db);
        }
    }
}
