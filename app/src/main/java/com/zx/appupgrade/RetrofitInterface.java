package com.zx.appupgrade;


import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by 周旭 on 2017/1/15.
 */

public interface RetrofitInterface {


    //模拟APP更新时，获取Version的数据
    @GET(MainConstant.VersionCode.URL_VERSION_PATH)
    Observable<Version> getVersionBean();
}

