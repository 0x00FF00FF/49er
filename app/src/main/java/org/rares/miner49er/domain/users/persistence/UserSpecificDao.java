package org.rares.miner49er.domain.users.persistence;

import com.pushtorefresh.storio3.Optional;
import io.reactivex.Single;
import org.rares.miner49er.domain.users.model.UserData;

public interface UserSpecificDao {

    Single<Optional<UserData>> getByEmail(String email);
}
