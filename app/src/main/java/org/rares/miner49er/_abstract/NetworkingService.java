package org.rares.miner49er._abstract;

import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.rares.miner49er.persistence.entity.Project;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public enum NetworkingService {
    INSTANCE;

    private static final String TAG = NetworkingService.class.getSimpleName();

    interface ProjectsService {
        //        @GET("users/{userId}/projects")
//        Observable<ProjectData> getProjectList(@Path("userId") int userId);
        @GET("projects.json")
//        @GET("noProjects.json")
        Single<List<Project>> getProjectList();
    }

//    public interface IssuesService {
//        @GET("projects/{projectId}/issues")
//        Call<List<IssueData>> getIssueList(@Path("projectId") int projectId);
//
//        @GET("issues/{issueId}")
//        Observable<IssueData> getIssue(@Path("issueId") int issueId);
//    }
//
//    public interface TimeEntriesService {
//        @GET("issues/{issueId}/timeentries")
//        Call<List<TimeEntryData>> getTimeEntryList(@Path("issueId") int issueId);
//
//        @HEAD("issues/{issueId}/timeentries")
//        Observable<List<TimeEntryData>> getChangedTimeEntryList(
//                @Path("issueId") int issueId,
//                @Query("since") long sinceDate
//        );
//
//        @GET("timeentries/{entryId}")
//        Observable<TimeEntryData> getTimeEntry(@Path("entryId") int timeEntryId);
//    }

//    public interface UserService {
//        @GET("users/{userId}")
//        Call<UserData> getUser(@Path("userId") int userId);
//
//        @GET("projects/{projectId}/users")
//        Call<List<UserData>> getUsers(@Path("projectId") int projectId);
//    }

    enum RestServiceGenerator {
        INSTANCE;

        private final String serviceUrl = "http://192.168.1.148/~rares/";

        private HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

        private OkHttpClient httpClient =
                new OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC))
                        .build();

        private Retrofit restClient =
                new Retrofit.Builder()
                        .baseUrl(serviceUrl)
                        .addConverterFactory(MoshiConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .client(httpClient)
                        .build();


        <C> C generateService(Class<C> c) {
            Log.w(TAG, "enum instance:::: " + this.hashCode());
            return restClient.create(c);
        }
    }

    // TODO: 8/1/18 do not kill self on network error
    public Single<List<Project>> getProjects() {

        Log.i(TAG, "getProjects: " + this.hashCode() + "|" + this.toString());

        long ctm = System.currentTimeMillis();
        ProjectsService projectsService = RestServiceGenerator.INSTANCE.generateService(ProjectsService.class);
        Log.w(TAG, "create projects service: ----------------- " + (System.currentTimeMillis() - ctm));

        return
                projectsService
                        .getProjectList()
                        .onTerminateDetach()
                        .doOnError(e -> Log.e(TAG, "[doOnError] getProjects: ERROR" + e.getLocalizedMessage()))
                        .onErrorResumeNext(x -> {
                            Log.i(TAG, "getProjects: returning empty list.");
                            return Single.just(Collections.emptyList());
                        })
                        .subscribeOn(Schedulers.computation());
//                .observeOn(AndroidSchedulers.mainThread())
                /*.subscribe(
                        (List<Project> pdList) -> {
                            Log.d(TAG, "getProjects: " + pdList.size());

                            Observable.fromArray(pdList.toArray(new Project[0]))
                                    .onTerminateDetach()
                                    .subscribeOn(Schedulers.computation())
                                    .subscribe(
                                            (projectData) -> {
                                                Log.i(TAG, "getProjects: > " + projectData.getName());
                                                logList((issue) -> {
                                                    Log.d(TAG, "getProjects: issue: " + issue);
                                                    logList(
                                                            (timeEntry) -> Log.w(TAG, "getProjects: timeEntry: " + timeEntry),
                                                            ((Issue) issue).getTimeEntries());
                                                }, projectData.getIssues());
                                            },
                                            Throwable::printStackTrace,
                                            () -> Log.w(TAG, "Completed.")
                                    );

                        },
                        Throwable::printStackTrace);

        return null;*/
    }

    private Flowable<Long> timerFlowable;// = Flowable.interval(60L, TimeUnit.SECONDS).share();

    private CompositeDisposable disposables = new CompositeDisposable();

    private Single<List<Project>> projectsObs = getProjects();

    private void cleanDisposables() {
        disposables.dispose();
        disposables = new CompositeDisposable();
    }

    public void start() {
        setTimerInterval(2);
    }

    public void end() {
        cleanDisposables();
//        timerFlowable = null;
    }

    public void registerProjectsConsumer(Consumer<List<Project>> consumer) {
        Log.d(TAG, "registerProjectsConsumer() called with: consumer = [" + consumer + "]");
        disposables.add(
                timerFlowable.doOnEach(x-> Log.e(TAG, "registerProjectsConsumer: IM ALIVE!!!! ")).subscribe(
                        timer -> disposables.add(projectsObs.subscribe(consumer)))
        );
    }

    public void registerProjectsConsumer(SingleObserver<List<Project>> consumer) {
        Log.d(TAG, "registerProjectsConsumer() called with: consumer = [" + consumer + "]");
        disposables.add(
                timerFlowable.doOnEach(x-> Log.e(TAG, "registerProjectsConsumer: IM ALIVE!!!! ")).subscribe(
                        timer -> projectsObs.subscribe(consumer))
        );
    }



    public void setTimerInterval(long seconds) {
        Log.w(TAG, "setTimerInterval: " + seconds + " seconds." );
        timerFlowable = Flowable.interval(seconds, TimeUnit.SECONDS).share().doOnNext(x-> Log.i(TAG, ">TICK!<"));
    }

    private void logList(Consumer<Object> c, List l) {
        for (Object o : l) {
            log(c, o);
        }
    }


    private void log(Consumer<Object> c, Object o) {
        try {
            c.accept(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
