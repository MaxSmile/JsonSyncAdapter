/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vasilkoff.android.UI;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.vasilkoff.android.Account.AccountService;
import com.vasilkoff.android.R;
import com.vasilkoff.android.Sync.SyncService;
import com.vasilkoff.android.Sync.SyncUtils;
import com.vasilkoff.android.Sync.model.VideoObject;
import com.vasilkoff.android.Sync.provider.DataProvider;
import com.vasilkoff.android.Sync.provider.DataContract;


public class VideosListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = VideosListFragment.class.getSimpleName();

    /**
     * Cursor adapter for controlling ListView results.
     */
    private VideoListAdapter mAdapter;

    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     *
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object mSyncObserverHandle;

    /**
     * Options menu used to populate ActionBar.
     */
    private Menu mOptionsMenu;




    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public VideosListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Create SyncAccount at launch, if needed.
     *
     * <p>This will create a new account with the system for our application, register our
     * {@link SyncService} with it, and establish a sync schedule.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Create account, if needed
        SyncUtils.CreateSyncAccount(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new VideoListAdapter(getContext(),R.layout.video_list_row,null,0);

//        mAdapter = new SimpleCursorAdapter(
//                getActivity(),       // Current context
//                android.R.layout.simple_list_item_activated_2,  // Layout for individual rows
//                null,                // Cursor
//                FROM_COLUMNS,        // Cursor columns to use
//                TO_FIELDS,           // Layout fields to use
//                0                    // No flags
//        );
//
//        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
//            @Override
//            public boolean setViewValue(View view, Cursor cursor, int i) {
//                final int imageColumn = cursor.getColumnIndex("bitmap_preview");
//                if (i == imageColumn) {
//                    aq.id(R.id.imageView).image(cursor.getString(cursor.getColumnIndex("bitmap_preview")));
//                    return true;
//                } else {
//                    // Let SimpleCursorAdapter handle other fields automatically
//                    return false;
//                }
//            }
//        });
        setListAdapter(mAdapter);
        setEmptyText(getText(R.string.loading));
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    /**
     * Query the content provider for data.
     *
     * <p>Loaders do queries in a background thread. They also provide a ContentObserver that is
     * triggered when data in the content provider changes. When the sync adapter updates the
     * content provider, the ContentObserver responds by resetting the loader and then reloading
     * it.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // We only have one loader, so we can ignore the value of i.
        // (It'll be '0', as set in onCreate().)
        return new CursorLoader(getActivity(),  // Context
                VideoObject.getCONTENT_URI(), // URI
                VideoObject.getProjection(),         // Projection
                null,                           // Selection
                null,                           // Selection args
                ""); // Sort
    }

    /**
     * Move the Cursor returned by the query into the ListView adapter. This refreshes the existing
     * UI with the data in the Cursor.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    /**
     * Called when the ContentObserver defined for the content provider detects that data has
     * changed. The ContentObserver resets the loader, and then re-runs the loader. In the adapter,
     * set the Cursor value to null. This removes the reference to the Cursor, allowing it to be
     * garbage-collected.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.changeCursor(null);
    }

    /**
     * Create the ActionBar.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        inflater.inflate(R .menu.main, menu);
    }

    /**
     * Respond to user gestures on the ActionBar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                SyncUtils.TriggerRefresh(getContext());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Load an article in the default browser when selected by the user.
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Cursor c = (Cursor) mAdapter.getItem(position);
        if (!c.isClosed()) {
            final int colId = c.getColumnIndex("id");
            String t = c.getString(colId);
            if (t == null) {
                Log.e(TAG, "Attempt to launch entry with null link");
                return;
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("Selected")
                    .setMessage(t)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // I <3 Android :)
                        }
                    }).show();
        }
    }

    /**
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occuring, false otherwise
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setRefreshActionButtonState(boolean refreshing) {
        if (mOptionsMenu == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    /**
     * Crfate a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = AccountService.GetAccount(getActivity().getString(R.string.ACCOUNT_TYPE));
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setRefreshActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, DataContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, DataContract.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

}