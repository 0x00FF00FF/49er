package org.rares.miner49er.entries;

import lombok.Setter;
import org.joda.time.DateTime;
import org.rares.miner49er.entries.model.TimeEntryData;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimeEntriesRepository {

    public TimeEntriesRepository() {
    }

    public TimeEntriesRepository(Comparator<TimeEntryData> comparator) {
        this.comparator = comparator;
    }

    public List<TimeEntryData> getTimeEntries() {
        // return from dummy data
        return initializeData(NumberUtils.getRandomInt(5, 40));
        // return from db call
        // return from network call
    }


    // how is the data being sorted at first run using diffUtil?
    private List<TimeEntryData> initializeData(int entries) {
        List<TimeEntryData> sortedData = new ArrayList<>();
        DateTime dt = new DateTime().minusDays(30);
        for (int i = 0; i < entries; i++) {
            TimeEntryData ted = new TimeEntryData();
            ted.setId(NumberUtils.getNextProjectId());
            ted.setUserName("Peter Piper");
            ted.setWorkDate(dt.plusDays(i).getMillis());
            ted.setDateAdded(dt.withDayOfYear(i + 1).getMillis());
            ted.setHours(6);
            sortedData.add(ted);
        }
        TimeEntryData ted = new TimeEntryData();
        ted.setId(NumberUtils.getNextProjectId());
        ted.setUserName("Fat Frumos");
        ted.setWorkDate(dt.plusDays(16).minusYears(1).getMillis());
        ted.setDateAdded(dt.withDayOfYear(4 + 1).getMillis());
        ted.setHours(-6);
        sortedData.add(ted);

//        sortedData.sort(Comparator.comparing(TimeEntryData::getWorkDate));
        Collections.sort(sortedData, comparator);

        return sortedData;
    }


    private final Comparator<TimeEntryData> defaultComparator = (t1, t2) -> (int) (t1.getWorkDate() - t2.getWorkDate());

    @Setter
    private Comparator<TimeEntryData> comparator = defaultComparator;

    public void useDefaultComparator() {
        comparator = defaultComparator;
    }
}
