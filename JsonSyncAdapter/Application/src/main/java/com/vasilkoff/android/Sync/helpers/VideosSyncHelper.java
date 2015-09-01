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
import com.vasilkoff.android.Sync.model.VideoObject;
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
public class VideosSyncHelper {

    public static final String TAG = VideosSyncHelper.class.getSimpleName();
    static GsonBuilder builder = new GsonBuilder();
    static final Gson gson = builder.create();

    public static void updateLocalVideoData(Context context,
                                            JSONObject data,
                                            final SyncResult syncResult)
            throws JSONException,
            RemoteException,
            OperationApplicationException {

        final Uri uri = VideoObject.getCONTENT_URI();
        final String[] projection = VideoObject.getProjection();
        final ContentResolver contentResolver = context.getContentResolver();




        JSONArray entries = data.getJSONArray("result");
        Log.i(TAG, "Parsing complete. Found " + entries.length() + " entries");


        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        HashMap<String, VideoObject> entryMap = new HashMap<String, VideoObject>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject e = entries.getJSONObject(i);
            VideoObject de = gson.fromJson(e.toString(),VideoObject.class);
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

            UsersSyncHelper.getUsersPage(e.author,syncResult);


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
        contentResolver.applyBatch(DataContract.CONTENT_AUTHORITY, batch);
        contentResolver.notifyChange(
                VideoObject.getCONTENT_URI(), // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network

        // TODO: load next page if need, but i think have to go different way, read DB and understand from it what needs to be loaded
        /*
        int pages = data.getInt("totalPages");
        videoPage = data.getInt("currentPage");
        if (pages>videoPage) {
            getVideoPage(videoPage+1,syncResult);
        }*/
    }

}
