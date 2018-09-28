package org.rares.miner49er._abstract;

import android.annotation.SuppressLint;
import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
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
        Consumer<List<T>> {

    private static final String TAG = Repository.class.getSimpleName();

    protected StorIOSQLite storio = StorioFactory.INSTANCE.get();
    protected NetworkingService ns = NetworkingService.INSTANCE;
    protected CompositeDisposable disposables = new CompositeDisposable();
    protected PublishProcessor<Byte> userActionProcessor = PublishProcessor.create();
    protected Flowable<Byte>
            userActionsObservable =
            userActionProcessor
                    .subscribeOn(Schedulers.io())
                    /*.doOnSubscribe((action) -> refreshData(false))*/;

    protected PublishProcessor<List<T>> demoProcessor = PublishProcessor.create();

    protected abstract Repository<T> setup();

    public abstract Repository<T> registerSubscriber(Consumer<List> consumer);

    protected abstract Repository<T> prepareEntities(List<T> entityList);

    /**
     * Implementation specific clearing of tables.
     * This method is already run inside a transaction
     * so there is no need to create a transaction
     * and/or execute the commands.
     *
     * @param ll the {@link com.pushtorefresh.storio3.sqlite.StorIOSQLite.LowLevel}
     *           that knows about the transaction
     */
    protected abstract Repository<T> clearTables(StorIOSQLite.LowLevel ll);

    public abstract Repository<T> shutdown();

    /**
     * Creates some fake data. <br/>
     */
    protected abstract List<T> initializeFakeData();

    protected Map<Integer, User> usersToAdd = new HashMap<>();
    protected Map<Integer, Issue> issuesToAdd = new HashMap<>();
    protected Map<Integer, Project> projectsToAdd = new HashMap<>();
    protected Map<Integer, TimeEntry> timeEntriesToAdd = new HashMap<>();

    protected UserStorIOSQLitePutResolver userPutResolver = null;
    protected ProjectStorIOSQLitePutResolver projectPutResolver = null;
    protected IssueStorIOSQLitePutResolver issuePutResolver = null;
    protected TimeEntryStorIOSQLitePutResolver timeEntryPutResolver = null;

    private Set<String> affectedTables = new HashSet<>();

    protected ItemViewProperties parentProperties = ItemViewProperties.create(Project.class);

    public Repository<T> setParentProperties(ItemViewProperties ivp) {
        if (ivp.getId() != 0) {
            parentProperties.setId(ivp.getId());
        }
        parentProperties.setItemBgColor(ivp.getItemBgColor());

        refreshQuery();
        return this;
    }

    protected abstract Repository<T> refreshQuery();

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

    protected List<T> getDbItems(Query getQuery, Class<T> resultsClass) {
        if (parentProperties.getId() == -1) {
            return initializeFakeData();
        }
        return storio
                .get()
                .listOfObjects(resultsClass)
                .withQuery(getQuery)
                .prepare()
                .executeAsBlocking();
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

    /**
     * Persists the &lt;entities&gt;ToAdd using
     * storio low level api. protected because
     * it can be called by child classes; final
     * because it should not be overridden by
     * child classes.
     */
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

            Log.w(TAG, "persistProjects: done insert/update _______________________________ " + (System.currentTimeMillis() - s));
        }
    }


    // enqueue refresh data
    public void refreshData(boolean onlyLocal) {
        userActionProcessor.onNext(UiEvent.TYPE_CLICK);
        if (!onlyLocal) {
            ns.refreshData();
        }
    }

    /**
     * Convenience method to show the state of the observables/observers.
     *
     * @return <code>true</code> if the disposables were disposed of.
     * in this case, the disposables should be
     * refreshed and resubscribed if this instance should be reused.
     */
    public boolean isDisposed() {
        return disposables.isDisposed();
    }
}
