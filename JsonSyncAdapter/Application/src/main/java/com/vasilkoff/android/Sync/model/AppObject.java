package com.vasilkoff.android.Sync.model;

import android.content.ContentResolver;
import android.net.Uri;

import com.vasilkoff.android.Sync.provider.DataContract;

/**
 * Created by maxim.vasilkov@gmail.com on 24/08/15.
 */
public class AppObject  extends ModelObject {

    public String app_name;//": "Zombie Highway 2",
    public String app_icon;//": "https://www.qviky.com/qviky/apps/com.auxbrain.zh2/icon.jpg",
    public String app_bundle_id;//": "com.auxbrain.zh2",
    public String app_url;//": "https://play.google.com/store/apps/details?id=com.auxbrain.zh2",
    public String app_developer;//": "Auxbrain Inc ",
    public String created_at;//": "2015-08-10T07:24:17+00:00"


    /**
     * MIME type for lists of entries.
     */
    public static final String getCONTENT_TYPE() {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.jsonsyncadapter.apps";
    }
    /**
     * MIME type for individual entries.
     */
    public static final String getCONTENT_ITEM_TYPE() {
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.jsonsyncadapter.app";
    }

    /**
     * Fully qualified URI for "entry" resources.
     */
    public static final Uri getCONTENT_URI() {
        return DataContract.BASE_CONTENT_URI.buildUpon().appendPath("apps").build();
    }

    public static String[] getProjection() {
        return getProjection(AppObject.class);
    }
}
