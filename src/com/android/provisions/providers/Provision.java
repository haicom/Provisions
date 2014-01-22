package com.android.provisions.providers;

import android.net.Uri;
import android.provider.BaseColumns;



public class Provision {
    private final static String TAG = "Provision";
    /** The authority for the contacts provider */
    public static final String AUTHORITY = "provision";
    /** A content:// style uri to the authority for the contacts provider */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    // Constructor
    public Provision() {

    }
    
    public interface BaseProvisionColumns extends BaseColumns {
        public static final String DATE   = "date";  
        public static final String UPLOAD = "upload";
    }
    
    public static final class Arrive implements BaseProvisionColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(AUTHORITY_URI, "arrive");
        
        public static final String DEVICE_IMEI = "device_imei";
        public static final String DEVICE_IMSI = "device_imsi";
        
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date DESC";

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/arrive";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/arrive";
    }
    
    
    public static final class Information implements BaseProvisionColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(AUTHORITY_URI, "information");
        
        public static final String DEVICE_ID = "device_id";
        public static final String DEVICE_MAC = "device_mac";
        public static final String DEVICE_IP = "device_ip";
        public static final String DEVICE_KERNEL = "device_kernel";
        public static final String DEVICE_MODEL = "device_model";
        public static final String DEVICE_RELEASE = "device_release";
        public static final String DEVICE_SDK = "device_sdk";      
        public static final String DEVICE_ROM = "device_rom";
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date DESC";

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/information";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/information";
    }
   
   
    public static final class InstalledApp implements BaseProvisionColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(AUTHORITY_URI, "installed_app");
        
        public static final String APP_NAME = "app_name";
        public static final String PACKAGE_NAME = "package_name";
        public static final String VERSION_NAME = "version_name";
        public static final String VERSION_CODE = "version_code";
        public static final String SOURCE_DIR = "source_dir";
    
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date DESC";

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/installed_app";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/installed_app";
    }
    
    public static final class GenPosition implements BaseProvisionColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(AUTHORITY_URI, "gen_position");
        
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";
   
    
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date DESC";

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/gen_position";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/gen_position";
    }

    public static final class AppChange implements BaseProvisionColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(AUTHORITY_URI, "app_change");
        
        public static final String APP_NAME = "app_name";
        public static final String PACKAGE_NAME = "package_name";
        public static final String VERSION_NAME = "version_name";
        public static final String VERSION_CODE = "version_code";
        public static final String SOURCE_DIR = "source_dir";
        public static final String CHANGE_TYPE = "change_type";
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date DESC";

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/app_change";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/app_change";
    }
}
