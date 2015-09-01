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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vasilkoff.android.Sync.model.AppObject;
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
public class AppsSyncHelper {
    private static final String TAG = AppsSyncHelper.class.getSimpleName();
    static GsonBuilder builder = new GsonBuilder();
    static final Gson gson = builder.create();

    public static void updateLocalAppData(Context context,
                                          JSONObject data,
                                          final SyncResult syncResult
                                   )
            throws JSONException,
            RemoteException,
            OperationApplicationException {

        final Uri uri = AppObject.getCONTENT_URI();
        final String[] projection = AppObject.getProjection();
        final ContentResolver contentResolver = context.getContentResolver();

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
            id = c.getInt(0);
            entryId = c.getString(1);
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
        contentResolver.applyBatch(DataContract.CONTENT_AUTHORITY, batch);
        contentResolver.notifyChange(
                AppObject.getCONTENT_URI(), // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }
}
