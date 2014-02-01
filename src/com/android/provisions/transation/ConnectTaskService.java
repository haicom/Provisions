
package com.android.provisions.transation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.provisions.R;
import com.android.provisions.providers.Provision;
import com.android.provisions.providers.Provision.Arrive;
import com.android.provisions.providers.Provision.GenPosition;
import com.android.provisions.providers.Provision.Information;
import com.android.provisions.providers.Provision.InstalledApp;

public class ConnectTaskService extends Service {
    private static final String TAG = "ConnectionTaskService";

    private ServiceHandler mServiceHandler;

    private Looper mServiceLooper;

    private int mResultCode;

    private ConnectivityManager mConnMgr;

    private BroadcastReceiver mReceiver;

    public static final int CONNECTION_TIMEOUT = 20000;

    public static final int SOCKET_TIMEOUT = 20000;

    public static final String ACTION_SEND_MESSAGE = "com.android.login.transaction.MESSAGE_SENT";

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        Log.d(TAG, "onCreate()");
        HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Temporarily removed for this duplicate message track down.

        mResultCode = intent != null ? intent.getIntExtra("result", 0) : 0;
        mConnMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mResultCode != 0) {
            Log.v(TAG, "onStart: #" + startId + " mResultCode: " + mResultCode + " = "
                    + translateResultCode(mResultCode));
        }

        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        /*
         * 主要目的是查看网络的状态， 如果处于没有网络的时候就通过注册广播来监听状态的变化 等有网络的时候继续操作
         */

        Log.v(TAG, "onStart: #" + startId + ": " + intent.getExtras() + " intent = " + intent);
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private static String translateResultCode(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                return "Activity.RESULT_OK";
            default:
                return "Unknown error code";
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Handle incoming transaction requests. The incoming requests are
         * initiated by the MMSC Server or by the MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            int serviceId = msg.arg1;
            Intent intent = (Intent)msg.obj;
            Log.d(TAG, "handleMessage serviceId: " + serviceId + " intent: " + intent);

            if (intent != null) {
                String action = intent.getAction();

                int error = intent.getIntExtra("errorCode", 0);

                Log.v(TAG, " handleMessage action: " + action + " error: " + error);
                if (ACTION_SEND_MESSAGE.endsWith(action)) {

                } else if ( intent.getAction().equals(Intent.ACTION_USER_PRESENT) ) {
                    
                }
            }

            // NOTE: We MUST not call stopSelf() directly, since we need to
            // make sure the wake lock acquired by AlertReceiver is released.
            ConnectReceiver.finishStartingService(ConnectTaskService.this, serviceId);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean NetworkIsAccess() {

        try {
            Runtime runtime = Runtime.getRuntime();
            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int mExitValue = mIpAddrProcess.waitFor();
            return mExitValue == 0;
        } catch (Exception ex) {

        }

        return false;
    }

    /***
     * 当网络改变时即把错误的数据全部设置为原始数据
     */
    public static class NetWorkBroadcastReceiver extends BroadcastReceiver {
        Context context;

        @Override
        public void onReceive(Context context, Intent intent) {
            this.context = context;
            String action = intent.getAction();
            Log.d(TAG, "NetWorkBroadcastReceiver onReceive() action: " + action);

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
            }

            ConnectivityManager connManager = (ConnectivityManager)context
                    .getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            /*
             * If we are being informed that connectivity has been established
             * to allow MMS traffic, then proceed with processing the pending
             * transaction, if any.
             */
            Log.d(TAG, "Handle NetWorkBroadcastReceiver onReceive(): networkInfo = " + networkInfo);

            if (networkInfo != null) {
                Log.d(TAG,
                        "NetWorkBroadcastReceiver networkInfo.getType() = " + networkInfo.getType()
                                + " networkInfo.isConnected() = " + networkInfo.isConnected());
            }

            // Check availability of the mobile network.
            if ((networkInfo == null)
                    || !((networkInfo.getType() == ConnectivityManager.TYPE_WIFI) || (networkInfo
                            .getType() == ConnectivityManager.TYPE_MOBILE))) {
                Log.d(TAG, " NetWorkBroadcastReceiver type is not TYPE_WIFI TYPE_MOBILE, bail");
                return;
            }

            if (!networkInfo.isConnected()) {
                Log.d(TAG, " NetWorkBroadcastReceiver Wifi or Mobile not connected, bail");
                return;
            }

        }

    };

    public class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {

            Log.d(TAG, " ScreenReceiver onReceive ");
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // do whatever you need to do here
                Log.d(TAG, " ScreenReceiver Intent.ACTION_SCREEN_OFF ");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // and do whatever you need to do here
                Log.d(TAG, " ScreenReceiver Intent.ACTION_SCREEN_ON ");
            }
        }
    }
    
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
    
    public synchronized void handleDataSaveToDB() {
        appendArriveInfo( );
        appendBaseInfo( );
        appendAllAppInfo();
        insertLatitudeAndLongitude();
    }
    private void appendArriveInfo() {
        int index = queryArriveInfo();
        
        if ( index == 0 ) {
            insertArriveToDB();
        }
    }
    
    
    private int queryArriveInfo() {
        int index = 0;
        Cursor cursor = getContentResolver().query(Arrive.CONTENT_URI, 
                        new String[] {
                        Arrive._ID
                        }, null, null, null);

        try {
            index = cursor.getCount();
        } finally {
            cursor.close();
        }

        return index;
    }

    private String insertArriveToDB() {
        String result = null;

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = telephonyManager.getDeviceId();
        String telePhone = telephonyManager.getLine1Number();
        String imei = telephonyManager.getSimSerialNumber();
        String imsi = telephonyManager.getSubscriberId();
        if ( !imei.isEmpty() && 
             !imsi.isEmpty()   ) {
            ops.add( ContentProviderOperation.newInsert(Arrive.CONTENT_URI)
                    .withValue(Arrive.DEVICE_IMEI, imei)
                    .withValue(Arrive.DEVICE_IMSI, imsi)
                    .build() );
        }
       

        try {
            getContentResolver().applyBatch(Provision.AUTHORITY, ops);
        } catch (Exception e) {
            result = getResources().getString(R.string.data_error);
            // Log exception
            Log.e(TAG, "Exceptoin encoutered while inserting contact: " + e);
        }

        Log.d(TAG, "getImeiAndImsi deviceId = " + deviceId + " telePhone = " + telePhone
                + " imei = " + imei + " imsi = " + imsi);

        return result;
    }
    
    private void appendBaseInfo() {
        int index = queryBaseInfo();
        if ( index == 0 ) {
            insertBaseInfoToDB();
        }
    }
    
    
    private int queryBaseInfo() {
        int index = 0;
        Cursor cursor = getContentResolver().query(Information.CONTENT_URI, 
                        new String[] {
                        Information._ID
                        }, null, null, null);

        try {
            index = cursor.getCount();
        } finally {
            cursor.close();
        }

        return index;
    }
    
    private String insertBaseInfoToDB( ) {
        ArrayList<ContentProviderOperation> ops = 
                new ArrayList<ContentProviderOperation>();
        String  kernel = null;
        String  strVersion = "/proc/version";
        String  strRes;
        String  result = null;
        String[] arrayOfString;  
        
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();

        Log.d(TAG, " info.getMacAddress() = " + info.getMacAddress() + " info.getIpAddress() = "
                + info.getIpAddress());
        
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE );        
        final String deviceId =  telephonyManager.getDeviceId();
        
        Log.d(TAG, "getBaseInfomation Build.MODEL =  " + Build.MODEL
                + " Build.VERSION.RELEASE  =  " + Build.VERSION.RELEASE 
                + " Build.VERSION.SDK =  "
                + Build.VERSION.SDK_INT);
        
      
        try {
            FileReader localFileReader = new FileReader(strVersion);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            strRes = localBufferedReader.readLine();
            arrayOfString = strRes.split("\\s+");
            kernel = arrayOfString[2];//KernelVersion
            localBufferedReader.close();
        } catch (IOException e) {
            
        }
                
        ops.add( ContentProviderOperation.newInsert(Information.CONTENT_URI)
                .withValue(Information.DEVICE_ID, deviceId)
                .withValue(Information.DEVICE_MAC, info.getMacAddress())
                .withValue(Information.DEVICE_IP, info.getIpAddress())
                .withValue(Information.DEVICE_KERNEL, kernel)        
                .withValue(Information.DEVICE_MODEL, Build.MODEL)
                .withValue(Information.DEVICE_RELEASE, Build.VERSION.RELEASE)
                .withValue(Information.DEVICE_SDK, Build.VERSION.SDK_INT)
                .withValue(Information.DEVICE_ROM, Build.VERSION.RELEASE)
                .build() );

        try {
            getContentResolver().applyBatch(Provision.AUTHORITY, ops);
        } catch (Exception e) {
            result = getResources().getString(R.string.data_error);
            // Log exception
            Log.e(TAG, "Exceptoin encoutered while inserting contact: " + e);
        }
        
        return result;
    }
    
    private void appendAllAppInfo() {
        boolean appLoad = getSharedPrefsValueBoolean(this, PREFS_LOAD_ALL_APP);
        
        if ( !appLoad ) {
            insertAllAppToDB();
        }
    }
    
    private String insertAllAppToDB( ) {
        String  result = null;
        ArrayList<ContentProviderOperation> ops = 
                new ArrayList<ContentProviderOperation>();
        final PackageManager packageManager =  getPackageManager();        
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        
        for ( PackageInfo packageInfo: packageInfos ) {
            String name = packageInfo.applicationInfo.loadLabel(packageManager).toString();            
            String packageName = packageInfo.packageName;
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;          
            String sourceDir = packageInfo.applicationInfo.sourceDir;
            ops.add( ContentProviderOperation.newInsert(InstalledApp.CONTENT_URI)
                    .withValue(InstalledApp.APP_NAME, name)
                    .withValue(InstalledApp.PACKAGE_NAME, packageName)
                    .withValue(InstalledApp.VERSION_NAME, versionName)
                    .withValue(InstalledApp.VERSION_CODE, versionCode)        
                    .withValue(InstalledApp.SOURCE_DIR, sourceDir)  
                    .build() );           
        }
        
        try {
            getContentResolver().applyBatch(Provision.AUTHORITY, ops);
            loadAllAppPrefsFinish(this);
        } catch (Exception e) {
            result = getResources().getString(R.string.data_error);
            // Log exception
            Log.e(TAG, "Exceptoin encoutered while inserting contact: " + e);
        }
        
        return result;
    }
    
    public static void loadAllAppPrefsFinish(Context context) {
        SharedPreferences  sharedPreferences = context.getSharedPreferences(DATA_PREFS_SAVE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREFS_LOAD_ALL_APP, true);
        editor.commit();
    }
    
    public  boolean  getSharedPrefsValueBoolean(Context context, String key) {
        SharedPreferences  sharedPreferences = context.getSharedPreferences(DATA_PREFS_SAVE, context.MODE_PRIVATE);
        boolean value = sharedPreferences.getBoolean(key, false);
        Log.d(TAG, " getSharedPrefsValue value = " + value);
        return value;
   }
    
    private String insertLatitudeAndLongitude() {
        String result = null;
        double latitude = 0.0;
        double longitude = 0.0;
        ArrayList<ContentProviderOperation> ops = 
                new ArrayList<ContentProviderOperation>();
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
                       if ( location != null ) {   
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
        
        ops.add( ContentProviderOperation.newInsert(GenPosition.CONTENT_URI)
                .withValue(GenPosition.LATITUDE, latitude)
                .withValue(GenPosition.LONGITUDE, longitude)               
                .build() );       
        
        try {
            getContentResolver().applyBatch(Provision.AUTHORITY, ops);
            loadAllAppPrefsFinish(this);
        } catch (Exception e) {
            result = getResources().getString(R.string.data_error);
            // Log exception
            Log.e(TAG, "Exceptoin encoutered while inserting contact: " + e);
        }
        Log.d(TAG, "getLatitudeAndLongitude latitude = " + latitude + " longitude = " + longitude);
        return result;
    }


    public static final String PREFS_LOAD_ALL_APP = "load_all_app";
    private static final String DATA_PREFS_SAVE = "DataSave";

}
