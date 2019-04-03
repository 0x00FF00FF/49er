package org.rares.miner49er._abstract;

import android.util.Log;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.rares.miner49er.persistence.entities.Issue;
import org.rares.miner49er.persistence.entities.Project;
import org.rares.miner49er.persistence.entities.TimeEntry;
import org.rares.miner49er.persistence.entities.User;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public enum NetworkingService {
    INSTANCE;

    private static final String TAG = NetworkingService.class.getSimpleName();

    interface ProjectsService {

        @GET("users/{userId}/projects")
        Observable<Project> getProjectList(@Path("userId") int userId);

//        @GET("projects_repaired.json")
        @GET("projects.json")
        Single<List<Project>> getProjectList();
    }

    public interface IssuesService {

//        @GET("projects/{projectId}/issues")
//        Single<List<Issue>> getIssueList(@Path("projectId") int projectId);

        @GET("issues.json")
        Single<List<Issue>> getIssueList();

        @GET("issues/{issueId}")
        Observable<Issue> getIssue(@Path("issueId") int issueId);
    }

    public interface TimeEntriesService {

//        @GET("issues/{issueId}/timeentries")
//        Single<List<TimeEntry>> getTimeEntryList(@Path("issueId") int issueId);

        @GET("timeentries.json")
        Single<List<TimeEntry>> getTimeEntryList();

        @HEAD("issues/{issueId}/timeentries")
        Observable<List<TimeEntry>> getChangedTimeEntryList(
                @Path("issueId") int issueId,
                @Query("since") long sinceDate
        );

        @GET("timeentries/{entryId}")
        Observable<TimeEntry> getTimeEntry(@Path("entryId") int timeEntryId);
    }

    public interface UserService {

        @GET("users/{userId}")
        Call<User> getUser(@Path("userId") int userId);

        @GET("projects/{projectId}/users")
        Call<List<User>> getUsers(@Path("projectId") int projectId);
    }

    public enum RestServiceGenerator {
        INSTANCE;

        private final String serviceUrl = "http://192.168.1.114/~rares/";

        private HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

        private OkHttpClient httpClient =
                new OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC))
                        .addNetworkInterceptor(new StethoInterceptor())
                        .readTimeout(5, TimeUnit.SECONDS)
                        .connectTimeout(5, TimeUnit.SECONDS)
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

        public OkHttpClient getHttpClient() {
            return httpClient;
        }

        public Retrofit getRestClient() {
            return restClient;
        }
    }


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
                        .subscribeOn(Schedulers.io());
    }

    public Single<List<Issue>> getIssues(int projectId) {

        Log.i(TAG, "getIssues: " + this.hashCode() + "|" + this.toString());

        long ctm = System.currentTimeMillis();
        IssuesService issuesService = RestServiceGenerator.INSTANCE.generateService(IssuesService.class);
        Log.w(TAG, "create issues service: ----------------- " + (System.currentTimeMillis() - ctm));

        return
                issuesService
                        .getIssueList(/*projectId*/)
                        .onTerminateDetach()
                        .doOnError(e -> Log.e(TAG, "[doOnError] getIssues: ERROR" + e.getLocalizedMessage()))
                        .onErrorResumeNext(x -> {
                            Log.i(TAG, "getIssues: returning empty list.");
                            return Single.just(Collections.emptyList());
                        })
                        .subscribeOn(Schedulers.io());
    }

    public Single<List<TimeEntry>> getTimeEntries(int issueId) {

        Log.i(TAG, "getTimeEntries: " + this.hashCode() + "|" + this.toString());

        long ctm = System.currentTimeMillis();
        TimeEntriesService issuesService = RestServiceGenerator.INSTANCE.generateService(TimeEntriesService.class);
        Log.w(TAG, "create issues service: ----------------- " + (System.currentTimeMillis() - ctm));

        return
                issuesService
                        .getTimeEntryList(/*issueId*/)
                        .onTerminateDetach()
                        .doOnError(e -> Log.e(TAG, "[doOnError] getTimeEntries: ERROR" + e.getLocalizedMessage()))
                        .onErrorResumeNext(x -> {
                            Log.i(TAG, "getTimeEntries: returning empty list.");
                            return Single.just(Collections.emptyList());
                        })
                        .subscribeOn(Schedulers.io());
    }

    private PublishProcessor<Long> onDemandPublisher = PublishProcessor.create();
    private Flowable<Long> onDemandFlowable = onDemandPublisher.subscribeOn(Schedulers.io());
    private Flowable<Long> timerFlowable;// = Flowable.interval(60L, TimeUnit.SECONDS).share();

    private CompositeDisposable disposables = new CompositeDisposable();

    private Single<List<Project>> projectsObs = getProjects();
//    private Single<List<Issue>> issuesObs = getIssues(111);
//    private Single<List<TimeEntry>> timeEntriesObs = getTimeEntries(111);

    private void cleanDisposables() {
        Log.i(TAG, "cleanDisposables: " + disposables.size());
        disposables.dispose();
    }

    public void start() {
        disposables = new CompositeDisposable();
        setTimerInterval(60000);
    }

    public void end() {
        cleanDisposables();
//        timerFlowable = null;
    }

    public void registerProjectsConsumer(Consumer<List<Project>> consumer) {
        // subscribe a [get projects] event to each timer tick.
        Log.i(TAG, "registerProjectsConsumer: >>>> " + consumer);
        disposables.add(
                timerFlowable.subscribe(
                        timer -> disposables.add(projectsObs.subscribe(consumer)))
                // this is designed to function with only one consumer.
                // if we register multiple consumers, the project service
                // will fetch data for each subscribe(consumer) call.
        );
    }

//    public void registerIssuesConsumer(Consumer<List<Issue>> consumer) {
////        Log.d(TAG, "registerProjectsConsumer() called with: consumer = [" + consumer + "]");
//        disposables.add(
//                timerFlowable.subscribe(
//                        timer -> disposables.add(issuesObs.subscribe(consumer)))
//        );
//    }
//
//    public void registerTimeEntriesConsumer(Consumer<List<TimeEntry>> consumer) {
////        Log.d(TAG, "registerProjectsConsumer() called with: consumer = [" + consumer + "]");
//        disposables.add(
//                timerFlowable.subscribe(
//                        timer -> disposables.add(timeEntriesObs.subscribe(consumer)))
//        );
//    }

    public void setTimerInterval(long seconds) {
        Log.w(TAG, "setTimerInterval: " + seconds + " seconds.");
        timerFlowable = Flowable.merge(onDemandFlowable, Flowable.interval(seconds, TimeUnit.SECONDS)).share()
                .subscribeOn(Schedulers.io())
                .doOnNext(x -> Log.i(TAG, ">TICK!<"));
    }

    public void refreshData() {
        Log.i(TAG, "refreshData: ---- " + onDemandPublisher.hasSubscribers());
        onDemandPublisher.onNext(System.currentTimeMillis());
    }

}
