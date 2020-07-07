package org.rares.miner49er.domain.entries.ui.actions;

import android.util.Log;
import com.pushtorefresh.storio3.Optional;
import lombok.Builder;
import org.joda.time.DateTime;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.custom.functions.NoErrorPredicate;

import java.util.List;

/**
 * Validation for max hours entered by a person. <br />
 *
 * <ul>Fields:
 * <li><code>dao</code> - a {@link org.rares.miner49er.persistence.dao.GenericEntityDao
 * GenericEntityDao}&lt;TimeEntryData&gt; used to get the already inserted time entries</li>
 * <li><code>maxHours</code> - the maximum amount of hours one could enter in a day (int)</li>
 * <li><code>ted</code> - a {@link TimeEntryData} that needs to contain user id and an amount
 * of hours; if the time entry contains an id, then the validation assumes it's an edit and
 * only takes into account the new (edited) number of hours</li>
 * </ul>
 */
@Builder
public class HoursPerDayValidation {
    private static final String TAG = HoursPerDayValidation.class.getSimpleName();
    private AsyncGenericDao<TimeEntryData> dao;
    private int maxHours;
    private TimeEntryData timeEntryData;

    /**
     * The predicate that tests the date/hours for validity.
     * @return <code>true</code> if the hours are in range,
     * <code>false</code> if the hours are over the limit
     */
    public NoErrorPredicate<Long> validation() {
        if (dao == null || maxHours == 0 || timeEntryData == null || timeEntryData.getUserId() == null) {
            throw new IllegalStateException("Validation not set up correctly.");
        }
        long ownerId = timeEntryData.getUserId();
        return date -> {
            DateTime dateTime = new DateTime(date);
            DateTime startOfDay = dateTime.withHourOfDay(0)
                    .withMinuteOfHour(0)
                    .withSecondOfMinute(0)
                    .withMillisOfSecond(0);
            DateTime endOfDay = dateTime.withHourOfDay(23)
                    .withMinuteOfHour(59)
                    .withSecondOfMinute(59)
                    .withMillisOfSecond(999);

            long start = startOfDay.toDateTime().getMillis();
            long end = endOfDay.toDateTime().getMillis();

            List<? extends AbstractViewModel> entities =
                    dao.getMatching(
                            start + " " + end + " " + ownerId,
                            Optional.of(null), true).blockingGet();
            if (entities == null || entities.isEmpty()) {
                return true;
            }
            int totalHours = timeEntryData.getHours();
            for (AbstractViewModel avm : entities) {
                TimeEntryData ted = (TimeEntryData) avm;
                if(!ted.id.equals(timeEntryData.id)) {
                    totalHours += ted.getHours();
                }
            }
            Log.i(TAG, "validation: current: " + timeEntryData.getHours() + " total: " + totalHours + " max: " + maxHours);
            return totalHours <= maxHours;
        };
    }
}
