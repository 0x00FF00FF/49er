package org.rares.miner49er.projects;

import android.util.Log;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import org.rares.miner49er._abstract.NetworkingService;
import org.rares.miner49er._abstract.Repository;
import org.rares.miner49er.persistence.StorioFactory;
import org.rares.miner49er.persistence.entity.Project;
import org.rares.miner49er.persistence.entity.ProjectTable;
import org.rares.miner49er.projects.model.ProjectData;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectsRepository extends Repository
        implements
        Consumer<List<Project>>

{
    private static final String TAG = ProjectsRepository.class.getSimpleName();

    private StorIOSQLite storio = StorioFactory.INSTANCE.get();
    private NetworkingService ns = NetworkingService.INSTANCE;
    private Flowable<Changes> projectTableObservable;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void setup() {
        Log.d(TAG, "setup() called." + storio.hashCode());
        ns.registerProjectsConsumer(this);
        projectTableObservable =
//                Flowable.merge(
//                        Flowable.just(Changes.newInstance(ProjectTable.NAME)),
                        storio
                                .observeChangesInTable(ProjectTable.NAME, BackpressureStrategy.LATEST);
//                ).doOnNext(d -> Log.i(TAG, "   >>>   : changes happened."));
    }

    @Override
    public void shutdown() {
        Log.w(TAG, "shutdown() called.");
        disposables.dispose();
    }

    private void removeAllProjects() {
        storio.delete()
                .byQuery(DeleteQuery.builder()
                        .table(ProjectTable.NAME)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    private void persistProjects(List<Project> projects) {
        Log.d(TAG, "persistProjects() called with: projects = [ ] + " + storio.hashCode());
        if (Collections.emptyList().equals(projects)) {
            Log.e(TAG, "RECEIVED EMPTY LIST. stopping here.");
            return;
        }
        // TODO: 8/1/18 perhaps a more refined solution would be a JsonAdapter.Factory
        // https://github.com/square/moshi/issues/295
        if (projects.size() == 1 && projects.get(0).getName() == null) {
            Log.e(TAG, "persistProjects: EMPTY LIST FROM SERVER");
            return;
        }
//        StorIOSQLite.LowLevel ll = storio.lowLevel();
//        try {
//            ll.beginTransaction();
//
//            ll.delete(DeleteQuery.builder()
//                    .table(ProjectTable.NAME)
//                    .build());
//
//            ProjectStorIOSQLitePutResolver r = new ProjectStorIOSQLitePutResolver();
//            for (int i = 0; i < projects.size(); i++) {
//                Project p = projects.get(i);
//                if (System.currentTimeMillis() % 2 == 0)
//                    ll.insert(InsertQuery.builder()
//                            .table(ProjectTable.NAME)
//                            .build(), r.mapToContentValues(p));
//                else Log.e(TAG, "persistProjects: SKIPPED " + p.getName());
//            }
//            ll.setTransactionSuccessful();
//        } catch (Exception x) {
//            Log.e(TAG, "persistProjects: ERRRORICAAAA", x);
//        } finally {
//            ll.endTransaction();
//        }
//        Log.w(TAG, "persistProjects: done _______________________________ ");
        storio
                .put()
                .objects(projects)
                .prepare()
                .asRxSingle()
                .subscribe();
    }

    public void registerSubscriber(Consumer<List<ProjectData>> consumer) {
        Log.d(TAG, "registerSubscriber() called with: consumer = [" + consumer + "]");

        disposables.add(projectTableObservable
                .map(change -> {
                    Log.i(TAG, "registerSubscriber: CHANGE >>> ADAPTER");
                    return storio
                            .get()
                            .listOfObjects(Project.class)
                            .withQuery(Query.builder().table(ProjectTable.NAME).build())
                            .prepare()
                            .executeAsBlocking();
                })
//                .flatMap(Flowable::fromIterable)
                .map(this::db2vm)
//                .toSortedList((p1, p2) -> (int) (p1.getId() - p2.getId()))    <- this will never finish|will not emit
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer));
    }

    @Override
    public void accept(List<Project> projects) throws Exception {
        // TODO: compare projects with the in-memory version

        Log.w(TAG, "[][][][][] DISPOSABLES SIZE: " + disposables.size());
        persistProjects(projects);
    }

    private List<ProjectData> db2vm(List<Project> pl) {
        Log.d(TAG, "db2vm() called with: p = [" + pl + "]");
        List<ProjectData> projectDataList = new ArrayList<>();

        for (Project p : pl) {
            ProjectData converted = new ProjectData();

            converted.setName(p.getName());
            converted.setIcon(p.getIcon());
            converted.setId(p.getId());
            converted.setDescription(p.getDescription());
            converted.setDateAdded(p.getDateAdded());
            converted.setPicture(p.getPicture());
            converted.setIcon(p.getIcon());
            converted.setColor(projectsColors[NumberUtils.getRandomInt(0, projectsColors.length - 1)]);
            projectDataList.add(converted);
        }

        return projectDataList;
    }

    private final String[] projectsColors = {
            "#cbbeb5",
            "#e9aac8",
            "#c9aac8",
            "#a9aac8",
            "#96e7cf",
            "#96c7cf",
            "#96a7cf",
            "#baa0a7",
            "#bac0c7",
            "#bae0e7",
            "#b5a1d1",
            "#b5c1d1",
            "#b5e1d1",
            "#dfc6d0",
            "#ffe6d0",
            "#bfa6d0",
            "#ecbcd7",
            "#d8d3e4",
            "#232467",
            "#644783",
            "#0f54ad",
            "#000033",
            "#282531",
            "#383640",
            "#282236",
            "#2a233c",
            "#2e4f70",
            "#44344e",
            "#6c619e",
            "#7070ff"
    };
}
