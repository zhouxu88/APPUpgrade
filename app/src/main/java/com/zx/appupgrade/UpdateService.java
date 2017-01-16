package com.zx.appupgrade;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * 2种方式都可以实现，下载安装
 * 1）DownloadManager
 * 2）自定义的下载安装任务
 */
public class UpdateService extends Service {

    public static final int NOTIFICATION_ID = 100;
    private static final int REQUEST_CODE = 10; //PendingIntent中的请求码
    //下载的新版本的apk的存放路径
    public static final String destPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "newversion.apk";


    private Context mContext = this;
    private Notification mNotification;
    private NotificationManager manager;
    private NotificationCompat.Builder builder;
    private RemoteViews remoteViews;
    private BroadcastReceiver receiver;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        receiverRegist();
        //下载apk文件
        AppUtils.downloadApkByDownloadManager(this);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //解除注册
        unregisterReceiver(receiver);
    }


    //广播接收的注册
    public void receiverRegist() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //安装apk
                AppUtils.installApk(context);
                stopSelf(); //停止下载的Service
            }
        };
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(receiver, filter); //注册广播
    }


    /**
     * 自定义Apk下载，安装
     *
     * @param newVersionApkUrl 新版本的apk的下载地址
     */
    private void download(String newVersionApkUrl) {

        initNotification(); //初始化通知

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            URL url = new URL(newVersionApkUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置连接的属性
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            //如果响应码为200
            if (conn.getResponseCode() == 200) {
                bis = new BufferedInputStream(conn.getInputStream());
                bos = new BufferedOutputStream(new FileOutputStream(destPath));
                int totalSize;
                int count = 0; //读取到的字节数的计数器
                int progress; //当前进度
                byte[] data = new byte[1024 * 1024];
                int len;
                //文件总的大小
                totalSize = conn.getContentLength();
                while ((len = bis.read(data)) != -1) {
                    count += len; //读取当前总的字节数
                    bos.write(data, 0, len);
                    bos.flush();

                    progress = (int) ((count / (float) totalSize) * 100);
                    //progress = (count * 100) / totalSize; //当前下载的进度

                    //重新设置自定义通知的进度条的进度
                    remoteViews.setProgressBar(R.id.progressBar, 100, progress, false);
                    remoteViews.setTextViewText(R.id.tv_progress, "已经下载了：" + progress + "%");
                    //发送通知
                    manager.notify(NOTIFICATION_ID, mNotification);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //下载文件完成以后，执行以下操作
        Intent installIntent = new Intent();
        /**启动系统服务的Activity，用于显示用户的数据。
         比较通用，会根据用户的数据类型打开相应的Activity。
         */
        installIntent.setAction(Intent.ACTION_VIEW);
        installIntent.setDataAndType(Uri.fromFile(new File(destPath)), "application/vnd.android.package-archive");
        //实例化延时的Activity
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, REQUEST_CODE, installIntent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentTitle("文件下载完毕！")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentText("已下载100%")
                .setContentIntent(pendingIntent);
        //点击通知图标，自动消失
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(NOTIFICATION_ID, notification);
    }


    //初始化通知
    private void initNotification() {
        builder = new NotificationCompat.Builder(mContext);
        //自定义的Notification
        remoteViews = new RemoteViews(getPackageName(), R.layout.layout_main_notification);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.stat_sys_download_anim0);

        builder.setTicker("开始下载apk文件")
                .setSmallIcon(R.drawable.stat_sys_download_anim5)
                .setLargeIcon(largeIcon)
                .setContent(remoteViews);

        //实例化通知对象
        mNotification = builder.build();


        //获取通知的管理器
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

}
