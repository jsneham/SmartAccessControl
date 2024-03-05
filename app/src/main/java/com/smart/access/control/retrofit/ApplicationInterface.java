package com.smart.access.control.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.POST;

public interface ApplicationInterface {

    @POST(Urls.register)
    Call<String> registerClient(
            @Field("name") String name, @Field("username") String username,
            @Field("email") String email, @Field("password") String password,
            @Field("c_password") String c_password
    );

    @POST(Urls.login)
    Call<String> loginClient(
            @Field("email") String email, @Field("password") String filter
    );
}
