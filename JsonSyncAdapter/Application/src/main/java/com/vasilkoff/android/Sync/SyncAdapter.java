package com.vasilkoff.android.Sync;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vasilkoff.android.Sync.helpers.AppsSyncHelper;
import com.vasilkoff.android.Sync.helpers.VideosSyncHelper;
import com.vasilkoff.android.Sync.model.AppObject;
import com.vasilkoff.android.Sync.model.CommentObject;
import com.vasilkoff.android.Sync.model.UserObject;
import com.vasilkoff.android.Sync.model.VideoObject;
import com.vasilkoff.android.Sync.provider.DataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;


class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = SyncAdapter.class.getSimpleName();

    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();


    private static final String APPS_URL = "https://www.qviky.com/qvk/api/v1.2/apps?unique_key=Uf+9kh3711nwpBRooJrktAMtkTVYktqcJOnehtss4ts=&user_id=0";
    private static int appsPage = 0;

    private static final String VIDEO_URL =
            "https://www.qviky.com/qvk/api/v1.2/feed?unique_key=Uf+9kh3711nwpBRooJrktAMtkTVYktqcJOnehtss4ts=&user_id=0";
    private static int videoPage = 0;


    private static final String COMMENTS_URL =
            "https://www.qviky.com/qvk/api/v1.2/comment?unique_key=Uf+9kh3711nwpBRooJrktAMtkTVYktqcJOnehtss4ts=";

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;



    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    public void getVideoPage(int page,final SyncResult syncResult) {
        AQuery aq = new AQuery(getContext());
        aq.ajax(VIDEO_URL+"&page="+page, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                try {
                    VideosSyncHelper.updateLocalVideoData(getContext(), json, syncResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getAppsPage(int page,final SyncResult syncResult) {
        AQuery aq = new AQuery(getContext());
        aq.ajax(APPS_URL+"&page="+page, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                try {
                    AppsSyncHelper.updateLocalAppData(getContext(), json, syncResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    public void getCommentsPage(int page,final SyncResult syncResult) {
        AQuery aq = new AQuery(getContext());
        aq.ajax(COMMENTS_URL+"&page="+page, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                try {
                    updateLocalCommentData(json, syncResult, CommentObject.getCONTENT_URI(), CommentObject.getProjection());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateLocalCommentData(JSONObject json, SyncResult syncResult, Uri content_uri, String[] projection) {
        // TODO: implement
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, final SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        getVideoPage(videoPage, syncResult);
        getAppsPage(appsPage, syncResult);
        //getUsersPage(usersPage,syncResult);
    }











}
