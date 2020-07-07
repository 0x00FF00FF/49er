package org.rares.miner49er.cache;

import android.util.LruCache;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.functions.Predicate;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeEntryDataCache implements Cache<TimeEntryData> {

    private final ViewModelCache cache;
    private final LruCache<Long, TimeEntryData> timeEntriesCache;
    private final LruCache<Long, IssueData> issueDataCache;
    private final String TAG = TimeEntryDataCache.class.getSimpleName();

    public TimeEntryDataCache(ViewModelCache cache) {
        this.cache = cache;
        timeEntriesCache = cache.getTimeEntriesLruCache();
        issueDataCache = cache.getIssuesLruCache();
    }

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
        if (ted.parentId == null) {
            return;
        }
        if (link && issueDataCache.get(ted.parentId) == null) {
            link = false;
        }
        if (link) {
            synchronized (issueDataCache.get(ted.parentId)) {
                IssueData issueData = issueDataCache.get(ted.parentId);
                if (issueData != null) {
                    List<TimeEntryData> timeEntries = issueData.getTimeEntries();
                    if (timeEntries != null) {
                        TimeEntryData toReplace = null;
                        for (TimeEntryData timeEntryData : timeEntries) {   // enforce data validity
                            if (timeEntryData.id.equals(ted.id)) {
                                timeEntryData.updateData(ted);
                                toReplace = timeEntryData;
                                break;
                            }
                        }
                        if (toReplace == null) {
                            if (timeEntries.equals(Collections.emptyList())) {
                                timeEntries = new ArrayList<>();
                            }
                            timeEntries.add(ted);
                            issueData.setTimeEntries(timeEntries);
                        } else {
                            timeEntries.remove(toReplace);
                            timeEntries.add(ted);
                        }
                    } else {
                        timeEntries = new ArrayList<>();
                        timeEntries.add(ted);
                        issueData.setTimeEntries(timeEntries);
                    }
                }
            }
        }
        timeEntriesCache.put(ted.id, ted);
        cache.sendEvent(CACHE_EVENT_UPDATE_ENTRY);
    }

    @Override
    public void removeData(TimeEntryData ted) {
        synchronized (issueDataCache.get(ted.parentId)) {
            IssueData issueData = issueDataCache.get(ted.parentId);
            List<TimeEntryData> timeEntryDataList = issueData.getTimeEntries();
            if (timeEntryDataList != null) {
                for (int i = 0; i < timeEntryDataList.size(); i++) {
                    TimeEntryData teData = timeEntryDataList.get(i);
                    if (teData.id.equals(ted.id)) {
                        timeEntryDataList.remove(i);
                        break;
                    }
                }
            }
        }

        timeEntriesCache.remove(ted.id);
        cache.sendEvent(CACHE_EVENT_REMOVE_ENTRY);
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
                if (timeEntries == null) {
                    return null;
                    // // TODO: 28.05.2019 null returned, but if needed, we can use Collections.emptyList()
                    // just remember to return the same thing in IssueDataCache, for consistency
                }
                Collections.sort(timeEntries, (te1, te2) -> te1.id.compareTo(te2.id));
                Collections.reverse(timeEntries);
                return timeEntries;
            }
        }
        return new ArrayList<>(timeEntriesCache.snapshot().values());
    }

    @Override
    public int getSize() {
        return timeEntriesCache.size();
    }
}
