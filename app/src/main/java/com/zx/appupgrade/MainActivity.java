package com.zx.appupgrade;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * APP版本迭代功能的实现
 */
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_WRITE = 100; //打开照相机的请求码
    private static final int RC_SETTINGS_SCREEN = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkVersion();
            }
        });
    }

    //比较当前版本与服务器最新的apk版本
    private void checkVersion() {

        HttpUtils.getVersionData(MainConstant.VersionCode.URL_VERSION_BASE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Version>() {
                    @Override
                    public void onCompleted() {
                        Log.i("tag", "---------->onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("tag", "---------->onError");
                    }

                    @Override
                    public void onNext(Version version) {
                        Log.i("tag", "---------->onNext");
                        //新版本的apk的地址
                        int newVersionCode = version.getData().getVersion();


                        //获取当前app版本
                        int currVersionCode = AppUtils.getPackageVersionCode(MainActivity.this);

                        //如果当前版本小于新版本，则提示更新
                        if (currVersionCode < newVersionCode) {
                            Log.i("tag", "有新版本需要更新");
                            showHintDialog();
                        }
                    }
                });

    }


    //显示询问用户是否更新APP的dialog
    private void showHintDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher)
                .setMessage("检测到当前有新版本，是否更新?")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //取消更新，则跳转到旧版本的APP的页面
                        Toast.makeText(MainActivity.this, "暂时不更新app", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //6.0以下系统，不需要请求权限,直接下载新版本的app
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            downloadApk();
                        } else {
                            //6.0以上,先检查，申请权限，再下载
                            checkPermission();
                        }

                    }
                }).create().show();
    }

    //检查权限
    private void checkPermission() {
        //app更新所需的权限
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
        if (EasyPermissions.hasPermissions(this, permissions)) {
            // Already have permission, do the thing(有权限，直接下载)
            // ...
            downloadApk();
        } else {
            // Do not have permissions, request them now(请求权限)
            EasyPermissions.requestPermissions(this, "app更新需要读写sdcard的权限",
                    REQUEST_CODE_WRITE, permissions);

        }
    }

    //下载最新版的app
    private void downloadApk() {
        boolean isWifi = AppUtils.isWifi(this); //是否处于WiFi状态
        if (isWifi) {
            startService(new Intent(MainActivity.this, UpdateService.class));
            Toast.makeText(MainActivity.this, "开始下载。", Toast.LENGTH_LONG).show();
        } else {
            //弹出对话框，提示是否用流量下载
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("是否要用流量进行下载更新");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    Toast.makeText(MainActivity.this, "取消更新。", Toast.LENGTH_LONG).show();
                }
            });

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startService(new Intent(MainActivity.this, UpdateService.class));
                    Toast.makeText(MainActivity.this, "开始下载。", Toast.LENGTH_LONG).show();
                }
            });
            builder.setCancelable(false);

            AlertDialog dialog = builder.create();
            //设置不可取消对话框
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

    }

    //授权的结果的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_WRITE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
               downloadApk();
            }
        }

    }

    /**
     * 用户同意授权了
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        downloadApk();
        Log.i("tag","--------->同意授权");
    }

    /**
     * 用户拒绝了授权,则通过弹出对话框让用户打开app设置界面，手动授权，然后返回app进行版本更新
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "没有同意授权", Toast.LENGTH_SHORT).show();
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, "请设置权限")
                    .setTitle("设置对话框")
                    .setPositiveButton("设置")
                    .setNegativeButton("取消", null /* click listener */)
                    .setRequestCode(RC_SETTINGS_SCREEN)
                    .build()
                    .show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SETTINGS_SCREEN) {
            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(this, "从app设置返回应用界面", Toast.LENGTH_SHORT)
                    .show();
            downloadApk();
        }
    }
}
