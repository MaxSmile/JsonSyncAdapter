package com.vasilkoff.android.Sync.model;

import android.content.ContentResolver;
import android.net.Uri;

import com.vasilkoff.android.Sync.provider.DataContract;

/**
 * Created by user on 29/08/15.
 */
public class FeedObject extends ModelObject {

    // User fields
    public String username;
    public String picture_path;

    // Apps fields
    public String app_name;
    public String app_icon;
    public String app_bundle_id;
    public String app_url;
    public String app_developer;

    //Video fileds
    public String author;
    public String created_at;
    public String video_path;
    public String description;
    public String bitmap_preview;
    public String recorded_app;
    public String duration;

    /**
     * MIME type for lists of entries.
     */
    public static  String getCONTENT_TYPE() {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.jsonsyncadapter.feed";
    }
    /**
     * MIME type for individual entries.
     */
    public static  String getCONTENT_ITEM_TYPE() {
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.jsonsyncadapter.feed";
    }

    /**
     * Fully qualified URI for "entry" resources.
     */
    public static Uri getCONTENT_URI() {
        return DataContract.BASE_CONTENT_URI.buildUpon().appendPath("feed").build();
    }

    public static String[] getProjection() {
        return getProjection(FeedObject.class);
    }

}
