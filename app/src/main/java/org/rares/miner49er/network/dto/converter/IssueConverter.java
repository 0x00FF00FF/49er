package org.rares.miner49er.network.dto.converter;

import io.reactivex.Single;
import lombok.Builder;
import org.rares.miner49er.network.dto.IssueDto;
import org.rares.miner49er.persistence.dao.GenericEntityDao;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Builder
public class IssueConverter {
    private final GenericEntityDao<User> userDao;   // use cache

    public Issue toModel(IssueDto issueDto) {
        Issue issue = new Issue();
        issue.setOwner(userDao.getByObjectIdIn(Collections.singletonList(issueDto.getOwnerId())).blockingFirst());
        issue.setOwnerId(issue.getOwner().getId());
        issue.setName(issueDto.getName());
        List<TimeEntry> timeEntries = new ArrayList<>();
        for (String teId : issueDto.getTimeEntries()) {
            TimeEntry te = new TimeEntry();
            te.setObjectId(teId);
            timeEntries.add(te);
        }
        issue.setTimeEntries(timeEntries);
        return new Issue();
    }

    public Single<Issue> toModelAsync(final IssueDto issueDto) {
        return Single.just(new Issue())
                .flatMap(i -> userDao.getByObjectIdIn(Collections.singletonList(issueDto.getOwnerId()))
                        .firstOrError()
                        .flatMap(user -> {
                            i.setOwner(user);
                            i.setOwnerId(user.getId());
                            i.setName(issueDto.getName());
                            List<TimeEntry> timeEntries = new ArrayList<>();
                            for (String teId : issueDto.getTimeEntries()) {
                                TimeEntry te = new TimeEntry();
                                te.setObjectId(teId);
                                timeEntries.add(te);
                            }
                            i.setTimeEntries(timeEntries);
                            i.setObjectId(issueDto.getId());
                            return Single.just(i);
                        }));
    }
}
