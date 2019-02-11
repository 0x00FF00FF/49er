package org.rares.miner49er.domain.issues.repository;

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
import org.rares.miner49er.cache.InMemoryCacheFactory;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.storio.tables.IssueTable;
import org.rares.miner49er.persistence.storio.tables.TimeEntryTable;
import org.rares.miner49er.util.NumberUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IssuesRepository extends Repository<Issue> {

    private static final String TAG = IssuesRepository.class.getSimpleName();

    private Flowable<Changes> issueTableObservable;
    private Query issuesQuery;

    public IssuesRepository() {
//        ns.registerIssuesConsumer(this);
//        issueTableObservable =
//                storio
//                        .observeChangesInTable(IssueTable.NAME, BackpressureStrategy.LATEST)
//                        .subscribeOn(Schedulers.io());
    }

    @Override
    public void setup() {

        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }

    }

    @Override
    public void shutdown() {
        disposables.dispose();
    }

    @Override
    protected boolean prepareEntities(List<Issue> entityList) {

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

        return false;
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
//                Flowable.merge(
//                        issueTableObservable
//                                .doOnNext((a) -> Log.d(TAG, "NET-- ON NEXT"))
//                                .map(changes -> getDbItems(issuesQuery, Issue.class))
//                                .map(list -> db2vm(list, false)),
                userActionsObservable
                        .doOnNext((a) -> Log.i(TAG, "LOCAL ON NEXT"))
                        .map(event -> getDbItems())
//                        .map(event -> getDbItems(issuesQuery, Issue.class))
                        .startWith(getDbItems())
//                        .map(list -> db2vm(list, true))
                        .onBackpressureBuffer(2, () -> Log.i(TAG, "registerSubscriber: BACK PRESSURE BUFFER"))
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer)
        );

    }

    @Override
    protected void refreshQuery() {
        issuesQuery = Query.builder()
                .table(IssueTable.NAME)
                .where(IssueTable.PROJECT_ID_COLUMN + " = ? ")
                .whereArgs(parentProperties.getId())
                .build();
    }

    protected List<IssueData> getDbItems() {
//        GenericDAO<IssueData> dao = GenericDaoFactory.ofType(IssueData.class);
        GenericDAO<IssueData> dao = InMemoryCacheFactory.from(new IssueData());
        return dao.getAll(parentProperties.getId());
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
            converted.setColor(
                    UiUtil.getBrighterColor(
                            parentProperties.getItemBgColor(),
                            (count++ % 2 == 0 ? 2 : 4) * 0.0200F));
            projectDataList.add(converted);
        }

        return projectDataList;
    }

    @Override
    protected final List<Issue> initializeFakeData() {
        List<Issue> dataList = new ArrayList<>();
        for (int i = 0; i < NumberUtils.getRandomInt(5, 30); i++) {
            Issue data = new Issue();
            data.setId(-1L);
            data.setName("Issue #" + i);
            dataList.add(data);
        }
        return dataList;
    }

}
