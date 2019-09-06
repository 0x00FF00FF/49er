package org.rares.miner49er.domain.users.persistence;

import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.entities.User;

import java.util.ArrayList;
import java.util.List;

public class UserConverter implements DaoConverter<User, UserData> {

    @Override
    public User vmToDm(UserData viewModel) {
        if (viewModel == null) {
            return null;
        }
        User user = new User();
        user.setName(viewModel.getName());
        user.setId(viewModel.getId());
        user.setEmail(viewModel.getEmail());
        user.setApiKey(viewModel.getApiKey());
        user.setPhoto(viewModel.getPicture());
        user.setRole(viewModel.getRole());
        user.setLastUpdated(viewModel.getLastUpdated());
        user.setObjectId(viewModel.getObjectId());
        user.setActive(viewModel.isActive() ? 1 : 0);
        return user;
    }

    @Override
    public UserData dmToVm(User databaseModel) {
        if (databaseModel == null) {
            return null;
        }

        UserData converted = new UserData();
        converted.setId(databaseModel.getId());
        converted.setName(databaseModel.getName());
        converted.setEmail(databaseModel.getEmail());
        converted.setApiKey(databaseModel.getApiKey());
        converted.setPicture(databaseModel.getPhoto());
        converted.setRole(databaseModel.getRole());
        converted.setLastUpdated(databaseModel.getLastUpdated());
        converted.setActive(databaseModel.getActive() == 1);
        converted.setObjectId(databaseModel.getObjectId());

        return converted;
    }

    @Override
    public List<User> vmToDm(List<UserData> viewModelList) {
        if (viewModelList == null) {
            return null;
        }

        ArrayList<User> entities = new ArrayList<>();
        for (UserData data : viewModelList) {
            entities.add(vmToDm(data));
        }
        return entities;
    }

    @Override
    public List<UserData> dmToVm(List<User> databaseModelList) {
        if (databaseModelList == null) {
            return null;
        }

        ArrayList<UserData> viewModels = new ArrayList<>();
        for (User p : databaseModelList) {
            viewModels.add(dmToVm(p));
        }
        return viewModels;
    }


}
