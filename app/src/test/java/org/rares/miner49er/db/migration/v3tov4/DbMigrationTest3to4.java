package org.rares.miner49er.db.migration.v3tov4;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.OpenParams;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rares.miner49er.db.migration.v3tov4.v3.tables.IssueTable;
import org.rares.miner49er.db.migration.v3tov4.v3.tables.ProjectsTable;
import org.rares.miner49er.db.migration.v3tov4.v3.tables.TimeEntryTable;
import org.rares.miner49er.db.migration.v3tov4.v3.tables.UserProjectTable;
import org.rares.miner49er.db.migration.v3tov4.v3.tables.UserTable;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.StorioDbHelper;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DbMigrationTest3to4 {

    private static final String TAG = DbMigrationTest3to4.class.getSimpleName();

    private final File localDbFile = new File("src/test/v3ToV4.db");

    private Context context = ApplicationProvider.getApplicationContext();
    private StorioDbHelper storioDbHelper;
    private SQLiteDatabase db;

    @Before
    public void setup() {
        System.out.println(localDbFile.getAbsolutePath());
        db = SQLiteDatabase.openOrCreateDatabase(localDbFile, SQLiteCursor::new);
        storioDbHelper = StorioDbHelper
                .builder()
                .version(3)
                .context(context)
                .tables(Arrays.asList(
                        UserTable::createTable,
                        TimeEntryTable::createTable,
                        IssueTable::createTable,
                        ProjectsTable::createTable,
                        UserProjectTable::createTable))
                .build();
        storioDbHelper.onCreate(db);
    }

    @After
    public void teardown() {
        if (!localDbFile.delete()) {
            fail();
        }
    }

    /*
     *  GIVEN   the old version of the database
     *  WHEN    the update script is ran
     *  THEN    the changes will be visible
     *          and applied with no loss of existing data.
     */
    @Test
    public void testMigration() {
        String insertProjects = "insert into projects " +
                "(\"_user_id\", " +
                "\"date_added\", " +
                "\"last_updated\", " +
                "\"project_name\", " +
                "\"project_description\", " +
                "\"icon_path\", " +
                "\"picture_path\" ) " +
                "values(" +
                "1, " +
                "1531216541999, " +
                "0, " +
                "\"my_project\", " +
                "\"my_description\", " +
                "\"http://www.example.com/\", " +
                "\"www.example.com\"" +
                ");";

        String insertIssues = "insert into issues " +
                "( \"_project_id\", " +
                "\"_user_id\", " +
                "\"date_added\", " +
                "\"last_updated\", " +
                "\"issue_name\", " +
                "\"deleted\" ) " +
                "values(1, 1, 1531216541999, 0, \"my issue\", 0);";

        String insertTimeEntries = "insert into time_entries " +
                "(\"_issue_id\", " +
                "\"_user_id\", " +
                "\"work_date\", " +
                "\"date_added\", " +
                "\"last_updated\", " +
                "\"hours\", " +
                "\"comments\", " +
                "\"deleted\") values (" +
                "1, 1, 1531216541999, 1531216541999, 0," +
                "4, \"comments with spaces\", 0);";

        String insertUsers = "insert into users (" +
                "\"last_updated\", " +
                "\"user_name\", " +
                "\"password\", " +
                "\"email\", " +
                "\"photo_path\", " +
                "\"api_key\", " +
                "\"role\", " +
                "\"active\") values(" +
                "0, \"user name\", \"5ee8e44fa887aa00==\", " +
                "\"user@somecomp.com\", \"some/path/image.jpg\", " +
                "\"xxx-xxxxx-xxxxxxxxxxx\", 1, 1);";

        String insertUserProjects = "insert into users_projects " +
                "(\"_user_id\", " +
                "\"_project_id\") " +
                "values (1, 1);";

        storioDbHelper.getWritableDatabase().execSQL(insertUsers);
        storioDbHelper.getWritableDatabase().execSQL(insertProjects);
        storioDbHelper.getWritableDatabase().execSQL(insertUserProjects);
        storioDbHelper.getWritableDatabase().execSQL(insertIssues);
        storioDbHelper.getWritableDatabase().execSQL(insertTimeEntries);

        Cursor cursor = storioDbHelper.getReadableDatabase().rawQuery("SELECT * FROM PROJECTS", null);
        String[] columnNames = cursor.getColumnNames();
        System.out.println("testMigration: before: " + Arrays.toString(columnNames));

        assertEquals(9, columnNames.length);

        Project project = new Project();
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnNames.length; i++) {
                System.out.println(i + ": " + columnNames[i] + " " + cursor.getColumnIndex(columnNames[i]));
            }
            System.out.println("------------------");

            project.setName(cursor.getString(cursor.getColumnIndex(ProjectsTable.COLUMN_PROJECT_NAME)));
            project.setDescription(cursor.getString(cursor.getColumnIndex(ProjectsTable.COLUMN_PROJECT_DESCRIPTION)));
            project.setId(cursor.getLong(cursor.getColumnIndex(ProjectsTable.COLUMN_ID)));
            project.setOwnerId(cursor.getLong(cursor.getColumnIndex(ProjectsTable.COLUMN_USER_ID)));
            project.setLastUpdated(cursor.getLong(cursor.getColumnIndex(ProjectsTable.COLUMN_DATE_LAST_UPDATED)));
            project.setPicture(cursor.getString(cursor.getColumnIndex(ProjectsTable.COLUMN_PICTURE_PATH)));
            project.setIcon(cursor.getString(cursor.getColumnIndex(ProjectsTable.COLUMN_ICON_PATH)));
            project.setDeleted(cursor.getInt(cursor.getColumnIndex(ProjectsTable.COLUMN_DELETED)));
            project.setDateAdded(cursor.getLong(cursor.getColumnIndex(ProjectsTable.COLUMN_DATE_ADDED)));
        }

        cursor.close();
        cursor = storioDbHelper.getReadableDatabase().rawQuery("SELECT * FROM ISSUES", null);
        columnNames = cursor.getColumnNames();
        assertEquals(8, columnNames.length);

        Issue issue = new Issue();
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnNames.length; i++) {
                System.out.println(i + ": " + columnNames[i] + " " + cursor.getColumnIndex(columnNames[i]));
            }
            System.out.println("------------------");
            issue.setId(cursor.getLong(cursor.getColumnIndex(IssueTable.ID_COLUMN)));
            issue.setProjectId(cursor.getLong(cursor.getColumnIndex(IssueTable.PROJECT_ID_COLUMN)));
            issue.setOwnerId(cursor.getLong(cursor.getColumnIndex(IssueTable.OWNER_ID_COLUMN)));
            issue.setDateAdded(cursor.getLong(cursor.getColumnIndex(IssueTable.DATE_ADDED_COLUMN)));
            issue.setDateDue(cursor.getLong(cursor.getColumnIndex(IssueTable.DATE_DUE_COLUMN)));
            issue.setLastUpdated(cursor.getLong(cursor.getColumnIndex(IssueTable.LAST_UPDATED_COLUMN)));
            issue.setName(cursor.getString(cursor.getColumnIndex(IssueTable.NAME_COLUMN)));
            issue.setDeleted(cursor.getInt(cursor.getColumnIndex(IssueTable.DELETED_COLUMN)));
        }

        cursor.close();
        cursor = storioDbHelper.getReadableDatabase().rawQuery("SELECT * FROM TIME_ENTRIES", null);
        columnNames = cursor.getColumnNames();
        assertEquals(9, columnNames.length);

        TimeEntry timeEntry = new TimeEntry();
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnNames.length; i++) {
                System.out.println(i + ": " + columnNames[i] + " " + cursor.getColumnIndex(columnNames[i]));
            }
            System.out.println("------------------");
            timeEntry.setId(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.ID_COLUMN)));
            timeEntry.setIssueId(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.ISSUE_ID_COLUMN)));
            timeEntry.setUserId(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.USER_ID_COLUMN)));
            timeEntry.setWorkDate(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.WORK_DATE_COLUMN)));
            timeEntry.setDateAdded(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.DATE_ADDED_COLUMN)));
            timeEntry.setLastUpdated(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.LAST_UPDATED_COLUMN)));
            timeEntry.setHours(cursor.getInt(cursor.getColumnIndex(TimeEntryTable.HOURS_COLUMN)));
            timeEntry.setComments(cursor.getString(cursor.getColumnIndex(TimeEntryTable.COMMENTS_COLUMN)));
            timeEntry.setDeleted(cursor.getInt(cursor.getColumnIndex(TimeEntryTable.DELETED_COLUMN)));
        }


        cursor.close();
        cursor = storioDbHelper.getReadableDatabase().rawQuery("SELECT * FROM USERS", null);
        columnNames = cursor.getColumnNames();
        assertEquals(9, columnNames.length);
        User user = new User();
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnNames.length; i++) {
                System.out.println(i + ": " + columnNames[i] + " " + cursor.getColumnIndex(columnNames[i]));
            }
            System.out.println("------------------");
            user.setId(cursor.getLong(cursor.getColumnIndex(UserTable.ID_COLUMN)));
            user.setLastUpdated(cursor.getLong(cursor.getColumnIndex(UserTable.LAST_UPDATED_COLUMN)));
            user.setName(cursor.getString(cursor.getColumnIndex(UserTable.NAME_COLUMN)));
            user.setPwd(cursor.getString(cursor.getColumnIndex(UserTable.PWD_COLUMN)));
            user.setEmail(cursor.getString(cursor.getColumnIndex(UserTable.EMAIL_COLUMN)));
            user.setPhoto(cursor.getString(cursor.getColumnIndex(UserTable.PHOTO_COLUMN)));
            user.setApiKey(cursor.getString(cursor.getColumnIndex(UserTable.API_KEY_COLUMN)));
            user.setRole(cursor.getInt(cursor.getColumnIndex(UserTable.ROLE_COLUMN)));
            user.setActive(cursor.getInt(cursor.getColumnIndex(UserTable.ACTIVE_COLUMN)));
        }


        cursor.close();
        cursor = storioDbHelper.getReadableDatabase().rawQuery("SELECT * FROM users_projects", null);
        columnNames = cursor.getColumnNames();
        assertEquals(3, columnNames.length);
        long uptId = -1;
        long uptUid = -1;
        long uptPid = -1;
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnNames.length; i++) {
                System.out.println(i + ": " + columnNames[i] + " " + cursor.getColumnIndex(columnNames[i]));
            }
            System.out.println("------------------");
            uptId = cursor.getLong(cursor.getColumnIndex(UserProjectTable.ID_COLUMN));
            uptUid = cursor.getLong(cursor.getColumnIndex(UserProjectTable.USER_ID_COLUMN));
            uptPid = cursor.getLong(cursor.getColumnIndex(UserProjectTable.PROJECT_ID_COLUMN));

        }

        cursor.close();

        System.out.println(uptId + " " + uptPid + " " + uptUid);


        project.setOwner(user);
        project.setTeam(Collections.singletonList(user));
        issue.setTimeEntries(Collections.singletonList(timeEntry));
        project.setIssues(Collections.singletonList(issue));

        System.out.println("-------------------------");
        System.out.println(project);

        assertNull(project.getObjectId());
        assertNull(issue.getObjectId());
        assertNull(timeEntry.getObjectId());
        assertNull(user.getObjectId());

        //
        // upgrade
        //
        //

        storioDbHelper.close();
        db = SQLiteDatabase.openDatabase(
                localDbFile,
                new OpenParams.Builder()
                        .setOpenFlags(SQLiteDatabase.OPEN_READWRITE & SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING)
                        .build());
        storioDbHelper = StorioDbHelper
                .builder()
                .version(4)
                .context(context)
                .tables(Arrays.asList(
                        UserTable::createTable,
                        TimeEntryTable::createTable,
                        IssueTable::createTable,
                        ProjectsTable::createTable,
                        UserProjectTable::createTable))
                .build();

        //

        cursor = storioDbHelper.getReadableDatabase().rawQuery("SELECT * FROM PROJECTS", null);
        columnNames = cursor.getColumnNames();
        System.out.println("testMigration: after: " + Arrays.toString(columnNames));

        assertEquals(11, columnNames.length);

        Project projectV4 = new Project();
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnNames.length; i++) {
                System.out.println(i + ": " + columnNames[i] + " " + cursor.getColumnIndex(columnNames[i]));
            }
            System.out.println("------------------");

            projectV4.setName(cursor.getString(cursor.getColumnIndex(ProjectsTable.COLUMN_PROJECT_NAME)));
            projectV4.setDescription(cursor.getString(cursor.getColumnIndex(ProjectsTable.COLUMN_PROJECT_DESCRIPTION)));
            projectV4.setId(cursor.getLong(cursor.getColumnIndex(ProjectsTable.COLUMN_ID)));
            projectV4.setOwnerId(cursor.getLong(cursor.getColumnIndex(ProjectsTable.COLUMN_USER_ID)));
            projectV4.setLastUpdated(cursor.getLong(cursor.getColumnIndex(ProjectsTable.COLUMN_DATE_LAST_UPDATED)));
            projectV4.setPicture(cursor.getString(cursor.getColumnIndex(ProjectsTable.COLUMN_PICTURE_PATH)));
            projectV4.setIcon(cursor.getString(cursor.getColumnIndex(ProjectsTable.COLUMN_ICON_PATH)));
            projectV4.setDeleted(cursor.getInt(cursor.getColumnIndex(ProjectsTable.COLUMN_DELETED)));
            projectV4.setDateAdded(cursor.getLong(cursor.getColumnIndex(ProjectsTable.COLUMN_DATE_ADDED)));
        }

        cursor.close();
        cursor = storioDbHelper.getReadableDatabase().rawQuery("SELECT * FROM ISSUES", null);
        columnNames = cursor.getColumnNames();
        assertEquals(9, columnNames.length);

        Issue issueV4 = new Issue();
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnNames.length; i++) {
                System.out.println(i + ": " + columnNames[i] + " " + cursor.getColumnIndex(columnNames[i]));
            }
            System.out.println("------------------");
            issueV4.setId(cursor.getLong(cursor.getColumnIndex(IssueTable.ID_COLUMN)));
            issueV4.setProjectId(cursor.getLong(cursor.getColumnIndex(IssueTable.PROJECT_ID_COLUMN)));
            issueV4.setOwnerId(cursor.getLong(cursor.getColumnIndex(IssueTable.OWNER_ID_COLUMN)));
            issueV4.setDateAdded(cursor.getLong(cursor.getColumnIndex(IssueTable.DATE_ADDED_COLUMN)));
            issueV4.setDateDue(cursor.getLong(cursor.getColumnIndex(IssueTable.DATE_DUE_COLUMN)));
            issueV4.setLastUpdated(cursor.getLong(cursor.getColumnIndex(IssueTable.LAST_UPDATED_COLUMN)));
            issueV4.setName(cursor.getString(cursor.getColumnIndex(IssueTable.NAME_COLUMN)));
            issueV4.setDeleted(cursor.getInt(cursor.getColumnIndex(IssueTable.DELETED_COLUMN)));
        }

        cursor.close();
        cursor = storioDbHelper.getReadableDatabase().rawQuery("SELECT * FROM TIME_ENTRIES", null);
        columnNames = cursor.getColumnNames();
        assertEquals(10, columnNames.length);

        TimeEntry timeEntryV4 = new TimeEntry();
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnNames.length; i++) {
                System.out.println(i + ": " + columnNames[i] + " " + cursor.getColumnIndex(columnNames[i]));
            }
            System.out.println("------------------");
            timeEntryV4.setId(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.ID_COLUMN)));
            timeEntryV4.setIssueId(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.ISSUE_ID_COLUMN)));
            timeEntryV4.setUserId(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.USER_ID_COLUMN)));
            timeEntryV4.setWorkDate(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.WORK_DATE_COLUMN)));
            timeEntryV4.setDateAdded(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.DATE_ADDED_COLUMN)));
            timeEntryV4.setLastUpdated(cursor.getLong(cursor.getColumnIndex(TimeEntryTable.LAST_UPDATED_COLUMN)));
            timeEntryV4.setHours(cursor.getInt(cursor.getColumnIndex(TimeEntryTable.HOURS_COLUMN)));
            timeEntryV4.setComments(cursor.getString(cursor.getColumnIndex(TimeEntryTable.COMMENTS_COLUMN)));
            timeEntryV4.setDeleted(cursor.getInt(cursor.getColumnIndex(TimeEntryTable.DELETED_COLUMN)));
        }


        cursor.close();
        cursor = storioDbHelper.getReadableDatabase().rawQuery("SELECT * FROM USERS", null);
        columnNames = cursor.getColumnNames();
        assertEquals(10, columnNames.length);
        User userV4 = new User();
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnNames.length; i++) {
                System.out.println(i + ": " + columnNames[i] + " " + cursor.getColumnIndex(columnNames[i]));
            }
            System.out.println("------------------");
            userV4.setId(cursor.getLong(cursor.getColumnIndex(UserTable.ID_COLUMN)));
            userV4.setLastUpdated(cursor.getLong(cursor.getColumnIndex(UserTable.LAST_UPDATED_COLUMN)));
            userV4.setName(cursor.getString(cursor.getColumnIndex(UserTable.NAME_COLUMN)));
            userV4.setPwd(cursor.getString(cursor.getColumnIndex(UserTable.PWD_COLUMN)));
            userV4.setEmail(cursor.getString(cursor.getColumnIndex(UserTable.EMAIL_COLUMN)));
            userV4.setPhoto(cursor.getString(cursor.getColumnIndex(UserTable.PHOTO_COLUMN)));
            userV4.setApiKey(cursor.getString(cursor.getColumnIndex(UserTable.API_KEY_COLUMN)));
            userV4.setRole(cursor.getInt(cursor.getColumnIndex(UserTable.ROLE_COLUMN)));
            userV4.setActive(cursor.getInt(cursor.getColumnIndex(UserTable.ACTIVE_COLUMN)));
        }


        cursor.close();
        cursor = storioDbHelper.getReadableDatabase().rawQuery("SELECT * FROM users_projects", null);
        columnNames = cursor.getColumnNames();
        assertEquals(3, columnNames.length);
        long uptIdV4 = -1;
        long uptUidV4 = -1;
        long uptPidV4 = -1;
        while (cursor.moveToNext()) {
            for (int i = 0; i < columnNames.length; i++) {
                System.out.println(i + ": " + columnNames[i] + " " + cursor.getColumnIndex(columnNames[i]));
            }
            System.out.println("------------------");
            uptIdV4 = cursor.getLong(cursor.getColumnIndex(UserProjectTable.ID_COLUMN));
            uptUidV4 = cursor.getLong(cursor.getColumnIndex(UserProjectTable.USER_ID_COLUMN));
            uptPidV4 = cursor.getLong(cursor.getColumnIndex(UserProjectTable.PROJECT_ID_COLUMN));

        }

        System.out.println(uptIdV4 + " " + uptPidV4 + " " + uptUidV4);


        projectV4.setOwner(userV4);
        projectV4.setTeam(Collections.singletonList(userV4));
        issueV4.setTimeEntries(Collections.singletonList(timeEntryV4));
        projectV4.setIssues(Collections.singletonList(issueV4));

        System.out.println("-------------------------");
        System.out.println(projectV4);

        assertEquals(project, projectV4);
        assertEquals(timeEntry, timeEntryV4);
        assertEquals(issue, issueV4);
        assertEquals(user, userV4);
        assertEquals(uptId, uptIdV4);
        assertEquals(uptPid, uptPidV4);
        assertEquals(uptUid, uptUidV4);
    }
}
