package com.android.provisions.providers;

import com.android.provisions.providers.Provision.AppChange;
import com.android.provisions.providers.Provision.Arrive;
import com.android.provisions.providers.Provision.GenPosition;
import com.android.provisions.providers.Provision.Information;
import com.android.provisions.providers.Provision.InstalledApp;


import android.content.Context;


import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ProvisionDatabaseHelper extends  SQLiteOpenHelper  {
    
    private final static String TAG = "BreezingDatabaseHelper";
    private static ProvisionDatabaseHelper sInstance = null;
    static final String DATABASE_NAME = "breezing.db";
    static final int DATABASE_VERSION = 2;
    private final Context mContext;
    
    
    private ProvisionDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }
    
    /**
     * Return a singleton helper for the combined Breezing health
     * database.
     */
    /* package */
    static synchronized ProvisionDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ProvisionDatabaseHelper(context);
        }
        return sInstance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        createArriveTables(db);
        createInformationTables(db);
        createInstalledAppTables(db);
        createGenPositionTables(db);        
        createAppChangeTables(db);
    }
    
    
   
    
    private void createArriveTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ProvisionProvider.TABLE_ARRIVE  + " ("
              +  Arrive._ID +  " INTEGER PRIMARY KEY, "
              +  Arrive.DEVICE_IMEI + " TEXT, "
              +  Arrive.DEVICE_IMSI + " TEXT, "
              +  Arrive.DATE +  " INTEGER NOT NULL, " 
              +  Arrive.UPLOAD + " INTEGER DEFAULT 0 " + 
                   ");");
    }
    
    private void createInformationTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ProvisionProvider.TABLE_INFORMATION  + " ("
              +  Information._ID +  " INTEGER PRIMARY KEY, "
              +  Information.DEVICE_ID  +  " TEXT, "
              +  Information.DEVICE_MAC +  " TEXT, "
              +  Information.DEVICE_IP  +  " TEXT, "
              +  Information.DEVICE_KERNEL +  " TEXT, "
              +  Information.DEVICE_MODEL +  " TEXT, "
              +  Information.DEVICE_RELEASE +  " TEXT, "
              +  Information.DEVICE_SDK +  " TEXT, "
              +  Information.DEVICE_ROM +  " TEXT, "  
              +  Information.DATE +  " INTEGER NOT NULL, " 
              +  Information.UPLOAD + " INTEGER DEFAULT 0 " + 
                   ");");
    }
    
    private void createInstalledAppTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ProvisionProvider.TABLE_INSTALLED_APP  + " ("
              +  InstalledApp._ID +  " INTEGER PRIMARY KEY, "
              +  InstalledApp.APP_NAME  +  " TEXT, "
              +  InstalledApp.PACKAGE_NAME +  " TEXT, "
              +  InstalledApp.VERSION_NAME  +  " TEXT, "
              +  InstalledApp.VERSION_CODE +  " TEXT, "
              +  InstalledApp.SOURCE_DIR +  " TEXT, "
              +  InstalledApp.DATE +  " INTEGER NOT NULL, " 
              +  InstalledApp.UPLOAD + " INTEGER DEFAULT 0 " + 
                   ");");
    }
    
    private void createGenPositionTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ProvisionProvider.TABLE_GEO_POSITION  + " ("
                +  GenPosition._ID +  " INTEGER PRIMARY KEY, "
                +  GenPosition.LATITUDE  +  " DOUBLE, "
                +  GenPosition.LONGITUDE +  " DOUBLE, "             
                +  GenPosition.DATE +  " INTEGER NOT NULL, " 
                +  GenPosition.UPLOAD + " INTEGER DEFAULT 0 " + 
                     ");");
    }
    
    private void createAppChangeTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ProvisionProvider.TABLE_APP_CHANGE  + " ("
                +  AppChange._ID +  " INTEGER PRIMARY KEY, "
                +  AppChange.APP_NAME  +  " TEXT, "
                +  AppChange.PACKAGE_NAME +  " TEXT, "
                +  AppChange.VERSION_NAME  +  " TEXT, "
                +  AppChange.VERSION_CODE +  " TEXT, "
                +  AppChange.SOURCE_DIR +  " TEXT, "
                +  AppChange.CHANGE_TYPE +  " INTEGER DEFAULT 0, " 
                +  AppChange.DATE +  " INTEGER NOT NULL, " 
                +  AppChange.UPLOAD + " INTEGER DEFAULT 0 " + 
                     ");");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
        
    }
}
