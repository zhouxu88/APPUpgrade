package com.zx.appupgrade;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Http工具类
 */
public class HttpUtils {

    private static Retrofit retrofit;
    private static RetrofitInterface retrofitInterface;

    //初始化Retrofit的配置,需要传递一个baseUrl所需的路径的参数
    public static void initRetrofitConfig(String baseUrl){
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(new OkHttpClient())
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);
    }



    //获取APP版本更新时的Version数据
    public static Observable<Version> getVersionData(String baseUrl){
        initRetrofitConfig(baseUrl);
        return retrofitInterface.getVersionBean();
    }
}
