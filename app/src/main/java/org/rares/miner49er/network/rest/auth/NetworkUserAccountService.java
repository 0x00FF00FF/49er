package org.rares.miner49er.network.rest.auth;

import android.util.Log;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.persistence.UserSpecificDao;
import org.rares.miner49er.network.NetworkingService;
import org.rares.miner49er.network.NetworkingService.RestServiceGenerator;
import org.rares.miner49er.network.dto.UserDto;
import org.rares.miner49er.network.dto.converter.UserConverter;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NetworkUserAccountService {
  private static final String TAG = NetworkUserAccountService.class.getSimpleName();

  private static final String contentDisposition = "Content-Disposition";
  private static final String userHeaderValue = "form-data; name=user;";
  private static final String imageHeaderValue = "form-data; name=\"image\"; filename=\"%s\"";

  private static final String IMAGE = "image";
  private static final String USER_DATA = "user";

  public static Single<UserData> login(UserData userData, AsyncGenericDao<UserData> usersDao) {
    Log.d(TAG, "login() called with: userData = [" + userData + "]");
    UserData localUser = null;
    if (userData.getId() == null) {
      Optional<UserData> optional = ((UserSpecificDao) usersDao).getByEmail(userData.getEmail()).blockingGet();
      if (optional.isPresent()) {
        localUser = optional.get();
      }
    }
    final Long id = null == localUser ? userData.getId() : localUser.getId();
    final String pwd = userData.getPassword();

    final SingleSubject<UserData> loginProcessor = SingleSubject.create();
    final LoginCallback loginCallback = new LoginCallback(loginProcessor);

//  RestServiceGenerator.INSTANCE.getHttpClient().newCall(request).enqueue(callback);
//  the login call must be executed asap
    try (Response response = RestServiceGenerator.INSTANCE
        .getHttpClient()
        .newCall(createLoginRequest(userData)).execute()) { // no thread management - runs on calling thread (!)
      if (response.isSuccessful()) {
        loginCallback.onResponse(null, response);
      } else {
        loginCallback.onFailure(null, new IOException("Login call was not successful. " + response.code() + ":" + response.message()));
      }
    } catch (IOException ex) {
      loginCallback.onFailure(null, ex);
    }

    //noinspection unchecked
    return loginProcessor
        .map(user -> {
          user.setId(id);
          user.setPassword(pwd);
          return user;
        })
        .flatMap(vm -> {
          Single dbOp;
          vm.lastUpdated = System.currentTimeMillis();
          if (vm.getId() == null) {
            dbOp = usersDao.insert(vm);
          } else {
            dbOp = usersDao.update(vm);
          }
          Log.i(TAG, "login: key: " + vm.getApiKey());
          //noinspection unchecked
          return dbOp.flatMap(res -> Single.just(vm));
        });
  }

  public static Single<UserData> createAccount(UserData userData, AsyncGenericDao<UserData> usersDao) {
    final String userPassword = userData.getPassword();
    final Map<String, Part> partMap = createNewUserRequest(userData);
    return NetworkingService.INSTANCE.userService
        .createAccount(partMap.get(USER_DATA), partMap.get(IMAGE))
        .subscribeOn(Schedulers.io())
        .flatMap(response -> {
          if (response.code() != 201) {
            throw new IllegalStateException("Server call failed: " + response.message());
          }
          return UserConverter.toModel(response.body())
              .map(user -> {
                user.setPwd(userPassword);
                return user;
              });
        })
        .flatMap(dm -> Single.just(new org.rares.miner49er.domain.users.persistence.UserConverter().dmToVm(dm)))
        .flatMap(vm -> usersDao
            .insert(vm)
            .map(insertId -> {
              vm.setId(insertId);
              return vm;
            }));
  }

  private static Request createLoginRequest(UserData userData) {
    final String email = userData.getEmail();
    final String pwd = userData.getPassword();
    UserData toSend = new UserData();
    toSend.setEmail(email);

    Log.i(TAG, "login: trying network login...");
    RequestBody userBody = null;
    try {
      //noinspection ConstantConditions
      userBody = RestServiceGenerator.INSTANCE.getRestClient()
          .requestBodyConverter(Class.forName(UserData.class.getCanonicalName()), new Annotation[0], new Annotation[0])
          .convert(toSend);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    if (userBody == null) {
      throw new IllegalStateException("Encountered problems while creating request body.");
    }

    return new Builder()
        .post(userBody)
        .url(RestServiceGenerator.INSTANCE.serviceUrl + "users/login")
        .addHeader("Authorization", Credentials.basic(email, pwd))
        .build();
  }

  private static Map<String, Part> createNewUserRequest(UserData userData) {
    final Map<String, Part> parts = new HashMap<>();
    RequestBody userBody = null;
    try {
      userBody = RestServiceGenerator.INSTANCE.getRestClient()
          .requestBodyConverter(Class.forName(Objects.requireNonNull(
              UserData.class.getCanonicalName())), new Annotation[0], new Annotation[0])
          .convert(userData);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    RequestBody imageBody = null;
    Part userPart = null;
    Part imagePart = null;
    if (userData.getPicture() != null && userData.getPicture().startsWith("/")) {
      File file = new File(userData.getPicture());
      imageBody = MultipartBody.create(MediaType.parse("image/*"), file);
    }

    if (userBody != null) {
      Map<String, String> headersValues = new HashMap<>();
      headersValues.put(contentDisposition, userHeaderValue);
      Headers headers = Headers.of(headersValues);
      userPart = Part.create(headers, userBody);
      parts.put(USER_DATA, userPart);
    }

    if (userBody != null && imageBody != null) {
      String[] split = userData.getPicture().split(File.separator);
      String imageName = split[split.length - 1];

      Map<String, String> headersValues = new HashMap<>();
      headersValues.put(contentDisposition, String.format(imageHeaderValue, imageName));
      Headers headers = Headers.of(headersValues);
      imagePart = Part.create(headers, imageBody);
      parts.put(IMAGE, imagePart);
    }
    return parts;
  }

  private static class LoginCallback implements Callback {

    private SingleSubject<UserData> loginProcessor;

    LoginCallback(SingleSubject<UserData> processor) {
      loginProcessor = processor;
    }

    @Override
    public void onFailure(Call call, IOException e) {
      loginProcessor.onError(e);
      Log.e(TAG, "onFailure: ", e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
      Log.i(TAG, "onResponse: " + response.headers());
      if (response.code() != 200) {
        loginProcessor.onError(new IllegalStateException("Invalid login response. " + response.code() + ": " + response.message()));
        return;
      }
      UserDto userDto = null;
      try {
        //noinspection ConstantConditions
        userDto = RestServiceGenerator.INSTANCE.getRestClient()
            .<UserDto>responseBodyConverter(Class.forName(UserDto.class.getCanonicalName()), new Annotation[0])
            .convert(response.body());
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      if (userDto == null) {
        loginProcessor.onError(new NullPointerException("Could not extract a valid login response."));
      } else {
        UserData userData = new org.rares.miner49er.domain.users.persistence.UserConverter().dmToVm(UserConverter.toModelBlocking(userDto));
        userData.setApiKey(response.header("X-AUTH-TOKEN"));
        loginProcessor.onSuccess(userData);
      }
      response.close();
    }
  }
}
