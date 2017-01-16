package com.zx.appupgrade;

/**
 * Created by 周旭 on 2017/1/15.
 */

public class MainConstant {
    //服务器中新版本的app的地址
    public static final String NEW_VERSION_APP_URL = "http://oh0vbg8a6.bkt.clouddn.com/app-debug.apk";

    //模拟APP版本更新时访问服务器的apk的地址
    public static class VersionCode{
        //获取测试用的服务器版的版本号的Json串
        public static final String URL_VERSIN_CODE = "http://oh0vbg8a6.bkt.clouddn.com/versioncheck.json";
        public static final String URL_VERSION_BASE = "http://oh0vbg8a6.bkt.clouddn.com/";
        public static final String URL_VERSION_PATH = "versioncheck.json";
    }
}
