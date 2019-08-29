package org.rares.miner49er.persistence.storio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.persistence.storio.tables.IssueTable;
import org.rares.miner49er.persistence.storio.tables.ProjectsTable;
import org.rares.miner49er.persistence.storio.tables.TimeEntryTable;
import org.rares.miner49er.persistence.storio.tables.UserProjectTable;
import org.rares.miner49er.persistence.storio.tables.UserTable;
import org.rares.miner49er.ui.custom.functions.Consumer;

import java.util.List;

public class StorioDbHelper extends SQLiteOpenHelper {

    private static final String TAG = StorioDbHelper.class.getSimpleName();

    private List<Consumer<SQLiteDatabase>> tables;
    private Context context;
    private int version = 3;

    public static class Builder {
        List<Consumer<SQLiteDatabase>> tables;
        Context context;
        int version;

        public StorioDbHelper build() {
            return new StorioDbHelper(this);
        }

        public Builder tables(List<Consumer<SQLiteDatabase>> tables) {
            this.tables = tables;
            return this;
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder version (int version) {
            this.version = version;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /*
     * version 2 added UserProjectTable
     * version 3 added pseudo-deletion columns
     */
//    public StorioDbHelper(Context context, List<Consumer<SQLiteDatabase>> tables) {
//        super(context, BaseInterfaces.DB_NAME, null, 3);
//        this.tables = tables;
//    }

    private StorioDbHelper(Builder builder) {
        super(builder.context, BaseInterfaces.DB_NAME, null, builder.version);
        this.tables = builder.tables;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Consumer<SQLiteDatabase> predicate : tables) {
            predicate.accept(db);
        }
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
        Log.d(TAG, "onUpgrade() called with: db = [" + db + "], oldVersion = [" + oldVersion + "], newVersion = [" + newVersion + "]");
        System.out.println("onUpgrade() called with: db = [" + db + "], oldVersion = [" + oldVersion + "], newVersion = [" + newVersion + "]");
        if (oldVersion < 2) {
            Log.w(TAG, "onUpgrade: " +
                    "from version [" + oldVersion + "] " +
                    "to version [" + newVersion + "] " +
                    "--- creating new UserProject Table.");
            UserProjectTable.createTable(db);
        }
        if (oldVersion < 3) {
            // add deleted column
            String alter = "ALTER TABLE %s ADD COLUMN %s %s DEFAULT %s;";
            db.execSQL(String.format(alter, UserTable.NAME, UserTable.ACTIVE_COLUMN, "INTEGER(1)", "1"));
            db.execSQL(String.format(alter, TimeEntryTable.NAME, TimeEntryTable.DELETED_COLUMN, "INTEGER(1)", "0"));
            db.execSQL(String.format(alter, IssueTable.NAME, IssueTable.DELETED_COLUMN, "INTEGER(1)", "0"));
            db.execSQL(String.format(alter, ProjectsTable.TABLE_NAME, ProjectsTable.COLUMN_DELETED, "INTEGER(1)", "0"));
        }
        if (oldVersion < 4) {
            // add objectId column
            String alter = "ALTER TABLE %s ADD COLUMN %s %s DEFAULT %s;";
            db.execSQL(String.format(alter, UserTable.NAME, UserTable.OBJECT_ID_COLUMN, "TEXT(32)", null));
            db.execSQL(String.format(alter, TimeEntryTable.NAME, TimeEntryTable.OBJECT_ID_COLUMN, "TEXT(32)", null));
            db.execSQL(String.format(alter, IssueTable.NAME, IssueTable.OBJECT_ID_COLUMN, "TEXT(32)", null));
            db.execSQL(String.format(alter, ProjectsTable.TABLE_NAME, ProjectsTable.COLUMN_OBJECT_ID, "TEXT(32)", null));
            db.execSQL(String.format(alter, ProjectsTable.TABLE_NAME, ProjectsTable.COLUMN_ARCHIVED, "INTEGER(1)", "0"));
        }
    }
}
