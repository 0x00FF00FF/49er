package org.rares.miner49er.network;

import android.util.Log;
import io.reactivex.disposables.CompositeDisposable;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.network.rest.AuthenticationInterceptor;
import org.rares.miner49er.network.rest.IssuesService;
import org.rares.miner49er.network.rest.ProjectsService;
import org.rares.miner49er.network.rest.TimeEntriesService;
import org.rares.miner49er.network.rest.UserService;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

import java.util.concurrent.TimeUnit;

//import retrofit2.converter.jackson.JacksonConverterFactory;

public enum NetworkingService {
  INSTANCE;

  private static final String TAG = NetworkingService.class.getSimpleName();

  public enum RestServiceGenerator {
    INSTANCE;

    public final String serviceUrl = "http://192.168.1.111:8080/";

    private HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    @Getter
    private AuthenticationInterceptor authIntercept =
        new AuthenticationInterceptor("", InMemoryCacheAdapterFactory.ofType(UserData.class));

    private OkHttpClient httpClient =
        new OkHttpClient.Builder()
            .addInterceptor(authIntercept)
//            .addInterceptor(loggingInterceptor.setLevel(Level.BODY))
//            .addNetworkInterceptor(new StethoInterceptor())
            .readTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build();

    private Retrofit restClient =
        new Retrofit.Builder()
            .baseUrl(serviceUrl)
            .addConverterFactory(MoshiConverterFactory.create())
//                        .addConverterFactory(JacksonConverterFactory.create())
            // jackson converter does not fail when using flowable + json stream
            // but it only takes the first object
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient)
            .build();


    <C> C generateService(Class<C> c) {
      return restClient.create(c);
    }

    public OkHttpClient getHttpClient() {
      return httpClient;
    }

    public Retrofit getRestClient() {
      return restClient;
    }
  }

  public final ProjectsService projectsService = RestServiceGenerator.INSTANCE.generateService(ProjectsService.class);
  public final IssuesService issuesService = RestServiceGenerator.INSTANCE.generateService(IssuesService.class);
  public final TimeEntriesService timeEntriesService = RestServiceGenerator.INSTANCE.generateService(TimeEntriesService.class);
  public final UserService userService = RestServiceGenerator.INSTANCE.generateService(UserService.class);


//  private PublishProcessor<Long> onDemandPublisher = PublishProcessor.create();
//  private Flowable<Long> onDemandFlowable = onDemandPublisher.subscribeOn(Schedulers.io());
//  private Flowable<Long> timerFlowable;// = Flowable.interval(60L, TimeUnit.SECONDS).share();

  private CompositeDisposable disposables = new CompositeDisposable();

//    private Flowable<ProjectDto> projectsFlowable = getProjects();
//    private Single<List<Issue>> issuesObs = getIssues(111);
//    private Single<List<TimeEntry>> timeEntriesObs = getTimeEntries(111);

  private void clearDisposables() {
    Log.i(TAG, "clearDisposables: " + disposables.size());
    disposables.clear();
  }

  public void start() {
    disposables = new CompositeDisposable();
//    setTimerInterval(60000);
  }

  public void end() {
    clearDisposables();
//        timerFlowable = null;
  }

//  public void setTimerInterval(long seconds) {
//    Log.w(TAG, "setTimerInterval: " + seconds + " seconds.");
//    timerFlowable = Flowable.merge(onDemandFlowable, Flowable.interval(seconds, TimeUnit.SECONDS)).share()
//        .subscribeOn(Schedulers.io())
//        .doOnNext(x -> Log.i(TAG, ">TICK!<"));
//  }

//  public void refreshData() {
//    Log.i(TAG, "refreshData: ---- " + onDemandPublisher.hasSubscribers());
//    onDemandPublisher.onNext(System.currentTimeMillis());
//  }

}
