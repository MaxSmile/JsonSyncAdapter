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
import com.vasilkoff.android.Sync.model.AppObject;
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

    private static final String USERS_URL =
            "https://www.qviky.com/qvk/api/v1.2.1/users?unique_key=Uf+9kh3711nwpBRooJrktAMtkTVYktqcJOnehtss4ts=&type=retrieve&user_id=";
    private static int usersPage = 0;

    private static final String COMMENTS_URL =
            "https://www.qviky.com/qvk/api/v1.2/comment?unique_key=Uf+9kh3711nwpBRooJrktAMtkTVYktqcJOnehtss4ts=";
    private static int commentsPage = 0;

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
                    updateLocalVideoData(json, syncResult, VideoObject.getCONTENT_URI(), VideoObject.getProjection());
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
                    updateLocalAppData(json, syncResult, AppObject.getCONTENT_URI(), AppObject.getProjection());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getUsersPage(int page,final SyncResult syncResult) {
        AQuery aq = new AQuery(getContext());
        aq.ajax(USERS_URL+"35&page="+page, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                try {
                    updateLocalAppData(json, syncResult, AppObject.getCONTENT_URI(), AppObject.getProjection());
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
                    updateLocalAppData(json, syncResult, AppObject.getCONTENT_URI(), AppObject.getProjection());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, final SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        getVideoPage(videoPage, syncResult);
        getAppsPage(appsPage, syncResult);
        //getUsersPage(usersPage,syncResult);
    }


    public void updateLocalVideoData(JSONObject data, final SyncResult syncResult,
                                     Uri uri, String[] projection)
            throws JSONException,
            RemoteException,
            OperationApplicationException {

        final ContentResolver contentResolver = getContext().getContentResolver();




        JSONArray entries = data.getJSONArray("result");
        Log.i(TAG, "Parsing complete. Found " + entries.length() + " entries");


        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        HashMap<String, VideoObject> entryMap = new HashMap<String, VideoObject>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject e = entries.getJSONObject(i);
            VideoObject de = gson.fromJson(e.toString(),VideoObject.class);// new VideoObject(e.getString("id"),e.getString("created"),e.getString("video"),0);
            entryMap.put(de.id, de);
        }

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Cursor c = contentResolver.query(uri, projection, null, null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

        // Find stale data
        int id;
        String entryId;
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            entryId = c.getString(COLUMN_ENTRY_ID);
            VideoObject match = entryMap.get(entryId);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(entryId);
                // Check to see if the entry needs to be updated
                Uri existingUri = uri.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if (!match.isTheSame(c)) {
                    // Update existing record
                    Log.i(TAG, "Scheduling update: " + existingUri);

                    ContentProviderOperation.Builder b = ContentProviderOperation.newUpdate(existingUri);
                    Field fileds[] = VideoObject.class.getFields();
                    for (int i = 0; i < fileds.length; i++) {
                        Field f = fileds[i];
                        try {
                            b.withValue(f.getName(),f.get(match));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    batch.add(b.build());
                    syncResult.stats.numUpdates++;
                } else {
                    Log.i(TAG, "No action: " + existingUri);
                }
            } else {
                // Entry doesn't exist. Remove it from the database.
//
//                Uri deleteUri = VideoObject.getCONTENT_URI().buildUpon()
//                        .appendPath(Integer.toString(id)).build();
//                Log.i(TAG, "Scheduling delete: " + deleteUri);
//                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
//                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Add new items
        for (VideoObject e : entryMap.values()) {
            Log.i(TAG, "Scheduling insert: entry_id=" + e.id);

            ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(VideoObject.getCONTENT_URI());
            Field fileds[] = VideoObject.class.getFields();
            for (int i = 0; i < fileds.length; i++) {
                Field f = fileds[i];
                try {
                    String name = f.getName();
                    String value = (String) f.get(e);
                    b.withValue(name,value);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }

            batch.add(b.build());
            syncResult.stats.numInserts++;
        }
        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(DataContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                VideoObject.getCONTENT_URI(), // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        int pages = data.getInt("totalPages");
        videoPage = data.getInt("currentPage");
        if (pages>videoPage) {
            getVideoPage(videoPage+1,syncResult);
        }
    }


    public void updateLocalAppData(JSONObject data, final SyncResult syncResult,
                                     Uri uri, String[] projection)
            throws JSONException,
            RemoteException,
            OperationApplicationException {

        final ContentResolver contentResolver = getContext().getContentResolver();

        JSONArray entries = data.getJSONArray("apps");
        Log.i(TAG, "Parsing complete. Found " + entries.length() + " entries");


        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        HashMap<String, AppObject> entryMap = new HashMap<String, AppObject>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject e = entries.getJSONObject(i);
            AppObject de = gson.fromJson(e.toString(),AppObject.class);
            entryMap.put(de.id, de);
        }

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Cursor c = contentResolver.query(uri, projection, null, null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

        // Find stale data
        int id;
        String entryId;
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            entryId = c.getString(COLUMN_ENTRY_ID);
            AppObject match = entryMap.get(entryId);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(entryId);
                // Check to see if the entry needs to be updated
                Uri existingUri = uri.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if (!match.isTheSame(c)) {
                    // Update existing record
                    Log.i(TAG, "Scheduling update: " + existingUri);

                    ContentProviderOperation.Builder b = ContentProviderOperation.newUpdate(existingUri);
                    Field fileds[] = AppObject.class.getFields();
                    for (int i = 0; i < fileds.length; i++) {
                        Field f = fileds[i];
                        try {
                            b.withValue(f.getName(),f.get(match));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    batch.add(b.build());
                    syncResult.stats.numUpdates++;
                } else {
                    Log.i(TAG, "No action: " + existingUri);
                }
            } else {
                // Entry doesn't exist. Remove it from the database.
//                Uri deleteUri = AppObject.getCONTENT_URI().buildUpon()
//                        .appendPath(Integer.toString(id)).build();
//                Log.i(TAG, "Scheduling delete: " + deleteUri);
//                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
//                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Add new items
        for (AppObject e : entryMap.values()) {
            Log.i(TAG, "Scheduling insert: entry_id=" + e.id);

            ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(AppObject.getCONTENT_URI());
            Field fileds[] = AppObject.class.getFields();
            for (int i = 0; i < fileds.length; i++) {
                Field f = fileds[i];
                try {
                    String name = f.getName();
                    String value = (String) f.get(e);
                    b.withValue(name,value);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }

            batch.add(b.build());
            syncResult.stats.numInserts++;
        }
        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(DataContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                AppObject.getCONTENT_URI(), // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }





    public void updateLocalUserData(JSONObject data, final SyncResult syncResult,
                                   Uri uri, String[] projection)
            throws JSONException,
            RemoteException,
            OperationApplicationException {

        final ContentResolver contentResolver = getContext().getContentResolver();


        // TODO: have to fix this code after fix the URL
        JSONArray entries = data.getJSONObject("user").getJSONArray("followers");
        Log.i(TAG, "Parsing complete. Found " + entries.length() + " entries");


        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        HashMap<String, UserObject> entryMap = new HashMap<>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject e = entries.getJSONObject(i);
            UserObject de = gson.fromJson(e.toString(),UserObject.class);// new VideoObject(e.getString("id"),e.getString("created"),e.getString("video"),0);
            entryMap.put(de.id, de);
        }

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Cursor c = contentResolver.query(uri, projection, null, null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

        // Find stale data
        int id;
        String entryId;
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            entryId = c.getString(COLUMN_ENTRY_ID);
            UserObject match = entryMap.get(entryId);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(entryId);
                // Check to see if the entry needs to be updated
                Uri existingUri = uri.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if (!match.isTheSame(c)) {
                    // Update existing record
                    Log.i(TAG, "Scheduling update: " + existingUri);

                    ContentProviderOperation.Builder b = ContentProviderOperation.newUpdate(existingUri);
                    Field fileds[] = AppObject.class.getFields();
                    for (int i = 0; i < fileds.length; i++) {
                        Field f = fileds[i];
                        try {
                            b.withValue(f.getName(),f.get(match));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    batch.add(b.build());
                    syncResult.stats.numUpdates++;
                } else {
                    Log.i(TAG, "No action: " + existingUri);
                }
            } else {
                // Entry doesn't exist. Remove it from the database.
//                Uri deleteUri = UserObject.getCONTENT_URI().buildUpon()
//                        .appendPath(Integer.toString(id)).build();
//                Log.i(TAG, "Scheduling delete: " + deleteUri);
//                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
//                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Add new items
        for (UserObject e : entryMap.values()) {
            Log.i(TAG, "Scheduling insert: entry_id=" + e.id);

            ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(UserObject.getCONTENT_URI());
            Field fileds[] = UserObject.class.getFields();
            for (int i = 0; i < fileds.length; i++) {
                Field f = fileds[i];
                try {
                    String name = f.getName();
                    String value = (String) f.get(e);
                    b.withValue(name,value);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }

            batch.add(b.build());
            syncResult.stats.numInserts++;
        }
        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(DataContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                UserObject.getCONTENT_URI(), // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

}
