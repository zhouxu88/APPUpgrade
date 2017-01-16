package com.zx.appupgrade;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.DOWNLOAD_SERVICE;

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


    /*
     * 通过DownloadManager下载apk
     * @param context
     */
    public static void downloadApkByDownloadManager(Context context) {
        //开始下载最新版本的apk文件
        DownloadManager downloadManager = (DownloadManager)context.getSystemService(DOWNLOAD_SERVICE);
        //DownloadManager实现下载
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(MainConstant.NEW_VERSION_APP_URL));
        request.setTitle("文件下载")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,MainConstant.NEW_VERSION_APK_NAME)
                //设置通知在下载中和下载完成都会显示
                //.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                //设置通知只在下载过程中显示，下载完成后不再显示
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        downloadManager.enqueue(request);
    }

    /**Apk的安装
     *
     * @param context
     */
    public static void installApk(Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //这个必须有
        intent.setDataAndType(
                Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), MainConstant.NEW_VERSION_APK_NAME)),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
