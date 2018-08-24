package org.rares.miner49er.issues;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er._abstract.UiEvent;
import org.rares.miner49er.issues.model.IssueData;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.tables.IssueTable;
import org.rares.miner49er.persistence.tables.TimeEntryTable;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IssuesRepository extends Repository<Issue> {

    private static final String TAG = IssuesRepository.class.getSimpleName();

    private Flowable<Changes> issueTableObservable;
    private Query issuesQuery;

    IssuesRepository() {
//        ns.registerIssuesConsumer(this);
//        issueTableObservable =
//                storio
//                        .observeChangesInTable(IssueTable.NAME, BackpressureStrategy.LATEST)
//                        .subscribeOn(Schedulers.io());
    }

    @Override
    public IssuesRepository setup() {

        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }

        return this;
    }

    @Override
    public IssuesRepository shutdown() {
        disposables.dispose();
        return this;
    }

    @Override
    protected IssuesRepository prepareEntities(List<Issue> entityList) {

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

        return this;
    }

    @Override
    protected IssuesRepository clearTables(StorIOSQLite.LowLevel ll) {
//        for (DeleteQuery query : queries) {
//            Log.i(TAG, "clearTables: " + query);
//            ll.delete(query);
//        }
        return this;
    }

    @Override
    public IssuesRepository registerSubscriber(Consumer<List> consumer) {

        disposables.add(
//                Flowable.merge(
//                        issueTableObservable
//                                .doOnNext((a) -> Log.d(TAG, "NET-- ON NEXT"))
//                                .map(changes -> getDbItems(issuesQuery, Issue.class))
//                                .map(list -> db2vm(list, false)),
                        userActionsObservable
                                .doOnNext((a) -> Log.i(TAG, "LOCAL ON NEXT"))
                                .map(event -> getDbItems(issuesQuery, Issue.class))
                                .startWith(getDbItems(issuesQuery, Issue.class))
                                .map(list -> db2vm(list, true))
//        )
//                        .onBackpressureLatest()
                        .onBackpressureBuffer(2, () -> Log.i(TAG, "registerSubscriber: BACK PRESSURE BUFFER"))
//                        .delay(200, TimeUnit.MILLISECONDS)
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer)
        );

        return this;
    }

    @Override
    protected IssuesRepository refreshQuery() {
        issuesQuery = Query.builder()
                .table(IssueTable.NAME)
                .where(IssueTable.PROJECT_ID_COLUMN + " = ? ")
                .whereArgs(parentProperties.getId())
                .build();
        return this;
    }

    @Override
    public void refreshData(boolean onlyLocal) {
        userActionProcessor.onNext(UiEvent.TYPE_CLICK);
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

    private List<IssueData> db2vm(List<Issue> issues, boolean local) {

//        Log.d(TAG, "db2vm() called with: p = [" + issues + "]");
        List<IssueData> projectDataList = new ArrayList<>();
        int count = 0;
        for (Issue i : issues) {
            IssueData converted = new IssueData();

            converted.setDateAdded(i.getDateAdded());
            converted.setDateDue(i.getDateDue());
            converted.setId(i.getId());
            converted.setName((local ? "" : "*") + i.getName());
            converted.setProjectId(i.getProjectId());
            converted.setColor(parentProperties.getItemBgColor() + (count++ % 2 == 0 ? 1 : -1) * 15);
            projectDataList.add(converted);
        }

        return projectDataList;
    }

    @Override
    protected final List<Issue> initializeFakeData() {
        List<Issue> dataList = new ArrayList<>();
        for (int i = 0; i < NumberUtils.getRandomInt(5, 30); i++) {
            Issue data = new Issue();
            data.setId(-1);
            data.setName("Issue #" + i);
            dataList.add(data);
        }
        return dataList;
    }

}
