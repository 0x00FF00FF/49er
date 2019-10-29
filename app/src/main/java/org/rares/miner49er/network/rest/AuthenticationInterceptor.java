package org.rares.miner49er.network.rest;

import android.util.Log;
import io.reactivex.schedulers.Schedulers;
import lombok.Setter;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.network.rest.auth.NetworkUserAccountService;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.io.IOException;

public class AuthenticationInterceptor implements Interceptor {
  private static final String TAG = AuthenticationInterceptor.class.getSimpleName();

  @Setter
  private String authToken;
  @Setter
  private AsyncGenericDao<UserData> usersDao;

  public AuthenticationInterceptor(String token, AsyncGenericDao<UserData> dao) {
    this.authToken = token;
    usersDao = dao;
  }

  private final int retryCount = 4;
  private int retries = 0;

  @Override
  public Response intercept(Chain chain) throws IOException {
    Log.d(TAG, "intercept() called. [" + authToken + "]");

    Request original = chain.request();

    Request.Builder builder = original.newBuilder();

    if (authToken != null && !authToken.equals("")) {
      builder
//                .header("Authorization", authToken);
          .header("X-AUTH-TOKEN", authToken);
    }

    Request request = builder.build();

    Response response = chain.proceed(request);
    UserData loggedInUser = ViewModelCacheSingleton.getInstance().loggedInUser;
//    boolean isLoginCall = request.url().pathSegments().contains("login");

    Log.d(TAG, "intercept: " + response.code());

    if (loggedInUser != null) {
      if (401 == response.code() || 500 == response.code()) { // invalid session throws 500
//        if (!response.isSuccessful()) {
        if (retries++ < retryCount) {
          Log.w(TAG, "intercept: ->  should invalidate API KEY and retry login.");

          authToken = "";
          Log.v(TAG, "intercept: before: " + authToken);
          ViewModelCacheSingleton.getInstance().loggedInUser =
              NetworkUserAccountService.login(loggedInUser, usersDao) // FIXME: inject non-static NetworkUserAccountService
                  .subscribeOn(Schedulers.io())
                  .blockingGet();

          loggedInUser = ViewModelCacheSingleton.getInstance().loggedInUser;

          authToken = loggedInUser.getApiKey();
          Log.i(TAG, "intercept: after: " + authToken);
        } else {
          Log.i(TAG, "intercept: max retries reached, enough is enough.");
          retries = 0;
        }
      }
      if (response.isSuccessful()) {
        retries = 0;
      }
    }

    return response;
  }
}