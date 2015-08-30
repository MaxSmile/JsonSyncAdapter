package com.vasilkoff.android.Sync.model;

import android.content.ContentResolver;

/**
 * Created by user on 29/08/15.
 */
public class FeedObject extends VideoObject {

    // User fields
    public String username;
    public String picture_path;

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

}
