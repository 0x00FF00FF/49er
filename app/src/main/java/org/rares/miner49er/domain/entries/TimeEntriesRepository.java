package org.rares.miner49er.domain.entries;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import org.joda.time.DateTime;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er._abstract.UiEvent;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.tables.TimeEntryTable;
import org.rares.miner49er.util.NumberUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeEntriesRepository extends Repository<TimeEntry> {

    private static final String TAG = TimeEntriesRepository.class.getSimpleName();

    private Flowable<Changes> timeEntriesTableObservable;

    private Query timeEntriesQuery = Query.builder()
            .table(TimeEntryTable.NAME)
            .where(TimeEntryTable.ISSUE_ID_COLUMN + " = ? ")
            .whereArgs(parentProperties.getId())
            .build();

    TimeEntriesRepository() {
//        ns.registerTimeEntriesConsumer(this);
//        timeEntriesTableObservable =
//                storio
//                        .observeChangesInTable(TimeEntryTable.NAME, BackpressureStrategy.LATEST)
//                        .subscribeOn(Schedulers.io());

    }

    @Override
    protected TimeEntriesRepository setup() {
        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }

        return this;
    }

    @Override
    public TimeEntriesRepository registerSubscriber(Consumer<List> consumer) {
//        disposables.add(
//                timeEntriesTableObservable
//                        .map(c -> getDbItems(getTimeEntriesQuery(), TimeEntry.class))
//                        .map(list -> db2vm(list, false))
//                        .onErrorResumeNext(Flowable.fromIterable(Collections.emptyList()))
//                        .doOnError((e) -> Log.e(TAG, "registerSubscriber: ", e))
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(consumer));

        disposables.add(
                userActionsObservable
                        .map(c -> getDbItems(getTimeEntriesQuery(), TimeEntry.class))
                        .startWith(getDbItems(getTimeEntriesQuery(), TimeEntry.class))
                        .map(list -> db2vm(list, true))
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
                .whereArgs(parentProperties.getId())
                .build();
        return this;
    }

    @Override
    public void refreshData(boolean onlyLocal) {
        userActionProcessor.onNext(UiEvent.TYPE_CLICK);
    }

    public Query getTimeEntriesQuery() {
//        Log.d(TAG, "getTimeEntriesQuery(): " + parentProperties.getId());
        return timeEntriesQuery;
    }

    private List<TimeEntryData> db2vm(List<TimeEntry> timeEntries, boolean local) {

        List<TimeEntryData> timeEntryDataList = new ArrayList<>();
        int count = 0;
        for (TimeEntry entry : timeEntries) {
            TimeEntryData converted = new TimeEntryData();

            converted.setDateAdded(entry.getDateAdded());
            converted.setUserName((local ? "" : "*") + entry.getUser().getName());
            converted.setId(entry.getId());
            converted.setWorkDate(entry.getWorkDate());
            converted.setComments(entry.getComments());
            converted.setHours(entry.getHours());
            converted.setUserId(entry.getUserId());
            converted.setUserPhoto(entry.getUser().getPhoto());
            converted.setIssueId(entry.getIssueId());
            converted.setColor(
                    UiUtil.getBrighterColor(
                            parentProperties.getItemBgColor(),
                            (count++ % 2 == 0 ? 3 : 4) * 0.0250F));

            timeEntryDataList.add(converted);
        }

        return timeEntryDataList;
    }

    @Override
    protected final List<TimeEntry> initializeFakeData() {

        int entries = NumberUtils.getRandomInt(4, 30);

        List<TimeEntry> sortedData = new ArrayList<>();

        DateTime dt = new DateTime().minusDays(30);

        User u = new User();
        u.setName("Fat Frumos");
        u.setId(14);
        u.setPhoto("");

        for (int i = 0; i < entries; i++) {
            TimeEntry ted = new TimeEntry();
            ted.setId(-1);
            ted.setWorkDate(dt.plusDays(i).getMillis());
            ted.setDateAdded(dt.withDayOfYear(i + 1).getMillis());
            ted.setHours(6);
            ted.setUser(u);
            ted.setUserId(u.getId());
            ted.setComments("pff...");
            ted.setIssueId(-1);
            sortedData.add(ted);
        }

        return sortedData;
    }
}
