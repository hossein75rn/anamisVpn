package com.v2ray.anamin.data.api;


import com.v2ray.anamin.data.model.ConfigSuccess;
import com.v2ray.anamin.data.model.Login;
import com.v2ray.anamin.data.model.VpnConfigs;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    Call<Login> login(@Field("username") String username, @Field("password") String password );

    @FormUrlEncoded
    @POST("config.php")
    Call<ConfigSuccess> config(@Field("uuid") String uuid, @Field("username") String username);
}
