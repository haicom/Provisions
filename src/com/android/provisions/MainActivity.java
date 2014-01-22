package com.android.provisions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getImeiAndImsi();
        getBaseInfomation();
        getVersion();
        getLatitudeAndLongitude();
       // getloadAllApplication();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void getImeiAndImsi() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        
        String deviceId = telephonyManager.getDeviceId();
        String telePhone = telephonyManager.getLine1Number();
        String imei = telephonyManager.getSimSerialNumber();
        String imsi = telephonyManager.getSubscriberId();
        
        Log.d(TAG, "getImeiAndImsi deviceId = " + deviceId + " telePhone = " 
        + telePhone + " imei = " + imei + " imsi = " + imsi);
        
    }

    private void getBaseInfomation() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        
        Log.d(TAG, " info.getMacAddress() = " + info.getMacAddress() + " info.getIpAddress() = " + info.getIpAddress() );
        
        PackageManager packageManager = this.getPackageManager();
        
        Log.d(TAG, "getBaseInfomation Build.MODEL =  " + Build.MODEL + " Build.VERSION.RELEASE  =  "
        + Build.VERSION.RELEASE + " Build.VERSION.SDK =  " + Build.VERSION.SDK_INT);
    }
    
    public String[] getVersion(){
        String[] version={"null","null","null","null"};
        String str1 = "/proc/version";
        String str2;
        String[] arrayOfString;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            version[0] = arrayOfString[2];//KernelVersion
            localBufferedReader.close();
        } catch (IOException e) {
        }
        version[1] = Build.VERSION.RELEASE;// firmware version
        version[2] = Build.MODEL;//model
        version[3] = Build.DISPLAY;//system version
        
        Log.d(TAG, "getVersion version[0] =  " + version[0] + " version[1] =  " + version[1] + " version[2] =  " + version[2]
                + " version[3] = " + version[3]);
        return version;
    }
    
    private void getLatitudeAndLongitude() {
        double latitude = 0.0;
        double longitude = 0.0;

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        if ( locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "getLatitudeAndLongitude GPS_PROVIDER location = " + location);
            if ( location != null ) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                      
            } else {
                LocationListener locationListener = new LocationListener() {
                        
                // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            
                        }
                        
                        // Provider被enable时触发此函数，比如GPS被打开
                        @Override
                        public void onProviderEnabled(String provider) {
                            
                        }
                        
                        // Provider被disable时触发此函数，比如GPS被关闭 
                        @Override
                        public void onProviderDisabled(String provider) {
                            
                        }
                        
                        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发 
                        @Override
                        public void onLocationChanged(Location location) {
                            if (location != null) {   
                                Log.d(TAG, "Location changed : Lat: "  
                                + location.getLatitude() + " Lng: "  
                                + location.getLongitude() );   
                            }
                        }
                    };
                    
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);   
                    Location locationNetWork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    Log.d(TAG, "getLatitudeAndLongitude NETWORK_PROVIDER locationNetWork = " + location);
                    if ( locationNetWork != null ) {   
                        latitude = locationNetWork.getLatitude(); //经度   
                        longitude = locationNetWork.getLongitude(); //纬度  
                }   
            }
        }
        
        
        Log.d(TAG, "getLatitudeAndLongitude latitude = " + latitude + " longitude = " + longitude);
    }
    
    private void getloadAllApplication() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final PackageManager packageManager =  getPackageManager();
        List<ResolveInfo> apps = null;
        apps = packageManager.queryIntentActivities(mainIntent, 0);
        Collections.sort( apps, new ResolveInfo.DisplayNameComparator(packageManager) );
        for (ResolveInfo app: apps ) {
           String name = app.activityInfo.name;
           String packageName = app.activityInfo.packageName;
           Drawable icon = app.loadIcon(packageManager);
           String sourceDir = app.activityInfo.applicationInfo.sourceDir;
           Log.d(TAG, "queryIntentActivities name = " + name + " packageName = " + packageName + " icon = " + icon
                   + " sourceDir = " + sourceDir);
        }
        
        List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(0);
        
        for ( PackageInfo packageInfo: packageInfos ) {
            String name = packageInfo.applicationInfo.loadLabel(packageManager).toString();            
            String packageName = packageInfo.packageName;
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            Drawable icon = packageInfo.applicationInfo.loadIcon(getPackageManager());
            String sourceDir = packageInfo.applicationInfo.sourceDir;
            
            Log.d(TAG, "getInstalledPackages name = " + name + " packageName = " + packageName + " icon = " + icon
                    + " sourceDir = " + sourceDir + " versionName = " + versionName + " versionCode ＝ " + versionCode);
        }
      
        
    }

}
