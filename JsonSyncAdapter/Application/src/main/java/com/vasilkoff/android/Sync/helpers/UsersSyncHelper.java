package com.vasilkoff.android.Sync.helpers;

import android.content.SyncResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by user on 30/08/15.
 */
public class UsersSyncHelper {
    public static final String TAG = UsersSyncHelper.class.getSimpleName();
    static GsonBuilder builder = new GsonBuilder();
    static final Gson gson = builder.create();


    public static void getUsersPage(String author, SyncResult syncResult) {

    }
}
