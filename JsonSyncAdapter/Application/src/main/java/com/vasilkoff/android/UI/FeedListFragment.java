package com.vasilkoff.android.UI;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.vasilkoff.android.Account.AccountService;
import com.vasilkoff.android.R;
import com.vasilkoff.android.Sync.SyncService;
import com.vasilkoff.android.Sync.SyncUtils;
import com.vasilkoff.android.Sync.model.AppObject;
import com.vasilkoff.android.Sync.model.FeedObject;
import com.vasilkoff.android.Sync.model.UserObject;
import com.vasilkoff.android.Sync.model.VideoObject;
import com.vasilkoff.android.Sync.provider.DataContract;
import com.vasilkoff.android.Sync.provider.Database;


public class FeedListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = FeedListFragment.class.getSimpleName();

    private FeedListAdapter mAdapter;

    private Object mSyncObserverHandle;

    /**
     * Options menu used to populate ActionBar.
     */
    private Menu mOptionsMenu;


    public FeedListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        SyncUtils.CreateSyncAccount(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SQLiteDatabase db = new Database(getContext()).getReadableDatabase();
        String query = "SELECT * FROM " +
                VideoObject.class.getSimpleName()+" v," +
                UserObject.class.getSimpleName()+" u," +
                AppObject.class.getSimpleName() + " a WHERE v.recorded_app=a.id AND u.id=v.author";

        Cursor cursor = db.rawQuery(query, null);

        mAdapter = new FeedListAdapter(getContext(),R.layout.feed_row,cursor,0);

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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri URI = FeedObject.getCONTENT_URI();
        String[] Projection = FeedObject.getProjection();
        return new CursorLoader(getActivity(),URI,Projection,null,null,null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

        mAdapter.changeCursor(null);
    }


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
                    Account account = AccountService.GetAccount(getActivity().getString(R.string.ACCOUNT_TYPE));
                    if (account == null) {
                        setRefreshActionButtonState(false);
                        return;
                    }
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