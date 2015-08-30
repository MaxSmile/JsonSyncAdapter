package com.vasilkoff.android.Sync.provider;

import android.net.Uri;

public class DataContract {
    /**
     * Content provider authority. Kepp it the same with
     */
    public static final String CONTENT_AUTHORITY = "com.vasilkoff.android.Sync";

    /**
     * Base URI.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

}