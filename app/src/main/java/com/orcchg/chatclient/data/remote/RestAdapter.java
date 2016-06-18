package com.orcchg.chatclient.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orcchg.chatclient.data.model.Check;
import com.orcchg.chatclient.data.model.LoginForm;
import com.orcchg.chatclient.data.model.Message;
import com.orcchg.chatclient.data.model.RegistrationForm;
import com.orcchg.chatclient.data.model.Status;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Observable;

public interface RestAdapter {

    String ENDPOINT = "http://" + ServerBridge.IP_ADDRESS + ":" + ServerBridge.PORT + "/";

    class Creator {
        public static RestAdapter create() {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RestAdapter.ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(client)
                    .build();
            return retrofit.create(RestAdapter.class);
        }
    }

    @GET("/login")
    Observable<LoginForm> getLoginForm();

    @POST("/login")
    Observable<Status> sendLoginForm(@Body LoginForm form);

    @GET("/register")
    Observable<RegistrationForm> getRegistrationForm();

    @POST("/register")
    Observable<Status> sendRegistrationForm(@Body RegistrationForm form);

    @POST("/message")
    Observable<Status> sendMessage(@Body Message message);

    @DELETE("/logout")
    Observable<Status> logout(@Query("id") long id, @Query("login") String name);

    @PUT("/switch_channel")
    Observable<Status> switchChannel(@Query("id") long id, @Query("channel") int channel, @Query("login") String name);

    @GET("/is_logged_in")
    Observable<Check> isLoggedIn(@Query("login") String name);

    @GET("/is_registered")
    Observable<Check> isRegistered(@Query("login") String name);
}
