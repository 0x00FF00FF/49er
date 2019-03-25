package org.rares.miner49er.domain.issues.persistence;

import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;

import java.util.ArrayList;
import java.util.List;

public class IssueConverter implements DaoConverter<Issue, IssueData> {

    private DaoConverter<TimeEntry, TimeEntryData> teConverter = DaoConverterFactory.of(TimeEntry.class, TimeEntryData.class);
    private DaoConverter<User, UserData> usrConverter = DaoConverterFactory.of(User.class, UserData.class);

    @Override
    public Issue vmToDm(IssueData viewModel) {
        if (viewModel == null) {
            return null;
        }

        Issue issue = new Issue();
        issue.setId(viewModel.getId());
        issue.setLastUpdated(viewModel.getLastUpdated());
        issue.setName(viewModel.getName());
        issue.setProjectId(viewModel.getParentId());
        issue.setDateAdded(viewModel.getDateAdded());
        issue.setDateDue(viewModel.getDateDue());
        issue.setOwnerId(viewModel.getOwnerId());
        if (viewModel.getOwner() != null) {
            issue.setOwnerId(viewModel.getOwner().id);
            issue.setOwner(usrConverter.vmToDm(viewModel.getOwner()));
        }
        if (viewModel.getTimeEntries() != null) {
            issue.setTimeEntries(teConverter.vmToDm(viewModel.getTimeEntries()));
        }

        return issue;
    }

    @Override
    public IssueData dmToVm(Issue databaseModel) {
        if (databaseModel == null) {
            return null;
        }

        IssueData converted = new IssueData();
        converted.setId(databaseModel.getId());
        converted.setName(databaseModel.getName());
        converted.setLastUpdated(databaseModel.getLastUpdated());
        converted.setDateAdded(databaseModel.getDateAdded());
        converted.setParentId(databaseModel.getProjectId());
        converted.setOwnerId(databaseModel.getOwnerId());
        if (databaseModel.getTimeEntries() != null) {
            converted.setTimeEntries(teConverter.dmToVm(databaseModel.getTimeEntries()));
        }
        if (databaseModel.getOwner() != null) {
            converted.setOwner(usrConverter.dmToVm(databaseModel.getOwner()));
        }
        return converted;
    }

    @Override
    public List<Issue> vmToDm(List<IssueData> viewModelList) {
        if (viewModelList == null) {
            return null;
        }
        ArrayList<Issue> databaseModelList = new ArrayList<>();
        for (IssueData i : viewModelList) {
            databaseModelList.add(vmToDm(i));
        }
        return databaseModelList;
    }

    @Override
    public List<IssueData> dmToVm(List<Issue> databaseModelList) {
        if (databaseModelList == null) {
            return null;
        }

        ArrayList<IssueData> viewModels = new ArrayList<>();
        for (Issue p : databaseModelList) {
            viewModels.add(dmToVm(p));
        }
        return viewModels;
    }


}
