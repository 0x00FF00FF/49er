package org.rares.miner49er._abstract;

import android.annotation.SuppressLint;
import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import lombok.Setter;
import org.rares.miner49er.persistence.StorioFactory;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.resolvers.IssueStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.resolvers.ProjectStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.resolvers.TimeEntryStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.resolvers.UserStorIOSQLitePutResolver;
import org.rares.miner49er.persistence.tables.IssueTable;
import org.rares.miner49er.persistence.tables.ProjectsTable;
import org.rares.miner49er.persistence.tables.TimeEntryTable;
import org.rares.miner49er.persistence.tables.UserTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressLint("UseSparseArrays")
public abstract class Repository<T>
        implements
        Consumer<List<T>>
{

    private static final String TAG = Repository.class.getSimpleName();

    protected StorIOSQLite storio = StorioFactory.INSTANCE.get();
    protected NetworkingService ns = NetworkingService.INSTANCE;
    protected CompositeDisposable disposables = new CompositeDisposable();

    protected abstract void setup();

    public abstract void registerSubscriber(Consumer<List> consumer);

    protected abstract void prepareEntities(List<T> entityList);

    /**
     * Implementation specific clearing of tables.
     * This method is already run inside a transaction
     * so there is no need to create a transaction
     * and/or execute the commands.
     *
     * @param ll the {@link com.pushtorefresh.storio3.sqlite.StorIOSQLite.LowLevel}
     *           that knows about the transaction
     */
    protected abstract void clearTables(StorIOSQLite.LowLevel ll);

    public abstract void shutdown();

    protected Map<Integer, User> usersToAdd = new HashMap<>();
    protected Map<Integer, Issue> issuesToAdd = new HashMap<>();
    protected Map<Integer, Project> projectsToAdd = new HashMap<>();
    protected Map<Integer, TimeEntry> timeEntriesToAdd = new HashMap<>();

    protected UserStorIOSQLitePutResolver userPutResolver = null;
    protected ProjectStorIOSQLitePutResolver projectPutResolver = null;
    protected IssueStorIOSQLitePutResolver issuePutResolver = null;
    protected TimeEntryStorIOSQLitePutResolver timeEntryPutResolver = null;

    private Set<String> affectedTables = new HashSet<>();

    @Setter
    protected int parentId = 0;

    private void insertIssue(IssueStorIOSQLitePutResolver putResolver, StorIOSQLite.LowLevel ll, Issue entity) {
        ll.insert(putResolver.mapToInsertQuery(entity), putResolver.mapToContentValues(entity));
    }

    private void insertTimeEntry(TimeEntryStorIOSQLitePutResolver putResolver, StorIOSQLite.LowLevel ll, TimeEntry entity) {
        ll.insert(putResolver.mapToInsertQuery(entity), putResolver.mapToContentValues(entity));
    }

    private void insertProject(ProjectStorIOSQLitePutResolver putResolver, StorIOSQLite.LowLevel ll, Project entity) {
        ll.insert(putResolver.mapToInsertQuery(entity), putResolver.mapToContentValues(entity));
    }

    private void insertUser(UserStorIOSQLitePutResolver putResolver, StorIOSQLite.LowLevel ll, User entity) {
        ll.insert(putResolver.mapToInsertQuery(entity), putResolver.mapToContentValues(entity));
    }

    @Override
    public void accept(List<T> list) throws Exception {
        Single<List<T>> persistSingle = Single.just(list).subscribeOn(Schedulers.io());
        Disposable persistDisposable = persistSingle.subscribe(this::prepareEntities);

        disposables.add(persistDisposable);
    }

    protected void configureAffectedTables(Set<String> affectedTables) {
        affectedTables.clear();
        if (issuesToAdd.size() > 0) {
            affectedTables.add(IssueTable.NAME);
        }
        if (usersToAdd.size() > 0) {
            affectedTables.add(UserTable.NAME);
        }
        if (timeEntriesToAdd.size() > 0) {
            affectedTables.add(TimeEntryTable.NAME);
        }
        if (projectsToAdd.size() > 0) {
            affectedTables.add(ProjectsTable.TABLE_NAME);
        }
    }

    protected final void persistEntities() {

        long s = System.currentTimeMillis();

        StorIOSQLite.LowLevel ll = storio.lowLevel();

        SQLiteTypeMapping<User> userTypeMapping = ll.typeMapping(User.class);
        SQLiteTypeMapping<Project> projectTypeMapping = ll.typeMapping(Project.class);
        SQLiteTypeMapping<Issue> issueTypeMapping = ll.typeMapping(Issue.class);
        SQLiteTypeMapping<TimeEntry> timeEntryTypeMapping = ll.typeMapping(TimeEntry.class);

        if (userTypeMapping != null) {
            userPutResolver = (UserStorIOSQLitePutResolver) userTypeMapping.putResolver();
        }
        if (projectTypeMapping != null) {
            projectPutResolver = (ProjectStorIOSQLitePutResolver) projectTypeMapping.putResolver();
        }
        if (issueTypeMapping != null) {
            issuePutResolver = (IssueStorIOSQLitePutResolver) issueTypeMapping.putResolver();
        }
        if (timeEntryTypeMapping != null) {
            timeEntryPutResolver = (TimeEntryStorIOSQLitePutResolver) timeEntryTypeMapping.putResolver();
        }

        ll.beginTransaction();

        try {

            clearTables(ll);

            for (User user : usersToAdd.values()) {
                insertUser(userPutResolver, ll, user);
            }

            for (TimeEntry timeEntry : timeEntriesToAdd.values()) {
                insertTimeEntry(timeEntryPutResolver, ll, timeEntry);
            }

            for (Issue issue : issuesToAdd.values()) {
                insertIssue(issuePutResolver, ll, issue);
            }

            for (Project project : projectsToAdd.values()) {
                insertProject(projectPutResolver, ll, project);
            }

            ll.setTransactionSuccessful();

            configureAffectedTables(affectedTables);
            ll.notifyAboutChanges(Changes.newInstance(affectedTables));
        } catch (Exception x) {
            Log.e(TAG, "persistProjects: ERRRORICAAAA", x);
        } finally {
            ll.endTransaction();

            usersToAdd.clear();
            issuesToAdd.clear();
            timeEntriesToAdd.clear();
            projectsToAdd.clear();

//            Log.w(TAG, "persistProjects: done insert/update _______________________________ " + (System.currentTimeMillis() - s));
        }
    }
}
