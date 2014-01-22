package com.android.provisions.transation;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;


import android.util.Log;

public class ConnectTaskService extends Service {
    private static final String TAG = "ConnectionTaskService";
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private int mResultCode;
    private ConnectivityManager mConnMgr;
    private BroadcastReceiver mReceiver;
 

    public static final int CONNECTION_TIMEOUT = 20000;
    public static final int SOCKET_TIMEOUT = 20000;

    public static final String ACTION_SEND_MESSAGE= "com.android.login.transaction.MESSAGE_SENT";

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
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
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mResultCode != 0) {
            Log.v(TAG, "onStart: #" + startId + " mResultCode: " + mResultCode +
                    " = " + translateResultCode(mResultCode));
        }
        
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);       
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
        
        /*
         * 主要目的是查看网络的状态，
         * 如果处于没有网络的时候就通过注册广播来监听状态的变化
         * 等有网络的时候继续操作
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
            Intent intent = (Intent) msg.obj;
            Log.d(TAG, "handleMessage serviceId: " + serviceId 	+ " intent: " + intent);


            if (intent != null) {
                String action = intent.getAction();

                int error = intent.getIntExtra("errorCode", 0);

                Log.v(TAG, " handleMessage action: " + action + " error: " + error);
                if (ACTION_SEND_MESSAGE.endsWith(action)) {
                  
                }
            }

            // NOTE: We MUST not call stopSelf() directly, since we need to
            // make sure the wake lock acquired by AlertReceiver is released.
            ConnectReceiver.finishStartingService(ConnectTaskService.this,  serviceId);
        }
    }

  

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public boolean  NetworkIsAccess() {
        
        try {
            Runtime runtime = Runtime.getRuntime();
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int mExitValue = mIpAddrProcess.waitFor();       
            return mExitValue==0;            
        } catch (Exception ex) {
               
        }
        
        return false;
   }

    /***
     * 当网络改变时即把错误的数据全部设置为原始数据
     *
     */
    public static class NetWorkBroadcastReceiver extends BroadcastReceiver {
        Context context ;
        @Override
        public void onReceive(Context context, Intent intent) {
            this.context = context;
            String action = intent.getAction();
            Log.d(TAG, "NetWorkBroadcastReceiver onReceive() action: " + action);

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
            }

            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            /*
             * If we are being informed that connectivity has been established
             * to allow MMS traffic, then proceed with processing the pending
             * transaction, if any.
             */
            Log.d(TAG, "Handle NetWorkBroadcastReceiver onReceive(): networkInfo = " + networkInfo );

            if (networkInfo != null) {
                Log.d(TAG, "NetWorkBroadcastReceiver networkInfo.getType() = " + networkInfo.getType() + " networkInfo.isConnected() = " + networkInfo.isConnected());
            }

            // Check availability of the mobile network.
            if ( ( networkInfo == null ) ||
                 !( ( networkInfo.getType() == ConnectivityManager.TYPE_WIFI ) ||
                    ( networkInfo.getType() == ConnectivityManager.TYPE_MOBILE ) ) ) {
                Log.d(TAG, " NetWorkBroadcastReceiver type is not TYPE_WIFI TYPE_MOBILE, bail");
                return;
            }

            if ( ! networkInfo.isConnected() ) {
                Log.d(TAG, " NetWorkBroadcastReceiver Wifi or Mobile not connected, bail");
                return;
            }

          
        }


      
    };
    
    public class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            
            Log.d(TAG," ScreenReceiver onReceive ");
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) ) {
                // do whatever you need to do here              
                Log.d(TAG," ScreenReceiver Intent.ACTION_SCREEN_OFF ");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // and do whatever you need to do here              
                Log.d(TAG, " ScreenReceiver Intent.ACTION_SCREEN_ON ");
            }
        }
    }

}
