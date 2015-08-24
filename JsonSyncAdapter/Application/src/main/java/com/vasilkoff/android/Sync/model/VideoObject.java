package com.vasilkoff.android.Sync.model;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.vasilkoff.android.Sync.provider.DataContract;

import java.lang.reflect.Field;

/**
 * Created by maxim.vasilkov@gmail.com on 24/08/15.
 */
public class VideoObject extends ModelObject {

    public String id;
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
    public static final String getCONTENT_TYPE() {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.jsonsyncadapter.videos";
    }
    /**
     * MIME type for individual entries.
     */
    public static final String getCONTENT_ITEM_TYPE() {
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.jsonsyncadapter.video";
    }

    /**
     * Fully qualified URI for "entry" resources.
     */
    public static final Uri getCONTENT_URI() {
        return DataContract.BASE_CONTENT_URI.buildUpon().appendPath("videos").build();
    }

    public static String[] getProjection() {
        return getProjection(VideoObject.class);
    }
}
