package org.rares.miner49er.entries;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.joda.time.DateTime;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.tables.TimeEntryTable;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeEntriesRepository extends Repository<TimeEntry> {

    private static final String TAG = TimeEntriesRepository.class.getSimpleName();

    private Flowable<Changes> timeEntriesTableObservable;

    private Query timeEntriesQuery = Query.builder()
            .table(TimeEntryTable.NAME)
            .where(TimeEntryTable.ISSUE_ID_COLUMN + " = ? ")
            .whereArgs(parentId)
            .build();

    @Override
    protected TimeEntriesRepository setup() {
//        Log.d(TAG, "setup() called." + storio.hashCode());

        disposables = new CompositeDisposable();
//        ns.registerTimeEntriesConsumer(this);
        timeEntriesTableObservable =
                storio
                        .observeChangesInTable(TimeEntryTable.NAME, BackpressureStrategy.LATEST)
                        .subscribeOn(Schedulers.io());
//                        .doOnNext(d -> Log.i(TAG, "   >>>   : changes happened inside the time entries table."));

        return this;
    }

    @Override
    public TimeEntriesRepository registerSubscriber(Consumer<List> consumer) {
        disposables.add(
                timeEntriesTableObservable
//                        .doOnNext(x -> Log.i(TAG, "registerSubscriber: change: " + x.affectedTables()))
                        .map(c -> getDbItems(getTimeEntriesQuery(), TimeEntry.class))
                        .map(this::db2vm)
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));

        disposables.add(
                userActionsObservable
                        .map(c -> getDbItems(getTimeEntriesQuery(), TimeEntry.class))
                        .map(this::db2vm)
                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(consumer));

        return this;
    }

    @Override
    protected TimeEntriesRepository prepareEntities(List<TimeEntry> entityList) {
        for (TimeEntry te : entityList) {
            if (te.getUserId() == 0) {
                te.setUserId(te.getUser().getId());
            }
            if (te.getIssueId() == 0) {
                te.setIssueId(te.getIssue().getId());
            }
            timeEntriesToAdd.put(te.getId(), te);
            if (!usersToAdd.keySet().contains(te.getUserId())) {
                usersToAdd.put(te.getUserId(), te.getUser());
            }
        }

        storio
                .put()
                .objects(usersToAdd.values())
                .withPutResolver(userPutResolver)
                .prepare()
                .executeAsBlocking();

        storio
                .put()
                .objects(timeEntriesToAdd.values())
                .withPutResolver(timeEntryPutResolver)
                .prepare()
                .asRxCompletable()
                .subscribe();

        return this;
    }

    @Override
    protected TimeEntriesRepository clearTables(StorIOSQLite.LowLevel ll) {
        return this;
    }

    @Override
    public TimeEntriesRepository shutdown() {
        disposables.dispose();
        return this;
    }

    @Override
    protected TimeEntriesRepository refreshQuery() {
        timeEntriesQuery = Query.builder()
                .table(TimeEntryTable.NAME)
                .where(TimeEntryTable.ISSUE_ID_COLUMN + " = ? ")
                .whereArgs(parentId)
                .build();
        return this;
    }

    public Query getTimeEntriesQuery() {
        Log.d(TAG, "getTimeEntriesQuery(): " + parentId);
        return timeEntriesQuery;
    }

    private List<TimeEntry> initializeFakeData() {

        int entries = NumberUtils.getRandomInt(4, 30);

        List<TimeEntry> sortedData = new ArrayList<>();

        DateTime dt = new DateTime().minusDays(30);

        User u = new User();
        u.setName("Fat Frumos");
        u.setId(14);

        for (int i = 0; i < entries; i++) {
            TimeEntry ted = new TimeEntry();
            ted.setId(NumberUtils.getNextProjectId());
            ted.setWorkDate(dt.plusDays(i).getMillis());
            ted.setDateAdded(dt.withDayOfYear(i + 1).getMillis());
            ted.setHours(6);
            ted.setUser(u);
            ted.setUserId(u.getId());
            sortedData.add(ted);
        }

        return sortedData;
    }

    private int counter = 0;

    private List<TimeEntryData> db2vm(List<TimeEntry> timeEntries) {

        Log.i(TAG, "db2vm: called." + timeEntries.size());

        boolean addStar = false;
        if (++counter % 2 == 0) {
            addStar = true;
        }
        if (counter > 10) {
            counter = 0;
        }

        List<TimeEntryData> timeEntryDataList = new ArrayList<>();

        for (TimeEntry entry : timeEntries) {
            TimeEntryData converted = new TimeEntryData();

            converted.setDateAdded(entry.getDateAdded());
            converted.setUserName((addStar ? "*" : "") + entry.getUser().getName());
            converted.setId(entry.getId());
            converted.setWorkDate(entry.getWorkDate());
            converted.setComments(entry.getComments());
            converted.setHours(entry.getHours());
            converted.setUserId(entry.getUserId());
            converted.setUserPhoto(entry.getUser().getPhoto());
            converted.setIssueId(entry.getIssueId());

            timeEntryDataList.add(converted);
        }

        return timeEntryDataList;
    }
}
