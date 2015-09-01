package com.vasilkoff.android.Sync.helpers;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vasilkoff.android.Sync.model.AppObject;
import com.vasilkoff.android.Sync.model.UserObject;
import com.vasilkoff.android.Sync.provider.DataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 30/08/15.
 */
public class UsersSyncHelper {
    public static final String TAG = UsersSyncHelper.class.getSimpleName();
    static GsonBuilder builder = new GsonBuilder();
    static final Gson gson = builder.create();

    private static final String USERS_URL =
            "https://www.qviky.com/qvk/api/v1.2.1/users?unique_key=Uf+9kh3711nwpBRooJrktAMtkTVYktqcJOnehtss4ts=&type=retrieve&user_id=";

    public void getUsersPage(final Context context, String userId, final SyncResult syncResult) {
        AQuery aq = new AQuery(context);
        aq.ajax(USERS_URL+userId, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                try {
                    updateLocalUserData(context,json, syncResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void updateLocalUserData(Context context,JSONObject data, final SyncResult syncResult)
            throws JSONException,
            RemoteException,
            OperationApplicationException {
        final Uri uri = UserObject.getCONTENT_URI();
        final String[] projection = UserObject.getProjection();
        final ContentResolver contentResolver = context.getContentResolver();


        JSONArray entries = data.getJSONArray("user");
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
            id = c.getInt(0);
            entryId = c.getString(1);
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
                    Field fileds[] = UserObject.class.getFields();
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
        contentResolver.applyBatch(DataContract.CONTENT_AUTHORITY, batch);
        contentResolver.notifyChange(
                UserObject.getCONTENT_URI(), // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

}
