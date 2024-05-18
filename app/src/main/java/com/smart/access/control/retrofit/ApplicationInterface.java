package com.smart.access.control.retrofit;

import com.smart.access.control.modals.LoginResponse;
import com.smart.access.control.modals.UserDetailsResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApplicationInterface {
    @FormUrlEncoded
    @POST(Urls.register)
    Call<LoginResponse> registerClient(
            @Field("name") String name, @Field("username") String username,
            @Field("user_type") String user_type,
            @Field("email") String email, @Field("password") String password,
            @Field("c_password") String c_password
    );

    @FormUrlEncoded
    @POST(Urls.login)
    Call<LoginResponse> loginClient(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET(Urls.user)
    Call<UserDetailsResponse> userClient(
            @Header("Authorization") String authorization
    );
}
