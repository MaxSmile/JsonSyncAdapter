package com.vasilkoff.android.Sync.model;

import android.content.ContentResolver;
import android.net.Uri;

import com.vasilkoff.android.Sync.provider.DataContract;

/**
 * Created by maxim.vasilkov@gmail.com on 24/08/15.
 */
public class CommentObject extends ModelObject {


    /**
     * MIME type for lists of entries.
     */
    public static final String getCONTENT_TYPE() {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.jsonsyncadapter.comments";
    }
    /**
     * MIME type for individual entries.
     */
    public static final String getCONTENT_ITEM_TYPE() {
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.jsonsyncadapter.comments";
    }

    /**
     * Fully qualified URI for "entry" resources.
     */
    public static final Uri getCONTENT_URI() {
        return DataContract.BASE_CONTENT_URI.buildUpon().appendPath("comments").build();
    }

    public static String[] getProjection() {
        return getProjection(CommentObject.class);
    }
}
