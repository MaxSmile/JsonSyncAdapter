package com.vasilkoff.android.Sync.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.vasilkoff.android.Sync.db.SelectionBuilder;
import com.vasilkoff.android.Sync.model.AppObject;
import com.vasilkoff.android.Sync.model.VideoObject;

/**
 * Created by maxim.vasilkov@gmail.com on 22/08/15.
 */
public class DataProvider extends ContentProvider {

    Database mDatabaseHelper;

    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = DataContract.CONTENT_AUTHORITY;

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.


    public static final int ROUTE_VIDEOS = 1;
    public static final int ROUTE_VIDEOS_ID = 2;
    public static final int ROUTE_APPS = 3;
    public static final int ROUTE_APPS_ID = 4;
    public static final int ROUTE_USERS = 5;
    public static final int ROUTE_USERS_ID = 6;
    public static final int ROUTE_COMNTS = 7;
    public static final int ROUTE_COMNTS_ID = 8;




    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "videos", ROUTE_VIDEOS);
        sUriMatcher.addURI(AUTHORITY, "videos/*", ROUTE_VIDEOS_ID);
        sUriMatcher.addURI(AUTHORITY, "apps", ROUTE_APPS);
        sUriMatcher.addURI(AUTHORITY, "apps/*", ROUTE_APPS_ID);
        sUriMatcher.addURI(AUTHORITY, "users", ROUTE_USERS);
        sUriMatcher.addURI(AUTHORITY, "users/*", ROUTE_USERS_ID);
        sUriMatcher.addURI(AUTHORITY, "comments", ROUTE_COMNTS);
        sUriMatcher.addURI(AUTHORITY, "comments/*", ROUTE_COMNTS_ID);

    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new Database(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_VIDEOS:
                return VideoObject.getCONTENT_TYPE();
            case ROUTE_VIDEOS_ID:
                return VideoObject.getCONTENT_ITEM_TYPE();
            case ROUTE_APPS:
                return AppObject.getCONTENT_TYPE();
            case ROUTE_APPS_ID:
                return AppObject.getCONTENT_ITEM_TYPE();
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_VIDEOS_ID: {
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where("_id=?", id);
            }
            case ROUTE_VIDEOS: {
                builder.table(VideoObject.class.getSimpleName())
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            }
            case ROUTE_APPS_ID: {
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where("_id=?", id);
            }
            case ROUTE_APPS: {
                builder.table(AppObject.class.getSimpleName())
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_VIDEOS: {
                long id = db.insertOrThrow(VideoObject.class.getSimpleName(), null, values);
                result = Uri.parse(VideoObject.getCONTENT_URI() + "/" + id);
            } break;
            case ROUTE_APPS: {
                long id = db.insertOrThrow(AppObject.class.getSimpleName(), null, values);
                result = Uri.parse(AppObject.getCONTENT_URI() + "/" + id);
            } break;
            case ROUTE_VIDEOS_ID: case ROUTE_APPS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_VIDEOS: {
                count = builder.table(VideoObject.class.getSimpleName())
                        .where(selection, selectionArgs)
                        .delete(db);
            }break;
            case ROUTE_VIDEOS_ID: {
                String id = uri.getLastPathSegment();
                count = builder.table(VideoObject.class.getSimpleName())
                        .where("_id=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
            } break;
            case ROUTE_APPS:{
                count = builder.table(AppObject.class.getSimpleName())
                        .where(selection, selectionArgs)
                        .delete(db);
            } break;
            case ROUTE_APPS_ID: {
                String id = uri.getLastPathSegment();
                count = builder.table(AppObject.class.getSimpleName())
                        .where("_id=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
            } break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an etry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_VIDEOS:
                count = builder.table(VideoObject.class.getSimpleName())
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_VIDEOS_ID: {
                String id = uri.getLastPathSegment();
                count = builder.table(VideoObject.class.getSimpleName())
                        .where("_id=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
            } break;
            case ROUTE_APPS:
                count = builder.table(AppObject.class.getSimpleName())
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_APPS_ID: {
                String id = uri.getLastPathSegment();
                count = builder.table(AppObject.class.getSimpleName())
                        .where("_id=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
            } break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }


}
