package com.zx.appupgrade;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by 周旭 on 2017/1/15.
 * App版本迭代的工具类
 */

public class AppUtils {

    /**
     * 获取当前APP版本号
     * @param context
     * @return
     */
    public static int getPackageVersionCode(Context context){
        PackageManager manager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = manager.getPackageInfo(context.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(packageInfo != null){
            return packageInfo.versionCode;
        }else{
            return 1;
        }
    }


    /**
     * 判断是否处于WiFi状态
     * getActiveNetworkInfo 是可用的网络，不一定是链接的，getNetworkInfo 是链接的。
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager manager = (ConnectivityManager)context. getSystemService(CONNECTIVITY_SERVICE);
        //NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //处于WiFi连接状态
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }
}
