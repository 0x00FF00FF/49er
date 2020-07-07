package org.rares.miner49er.network.dto.converter;

import io.reactivex.Single;
import lombok.Builder;
import org.rares.miner49er.network.dto.TimeEntryDto;
import org.rares.miner49er.persistence.dao.GenericEntityDao;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;

import java.util.Collections;

@Builder
public class TimeEntryConverter {
  private final GenericEntityDao<User> userDao;   // use cache

  public TimeEntry toModel(TimeEntryDto timeEntryDto) {
    TimeEntry te = new TimeEntry();
    User user = userDao.getByObjectIdIn(Collections.singletonList(timeEntryDto.getOwnerId())).blockingFirst();
    te.setUserId(user.getId());
    te.setUser(user);
    te.setComments(timeEntryDto.getComments());
    te.setHours(timeEntryDto.getHours());
    te.setWorkDate(timeEntryDto.getWorkDate());
    te.setDateAdded(timeEntryDto.getDateAdded());
    te.setObjectId(timeEntryDto.getId());
    return te;
  }

  public Single<TimeEntry> toModelAsync(TimeEntryDto timeEntryDto) {
    return Single.just(new TimeEntry())
        .flatMap(te -> userDao
            .getByObjectIdIn(Collections.singletonList(timeEntryDto.getOwnerId()))
            .firstOrError()
            .flatMap(user -> {
              te.setUserId(user.getId());
              te.setUser(user);
              te.setComments(timeEntryDto.getComments());
              te.setHours(timeEntryDto.getHours());
              te.setWorkDate(timeEntryDto.getWorkDate());
              te.setDateAdded(timeEntryDto.getDateAdded());
              te.setObjectId(timeEntryDto.getId());
              return Single.just(te);
            }));
  }
}
