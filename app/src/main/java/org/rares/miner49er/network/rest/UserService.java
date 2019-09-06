package org.rares.miner49er.network.rest;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import org.rares.miner49er.network.dto.UserDto;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserService {

    @GET("users/{userId}")
    Single<UserDto> getUser(@Path("userId") int userId);

//        @GET("projects/{projectId}/users")
//        Flowable<User> getUsers(@Path("projectId") int projectId);

    @Multipart
    @POST("users")
    Single<UserDto> createAccount(@Part MultipartBody.Part user, @Part MultipartBody.Part image);

    @POST
    Single<UserDto> login(UserDto user);

    @PUT("users/{userId}")
    Single<Void> putUser(@Path("userId") String userId, UserDto user);

    @DELETE("users/{userId}")
    Single<Void> deleteUser(@Path("userId") String userId);
}