package com.android.provisions.providers;





import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.android.provisions.providers.Provision.AppChange;
import com.android.provisions.providers.Provision.Arrive;
import com.android.provisions.providers.Provision.BaseProvisionColumns;
import com.android.provisions.providers.Provision.GenPosition;
import com.android.provisions.providers.Provision.Information;
import com.android.provisions.providers.Provision.InstalledApp;

public class ProvisionProvider extends  SQLiteContentProvider {
    private final static String TAG = "ProvisionProvider";
    
    public  static String TABLE_ARRIVE ="arrive";   
    public  static String TABLE_INFORMATION = "information";
    public  static String TABLE_INSTALLED_APP = "installed_app";
    public  static String TABLE_GEO_POSITION = "geo_position";
    public  static String TABLE_APP_CHANGE = "app_change";
   

    private SQLiteOpenHelper mOpenHelper;

    @Override
    public String getType(Uri url) {
        Log.d(TAG, " getType uri =   " + url);
        // Generate the body of the query.
        int match = sURLMatcher.match(url);
        switch (match) {
            case PROVISION_ARRIVE:
                return Arrive.CONTENT_TYPE;
            case PROVISION_ARRIVE_ID:
                return Arrive.CONTENT_ITEM_TYPE;
            case PROVISION_INFORMATION:
                return Information.CONTENT_TYPE;
            case PROVISION_INFORMATION_ID:
                return Information.CONTENT_ITEM_TYPE;
            case PROVISION_INSTALLED_APP:
                return InstalledApp.CONTENT_TYPE;
            case PROVISION_INSTALLED_APP_ID:
                return InstalledApp.CONTENT_ITEM_TYPE;
            case PROVISION_GEO_POSITION:
                return GenPosition.CONTENT_TYPE;
            case PROVISION_GEO_POSITION_ID:
                return GenPosition.CONTENT_ITEM_TYPE;
            case PROVISION_APP_CHANGE:
                return AppChange.CONTENT_TYPE;
            case PROVISION_APP_CHANGE_ID:
                return AppChange.CONTENT_ITEM_TYPE;           
            default:
                return null;
        }
    }

    private void notifyChange(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        cr.notifyChange(uri, null);
    }

    @Override
    public boolean onCreate() {
        super.onCreate();
        try {
            return initialize();
        } catch (RuntimeException e) {
            Log.e(TAG, "Cannot start provider", e);
            return false;
        }
    }

    private boolean initialize() {
        final Context context = getContext();
        mOpenHelper = getDatabaseHelper();
        mDb = mOpenHelper.getWritableDatabase();
        return (mDb != null);
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        // Generate the body of the query.
        int match = sURLMatcher.match(url);
        switch (match) {
            case PROVISION_ARRIVE:
                qb.setTables(TABLE_ARRIVE);
                break;
            case PROVISION_ARRIVE_ID:
                qb.setTables(TABLE_ARRIVE);
                qb.appendWhere("(_id = " + url.getPathSegments().get(1) + ")");
                break;
            case PROVISION_INFORMATION:
                qb.setTables(TABLE_INFORMATION);
                break;
            case PROVISION_INFORMATION_ID:
                qb.setTables(TABLE_INFORMATION);
                qb.appendWhere("(_id = " + url.getPathSegments().get(1) + ")");
                break;
            case PROVISION_INSTALLED_APP:
                qb.setTables(TABLE_INSTALLED_APP);
                break;
            case PROVISION_INSTALLED_APP_ID:
                qb.setTables(TABLE_INSTALLED_APP);
                qb.appendWhere("(_id = " + url.getPathSegments().get(1) + ")");
                break;
            case PROVISION_GEO_POSITION:
                qb.setTables(TABLE_GEO_POSITION);               
                break;
            case PROVISION_GEO_POSITION_ID:
                qb.setTables(TABLE_GEO_POSITION);
                qb.appendWhere("(_id = " + url.getPathSegments().get(1) + ")");
                break;
            case PROVISION_APP_CHANGE:
                qb.setTables(TABLE_APP_CHANGE);
                break;
            case PROVISION_APP_CHANGE_ID:
                qb.setTables(TABLE_APP_CHANGE);
                qb.appendWhere("(_id = " + url.getPathSegments().get(1) + ")");
                break;
           
            default:
                Log.e(TAG, "query: invalid request: " + url);
                return null;
        }

        String finalSortOrder = null;
        if (TextUtils.isEmpty(sort)) {
            if ( qb.getTables().equals(TABLE_ARRIVE) ) {
                finalSortOrder = Arrive.DEFAULT_SORT_ORDER;
            } else if ( qb.getTables().equals(TABLE_INFORMATION) ) {
                finalSortOrder = Information.DEFAULT_SORT_ORDER;
            } else if ( qb.getTables().equals(TABLE_INSTALLED_APP) ) {
                finalSortOrder = InstalledApp.DEFAULT_SORT_ORDER;
            } else if ( qb.getTables().equals(TABLE_GEO_POSITION) ) {
                finalSortOrder = GenPosition.DEFAULT_SORT_ORDER;
            } else if ( qb.getTables().equals(TABLE_APP_CHANGE) ) {
                finalSortOrder = AppChange.DEFAULT_SORT_ORDER;
            }
        } else {
            finalSortOrder = sort;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projectionIn, selection,
                selectionArgs, null, null, finalSortOrder);

        
        ret.setNotificationUri(getContext().getContentResolver(), url);
        return ret;
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        return super.delete(url, where, whereArgs);
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        return super.insert(url, initialValues);
    }

    @Override
    public int update(Uri url, ContentValues values, String where,
            String[] whereArgs) {
        return super.update(url, values, where, whereArgs);
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        Log.d(TAG, " applyBatch ");
        return super.applyBatch(operations);
    }

  
    private ContentValues addDateFormat(ContentValues initialValues, ContentValues values, boolean addDate) {
        int date = simpleDateFormat("yyyyMMddHHmmss");

        if (addDate) {
            values.put(BaseProvisionColumns.DATE, date);
        }    

        return values;
    }

  
    private int simpleDateFormat(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String strDate = sdf.format( new Date() );
        Log.d(TAG, "format = " + format + " sdf.format( new Date() )  = " +  sdf.format( new Date() ) );
        Date date = null;
        try {
            date =  sdf.parse(strDate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d(TAG, " simpleDateFormat date = " + date);
        int intDate = Integer.parseInt(strDate);
        Log.d(TAG, "simpleDateFormat longDate = " + intDate);
        return intDate;
    }



    @Override
    protected SQLiteOpenHelper getDatabaseHelper(Context context) {
        return ProvisionDatabaseHelper.getInstance(getContext());
    }

    @Override
    protected Uri insertInTransaction(Uri url, ContentValues initialValues) {
        long rowID = 0;
        int match = sURLMatcher.match(url);

        Log.d(TAG, "Insert uri = " + url + ", match = " + match);


        switch (match) {
            case PROVISION_ARRIVE:
                rowID = insertArrive(initialValues);
                break;
            case PROVISION_INFORMATION:
                rowID = insertInformation(initialValues);
                break;
            case PROVISION_INSTALLED_APP:
                rowID = insertInstalledApp(initialValues);
                break;
            case PROVISION_GEO_POSITION:
                rowID = insertGeoPosition(initialValues);
                break;
            case PROVISION_APP_CHANGE:
                rowID = insertAppChange(initialValues);
                break;          
            default:
                Log.e(TAG, "insert: invalid request: " + url);
                return null;
        }

        if (rowID > 0) {
            Uri uri = ContentUris.withAppendedId(url, rowID);

            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.d(TAG, "insert " + uri + " succeeded");
            }
            notifyChange(uri);
            return uri;
        } else {
            Log.e(TAG,"insert: failed! " + initialValues.toString());
        }
  
        return null;
    }
    
    private long insertArrive(ContentValues initialValues) {
        boolean addDate = false;
        ContentValues values = new ContentValues(initialValues);
        
        if ( !initialValues.containsKey(BaseProvisionColumns.DATE) ) {
            addDate = true;
        }
        
        values = addDateFormat(initialValues, values, addDate);
        
        return mDb.insert(TABLE_ARRIVE, Arrive.DEVICE_IMEI, values);
    }
    
    private long insertInformation(ContentValues initialValues) {
        boolean addDate = false;
        ContentValues values = new ContentValues(initialValues);
        
        if ( !initialValues.containsKey(BaseProvisionColumns.DATE) ) {
            addDate = true;
        }
        
        values = addDateFormat(initialValues, values, addDate);
        
        return mDb.insert(TABLE_INFORMATION, Information.DEVICE_ID, values);
    }
    
    private long insertInstalledApp(ContentValues initialValues) {
        boolean addDate = false;
        ContentValues values = new ContentValues(initialValues);
        
        if ( !initialValues.containsKey(BaseProvisionColumns.DATE) ) {
            addDate = true;
        }
        
        values = addDateFormat(initialValues, values, addDate);
        
        return mDb.insert(TABLE_INSTALLED_APP, InstalledApp.APP_NAME, values);
    }
    
    private long insertGeoPosition(ContentValues initialValues) {
        boolean addDate = false;
        ContentValues values = new ContentValues(initialValues);
        
        if ( !initialValues.containsKey(BaseProvisionColumns.DATE) ) {
            addDate = true;
        }
        
        values = addDateFormat(initialValues, values, addDate);
        
        return mDb.insert(TABLE_GEO_POSITION, GenPosition.LATITUDE, values);
    }
    
    private long insertAppChange(ContentValues initialValues) {
        boolean addDate = false;
        ContentValues values = new ContentValues(initialValues);
        
        if ( !initialValues.containsKey(BaseProvisionColumns.DATE) ) {
            addDate = true;
        }
        
        values = addDateFormat(initialValues, values, addDate);
        
        return mDb.insert(TABLE_APP_CHANGE, AppChange.APP_NAME, values);
    }  

    @Override
    protected int updateInTransaction(Uri url, ContentValues values, String where,
                String[] whereArgs) {
        int count = 0;
        String table = null;
        String extraWhere = null;

        int match = sURLMatcher.match(url);
        switch (match) {
            case PROVISION_ARRIVE:
                table = TABLE_ARRIVE;
                break;
            case PROVISION_ARRIVE_ID:
                table = TABLE_ARRIVE;
                extraWhere = "_id=" + url.getPathSegments().get(1);
                break;
            case PROVISION_INFORMATION:
                table = TABLE_INFORMATION;
                break;
            case PROVISION_INFORMATION_ID:
                table = TABLE_INFORMATION;
                extraWhere = "_id=" + url.getPathSegments().get(1);
                break;
            case PROVISION_INSTALLED_APP:
                table = TABLE_INSTALLED_APP;
                break;
            case PROVISION_INSTALLED_APP_ID:
                table = TABLE_INSTALLED_APP;
                extraWhere = "_id=" + url.getPathSegments().get(1);
                break;
            case PROVISION_GEO_POSITION:
                table = TABLE_GEO_POSITION;
                break;
            case PROVISION_GEO_POSITION_ID:
                table = TABLE_GEO_POSITION;
                extraWhere = "_id=" + url.getPathSegments().get(1);
                break;
            case PROVISION_APP_CHANGE:
                table = TABLE_APP_CHANGE;
                break;
            case PROVISION_APP_CHANGE_ID:
                table = TABLE_APP_CHANGE;
                extraWhere = "_id=" + url.getPathSegments().get(1);
                break;          
            default:
                throw new UnsupportedOperationException(
                        "URI " + url + " not supported");
        }

        if (extraWhere != null) {
            where = DatabaseUtils.concatenateWhere(where, extraWhere);
        }

        Log.d(TAG, "update where = " + where);
        count = mDb.update(table, values, where, whereArgs);

        if (count > 0) {
            Log.d(TAG, "update " + url + " succeeded");
            notifyChange(url);
        }

        return count;
    }

    @Override
    protected int deleteInTransaction(Uri url, String where,
            String[] whereArgs) {
        int count = 0;
        int match = sURLMatcher.match(url);


        switch (match) {
            case PROVISION_ARRIVE:
                count = mDb.delete(TABLE_ARRIVE, where, whereArgs);
                break;
            case PROVISION_ARRIVE_ID:
                int arriveId;

                try {
                    arriveId = Integer.parseInt( url.getPathSegments().get(1) );
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Bad message id: " + url.getPathSegments().get(1));
                }

                where = DatabaseUtils.concatenateWhere("_id = " + arriveId, where);
                count = mDb.delete(TABLE_ARRIVE, where, whereArgs);
                break;
            case PROVISION_INFORMATION:
                count = mDb.delete(TABLE_INFORMATION, where, whereArgs);
                break;
            case PROVISION_INFORMATION_ID:
                int informationId = 0;

                try {
                    informationId = Integer.parseInt(url.getPathSegments().get(1));
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Bad message id: " + url.getPathSegments().get(1));
                }

                where = DatabaseUtils.concatenateWhere("_id = " + informationId, where);
                count = mDb.delete(TABLE_INFORMATION, where, whereArgs);
                break;
            case PROVISION_INSTALLED_APP:
                count = mDb.delete(TABLE_INSTALLED_APP, where, whereArgs);
                break;
            case PROVISION_INSTALLED_APP_ID:
                int appId;

                try {
                    appId = Integer.parseInt(url.getPathSegments().get(1));
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Bad message id: " + url.getPathSegments().get(1));
                }

                where = DatabaseUtils.concatenateWhere("_id = " + appId, where);
                count = mDb.delete(TABLE_INSTALLED_APP, where, whereArgs);
                break;
            case PROVISION_GEO_POSITION:
                count = mDb.delete(TABLE_GEO_POSITION, where, whereArgs);
                break;
            case PROVISION_GEO_POSITION_ID:
                int positionId;

                try {
                    positionId = Integer.parseInt(url.getPathSegments().get(1));
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Bad message id: " + url.getPathSegments().get(1));
                }

                where = DatabaseUtils.concatenateWhere("_id = " + positionId, where);
                count = mDb.delete(TABLE_GEO_POSITION, where, whereArgs);
                break;
            case PROVISION_APP_CHANGE:
                count = mDb.delete(TABLE_APP_CHANGE, where, whereArgs);
                break;
            case PROVISION_APP_CHANGE_ID:
                int changeId;

                try {
                    changeId = Integer.parseInt(url.getPathSegments().get(1));
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Bad message id: " + url.getPathSegments().get(1));
                }

                where = DatabaseUtils.concatenateWhere("_id = " + changeId, where);
                count = mDb.delete(TABLE_APP_CHANGE, where, whereArgs);
                break;         
            default:
                Log.e(TAG, "query: invalid request: " + url);
        }

        return count;
    }

    @Override
    protected void notifyChange() {
        ContentResolver cr = getContext().getContentResolver();
        //cr.notifyChange(uri, null);
    }
    
    private static final int PROVISION_ARRIVE = 1;
    private static final int PROVISION_ARRIVE_ID = 2;
    private static final int PROVISION_INFORMATION = 3;
    private static final int PROVISION_INFORMATION_ID = 4;
    private static final int PROVISION_INSTALLED_APP = 5;
    private static final int PROVISION_INSTALLED_APP_ID = 6;
    private static final int PROVISION_GEO_POSITION = 7;
    private static final int PROVISION_GEO_POSITION_ID = 8;
    private static final int PROVISION_APP_CHANGE = 9;
    private static final int PROVISION_APP_CHANGE_ID = 10;
    
   

    private static final UriMatcher sURLMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI(Provision.AUTHORITY, "arrive", PROVISION_ARRIVE);
        sURLMatcher.addURI(Provision.AUTHORITY, "arrive/#", PROVISION_ARRIVE_ID);
        sURLMatcher.addURI(Provision.AUTHORITY, "information", PROVISION_INFORMATION);
        sURLMatcher.addURI(Provision.AUTHORITY, "information/#", PROVISION_INFORMATION_ID);
        sURLMatcher.addURI(Provision.AUTHORITY, "installed_app", PROVISION_INSTALLED_APP);
        sURLMatcher.addURI(Provision.AUTHORITY, "installed_app/#", PROVISION_INSTALLED_APP_ID);
        sURLMatcher.addURI(Provision.AUTHORITY, "gen_position", PROVISION_GEO_POSITION);
        sURLMatcher.addURI(Provision.AUTHORITY, "gen_position/#", PROVISION_GEO_POSITION_ID);
        sURLMatcher.addURI(Provision.AUTHORITY, "app_change", PROVISION_APP_CHANGE);
        sURLMatcher.addURI(Provision.AUTHORITY, "app_change/#", PROVISION_APP_CHANGE_ID);    
    }


}
