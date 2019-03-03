package org.rares.miner49er.domain.entries.persistence;

import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.functions.Predicate;
import org.rares.miner49er.cache.Cache;
import org.rares.miner49er.cache.SimpleCache;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeEntryDataCache implements Cache<TimeEntryData> {

    private final SimpleCache cache = SimpleCache.getInstance();
    private LruCache<Long, TimeEntryData> timeEntriesCache = cache.getTimeEntriesCache();
    private LruCache<Long, IssueData> issueDataCache = cache.getIssuesCache();

    // TODO: 3/2/19 synchronize put

    @Override
    public void putData(List<TimeEntryData> list, Predicate<TimeEntryData> ptCondition, boolean link) {

    }

    @Override
    public void putData(List<TimeEntryData> list, boolean link) {
        for (TimeEntryData ted : list) {
            putData(ted, link);
        }
    }

    @Override
    public void putData(TimeEntryData ted, boolean link) {
        if (link) {
            IssueData issueData = issueDataCache.get(ted.parentId);
            if (issueData != null) {
                List<TimeEntryData> timeEntries = issueData.getTimeEntries();
                if (timeEntries != null) {
                    boolean found = false;
                    for (TimeEntryData timeEntryData : timeEntries) {   // enforce data validity
                        if (timeEntryData.id.equals(ted.id)) {
                            timeEntryData.updateData(ted);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        timeEntries.add(ted);
                    }
                } else {
                    timeEntries = new ArrayList<>();
                    timeEntries.add(ted);
                    issueData.setTimeEntries(timeEntries);
                }
            }
        }
        timeEntriesCache.put(ted.id, ted);
        cache.sendEvent();
    }

    @Override
    public void removeData(TimeEntryData ted) {
        timeEntriesCache.remove(ted.id);
        cache.sendEvent();
    }

    @Override
    public TimeEntryData getData(Long id) {
        return timeEntriesCache.get(id);
    }

    @Override
    public List<TimeEntryData> getData(Optional<Long> parentId) {
        if (parentId.isPresent()) {
            IssueData issueData = issueDataCache.get(parentId.get());
            if (issueData != null) {
                List<TimeEntryData> timeEntries = issueData.getTimeEntries();
                return timeEntries == null ? Collections.emptyList() : timeEntries;
            }
        }
        return new ArrayList<>(timeEntriesCache.snapshot().values());
    }
}
