package org.rares.miner49er.domain.entries.persistence;

import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.entities.TimeEntry;

import java.util.ArrayList;
import java.util.List;

public class TimeEntryConverter implements DaoConverter<TimeEntry, TimeEntryData> {

    @Override
    public TimeEntry vmToDm(TimeEntryData viewModel) {
        if (viewModel == null) {
            return null;
        }

        TimeEntry dbModel = new TimeEntry();
        dbModel.setId(viewModel.getId());
        dbModel.setLastUpdated(viewModel.getLastUpdated());
        dbModel.setDateAdded(viewModel.getDateAdded());
        dbModel.setWorkDate(viewModel.getWorkDate());
        dbModel.setComments(viewModel.getComments());
        dbModel.setIssueId(viewModel.getParentId());
        dbModel.setHours(viewModel.getHours());
        dbModel.setUserId(viewModel.getUserId());

        return dbModel;
    }

    @Override
    public TimeEntryData dmToVm(TimeEntry databaseModel) {
        if (databaseModel == null) {
            return null;
        }

        TimeEntryData viewModel = new TimeEntryData();
        viewModel.setId(databaseModel.getId());
        viewModel.setLastUpdated(databaseModel.getLastUpdated());
        viewModel.setDateAdded(databaseModel.getDateAdded());
        viewModel.setWorkDate(databaseModel.getWorkDate());
        viewModel.setComments(databaseModel.getComments());
        viewModel.setParentId(databaseModel.getIssueId());
        viewModel.setHours(databaseModel.getHours());
        viewModel.setUserId(databaseModel.getUserId());
        if (databaseModel.getUser() != null) {
            viewModel.setUserPhoto(databaseModel.getUser().getPhoto());
            viewModel.setUserName(databaseModel.getUser().getName());
        }
        return viewModel;
    }

    @Override
    public List<TimeEntry> vmToDm(List<TimeEntryData> viewModelList) {
        return null;
    }

    @Override
    public List<TimeEntryData> dmToVm(List<TimeEntry> databaseModelList) {
        if (databaseModelList == null) {
            return null;
        }

        ArrayList<TimeEntryData> viewModels = new ArrayList<>();
        for (TimeEntry p : databaseModelList) {
            viewModels.add(dmToVm(p));
        }
        return viewModels;
    }


}
