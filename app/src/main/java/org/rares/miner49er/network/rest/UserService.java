package org.rares.miner49er.network.rest;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.network.dto.UserDto;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

import java.util.List;
import java.util.Map;

public interface UserService {

    @GET("users/{userId}")
    Single<UserDto> getUser(@Path("userId") int userId);

//        @GET("projects/{projectId}/users")
//        Flowable<User> getUsers(@Path("projectId") int projectId);

    @GET("users")
    Single<List<UserDto>> getUsers();

    @Multipart
    @POST("users/new")
    Single<UserDto> createAccount(@PartMap Map<String, RequestBody> requestBodyMap);

    @Multipart
    @POST("users/new")
    Single<Response<UserDto>> createAccount(@Part MultipartBody.Part user, @Part MultipartBody.Part image);

    @POST("users/login")
    Single<Response<UserDto>> login(@Body UserData user);

    @PUT("users/{userId}")
    Single<Void> putUser(@Path("userId") String userId, @Body UserDto user);

    @DELETE("users/{userId}")
    Single<Void> deleteUser(@Path("userId") String userId);
}