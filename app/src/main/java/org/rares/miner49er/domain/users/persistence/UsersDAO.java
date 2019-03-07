package org.rares.miner49er.domain.users.persistence;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.dao.converters.DaoConverter;
import org.rares.miner49er.persistence.dao.converters.DaoConverterFactory;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.StorioFactory;

import java.util.Collections;
import java.util.List;

@Deprecated
public class UsersDAO implements GenericDAO<UserData> {
    private StorIOSQLite storIOSQLite;

    public static final String TAG = UsersDAO.class.getSimpleName();
    private DaoConverter<User, UserData> daoConverter = DaoConverterFactory.of(User.class, UserData.class);

    public static UsersDAO newInstance() {
        return new UsersDAO();
    }

    private UsersDAO() {
        storIOSQLite = StorioFactory.INSTANCE.get();
    }

    @Override
    public List<UserData> getAll(boolean lazy) {
        List<User> users = StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver().getAll(storIOSQLite);
        return daoConverter.dmToVm(users);
    }

    @Override
    public List<UserData> getAll(long parentId, boolean lazy) {
        return Collections.emptyList();
    }

    @Override
    public List<UserData> getMatching(String term, boolean lazy) {
        List<User> users = StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver().getMatchingName(storIOSQLite, term);
        return daoConverter.dmToVm(users);
    }

    public List<UserData> getByRole(int role) {
        if (role < 10 || role > 12) {
            return Collections.emptyList();
        }
        List<User> users = StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver().getMatchingRole(storIOSQLite, role);
        return daoConverter.dmToVm(users);
    }

    @Override
    public UserData get(long id, boolean lazy) {
        User user = StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver().getById(storIOSQLite, id);
        return daoConverter.dmToVm(user);
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
                .object(daoConverter.vmToDm(toUpdate))
                .prepare()
                .executeAsBlocking()
                .wasUpdated();
        Log.d(TAG, "updated: " + toUpdate.getId() + ": " + updateSuccess);
    }

    @Override
    public void delete(UserData toDelete) {
        assertDeleteReady(toDelete);

        int deletedRows = storIOSQLite.delete()
                .object(daoConverter.vmToDm(toDelete))
                .prepare()
                .executeAsBlocking()
                .numberOfRowsDeleted();
        Log.d(TAG, "delete: deleted rows: " + deletedRows);
    }

}
