package com.android.provisions;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;

public class PackageChangeReceive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (    intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)
                || intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)  ) {
               
           String[] data = intent.getData().toString().split(":");
           String packageName = data[ data.length-1 ];

           List<PackageInfo> packageInfoList =  context.getPackageManager().
                   getInstalledPackages(0);
           for ( int index = 0; index < packageInfoList.size(); index++) {
               PackageInfo packageInfo = packageInfoList.get(index);
               
               if ( packageInfo.packageName.equals(packageName) ) {
                   String appName = packageInfo.applicationInfo.loadLabel
                           (context.getPackageManager()).toString();
                   String appVersion = packageInfo.versionName;
                   int appVerCode = packageInfo.versionCode;                 
                }
            }
        }
    }
}
