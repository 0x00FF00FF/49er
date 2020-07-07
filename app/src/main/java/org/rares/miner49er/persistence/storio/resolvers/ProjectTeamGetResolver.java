package org.rares.miner49er.persistence.storio.resolvers;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.User;
import org.rares.miner49er.persistence.storio.StorioFactory;

import java.util.List;

public class ProjectTeamGetResolver extends ProjectStorIOSQLiteGetResolver {

  private static final String TAG = ProjectTeamGetResolver.class.getSimpleName();

  @NonNull
  @Override
  public Project mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
//    Log.d(TAG, String.format("mapFromCursor() called from: %s", Thread.currentThread().getName()));
    Project project = super.mapFromCursor(storIOSQLite, cursor);

    List<User> team = StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver().getAll(storIOSQLite, project.getId());
    project.setTeam(team);

    return project;
  }

//  @Override
//  public Single<List<Project>> getAllAsync(StorIOSQLite storIOSQLite) {
//    UserStorIOSQLiteGetResolver getResolver = StorioFactory.INSTANCE.getUserStorIOSQLiteGetResolver();
//    return super.getAllAsync(storIOSQLite)
//        .flatMapPublisher(Flowable::fromIterable)
//        .map(p -> {
//          p.setTeam(getResolver.getAll(storIOSQLite, p.getId()));
//          return p;
//        })
//        .toList();
//  }

  protected LazyProjectGetResolver getInstance() {
    return this;
  }
}
