package org.rares.miner49er.domain.users.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.StorioFactory;
import org.rares.miner49er.persistence.storio.resolvers.UserStorIOSQLiteGetResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsersDAO implements GenericDAO<UserData> {
    private StorIOSQLite storIOSQLite;

    public static UsersDAO newInstance() {
        return new UsersDAO();
    }

    private UsersDAO() {
        storIOSQLite = StorioFactory.INSTANCE.get();
    }

    @Override
    public List<UserData> getAll() {
        List<User> users = UserStorIOSQLiteGetResolver.getAll(storIOSQLite);
        return convertDbModelList(users);
    }

    @Override
    public List<UserData> getMatching(String term) {
        List<User> users = UserStorIOSQLiteGetResolver.getMatchingName(storIOSQLite, term);
        return convertDbModelList(users);
    }

    public List<UserData> getByRole(int role) {
        if (role < 10 || role > 12) {
            return Collections.emptyList();
        }
        List<User> users = UserStorIOSQLiteGetResolver.getMatchingRole(storIOSQLite, role);
        return convertDbModelList(users);
    }

    @Override
    public UserData get(long id) {
        User user = UserStorIOSQLiteGetResolver.getById(storIOSQLite, id);
        return convertDbModel(user);
    }

    @Override
    public long insert(UserData toInsert) {
        assertInsertReady(toInsert);
        return storIOSQLite
                .put()
                .object(toInsert)
                .prepare()
                .executeAsBlocking()
                .insertedId();
    }

    @Override
    public void update(UserData toUpdate) {
        assertUpdateReady(toUpdate);
        boolean updateSuccess = storIOSQLite.put()
                .object(convertViewModel(toUpdate))
                .prepare()
                .executeAsBlocking()
                .wasUpdated();
        Log.d(TAG, "updated: " + toUpdate.getId() + ": " + updateSuccess);
    }

    @Override
    public void delete(UserData toDelete) {
        assertDeleteReady(toDelete);

        int deletedRows = storIOSQLite.delete()
                .object(convertViewModel(toDelete))
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted();
        Log.d(TAG, "delete: deleted rows: " + deletedRows);
    }

    public static List<UserData> convertDbModelList(List<User> entities) {
        if (entities == null) {
            return null;
        }

        ArrayList<UserData> viewModels = new ArrayList<>();
        for (User p : entities) {
            viewModels.add(convertDbModel(p));
        }
        return viewModels;
    }

    public static List<User> convertViewModelList(List<UserData> viewModels) {
        if (viewModels == null) {
            return null;
        }

        ArrayList<User> entities = new ArrayList<>();
        for (UserData data : viewModels) {
            entities.add(convertViewModel(data));
        }
        return entities;
    }

    public static UserData convertDbModel(User entity) {
        if (entity == null) {
            return null;
        }

        Log.i(TAG, "convertDbModel: >>>> " + entity.toString());

        UserData converted = new UserData();
        converted.setId(entity.getId());
        converted.setName(entity.getName());
        converted.setEmail(entity.getEmail());
        converted.setApiKey(entity.getApiKey());
        converted.setPicture(entity.getPhoto());
        converted.setRole(entity.getRole());
        converted.setLastUpdated(entity.getLastUpdated());

        return converted;
    }

    public static User convertViewModel(UserData viewData) {
        if (viewData == null) {
            return null;
        }

        User user = new User();
        user.setName(viewData.getName());
        user.setId(viewData.getId());
        user.setEmail(viewData.getEmail());
        user.setApiKey(viewData.getApiKey());
        user.setPhoto(viewData.getPicture());
        user.setRole(viewData.getRole());
        user.setLastUpdated(viewData.getLastUpdated());
        return user;
    }
}
