package org.rares.miner49er.issues;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er.issues.model.IssueData;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.tables.IssueTable;
import org.rares.miner49er.persistence.tables.TimeEntryTable;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class IssuesRepository extends Repository<Issue> {

    private static final String TAG = IssuesRepository.class.getSimpleName();

    private Flowable<Changes> issueTableObservable;
//    private List<DeleteQuery> queries = new ArrayList<>();


    @Override
    public void setup() {
        Log.d(TAG, "setup() called." + storio.hashCode());

        disposables = new CompositeDisposable();
//        ns.registerIssuesConsumer(this);
        issueTableObservable =
                storio
                        .observeChangesInTable(IssueTable.NAME, BackpressureStrategy.LATEST)
                        .subscribeOn(Schedulers.io())
                        .doOnNext(d -> Log.i(TAG, "   >>>   : changes happened inside the issues table."));
    }

    @Override
    public void shutdown() {
        disposables.dispose();
    }

    @Override
    protected void prepareEntities(List<Issue> entityList) {

        for (Issue i : entityList) {
            if (i.getTimeEntries() != null) {

//                DeleteQuery teQuery = newTimeEntriesQuery().whereArgs(i.getId()).build();
//                queries.add(teQuery);

                for (TimeEntry te : i.getTimeEntries()) {
                    if (te.getUserId() == 0) {
                        te.setUserId(te.getUser().getId());
                    }
                    if (te.getIssueId() == 0) {
                        te.setIssueId(te.getIssue().getId());
                    }
                    timeEntriesToAdd.put(te.getId(), te);
                }
            }

            if (i.getOwnerId() == 0) {
                i.setOwnerId(i.getOwner().getId());
            }

            if (i.getProjectId() == 0) {
                i.setProjectId(i.getProject().getId());
            }

//            DeleteQuery issuesQuery = newIssuesQuery().whereArgs(i.getProjectId()).build();
//            queries.add(issuesQuery);

            if (!usersToAdd.keySet().contains(i.getOwnerId())) {
                usersToAdd.put(i.getOwner().getId(), i.getOwner());
//                DeleteQuery usersQuery =
//                        DeleteQuery.builder()
//                                .table(UserTable.NAME)
//                                .where(UserTable.ID_COLUMN + " = ? ")
//                                .whereArgs(i.getOwnerId()).build();
//                queries.add(usersQuery);
            }
            issuesToAdd.put(i.getId(), i);
        }

        storio.put()
                .objects(usersToAdd.values())
                .withPutResolver(userPutResolver)
                .prepare()
                .executeAsBlocking();

        storio.put()
                .objects(issuesToAdd.values())
                .withPutResolver(issuePutResolver)
                .prepare()
                .executeAsBlocking();

        storio.put()
                .objects(timeEntriesToAdd.values())
                .withPutResolver(timeEntryPutResolver)
                .prepare()
                .asRxCompletable()
                .subscribe();
    }

    @Override
    protected void clearTables(StorIOSQLite.LowLevel ll) {
//        for (DeleteQuery query : queries) {
//            Log.i(TAG, "clearTables: " + query);
//            ll.delete(query);
//        }
    }

    @Override
    public void registerSubscriber(Consumer<List> consumer) {
        disposables.add(
                issueTableObservable
                        .doOnNext(x -> Log.i(TAG, "registerSubscriber: change: " + x.affectedTables()))
                        .map(changes -> storio
                                .get()
                                .listOfObjects(Issue.class)
                                .withQuery(Query.builder()
                                        .table(IssueTable.NAME)
                                        .where(IssueTable.PROJECT_ID_COLUMN + " = ? ")
                                        .whereArgs(parentId)
                                        .build())
                                .prepare()
                                .executeAsBlocking())
                        .startWith(initializeFakeData())
                        .map(this::db2vm)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));
    }


    private DeleteQuery.CompleteBuilder newTimeEntriesQuery() {
        return DeleteQuery.builder()
                .table(TimeEntryTable.NAME)
                .where(TimeEntryTable.ISSUE_ID_COLUMN + " = ? ");
    }

    private DeleteQuery.CompleteBuilder newIssuesQuery() {
        return DeleteQuery.builder()
                .table(IssueTable.NAME)
                .where(IssueTable.PROJECT_ID_COLUMN + " = ? ");
    }

    private List<Issue> initializeFakeData() {
        List<Issue> dataList = new ArrayList<>();
        for (int i = 0; i < NumberUtils.getRandomInt(5, 30); i++) {
            Issue data = new Issue();
            data.setId(NumberUtils.getNextProjectId());
            data.setName("Issue #" + i);
            dataList.add(data);
        }
        return dataList;
    }

    private int counter = 0;

    private List<IssueData> db2vm(List<Issue> issues) {

        boolean addStar = false;
        if (++counter % 2 == 0) {
            addStar = true;
        }
        if (counter > 10) {
            counter = 0;
        }

        Log.d(TAG, "db2vm() called with: p = [" + issues + "]");
        List<IssueData> projectDataList = new ArrayList<>();

        for (Issue i : issues) {
            IssueData converted = new IssueData();

            converted.setDateAdded(i.getDateAdded());
            converted.setDateDue(i.getDateDue());
            converted.setId(i.getId());
            converted.setName(i.getName() + (addStar ? " *" : ""));
            converted.setProjectId(i.getProjectId());
            projectDataList.add(converted);
        }

        return projectDataList;
    }
}
