package com.vasilkoff.android.UI;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.vasilkoff.android.R;
import com.vasilkoff.android.Sync.model.CommentObject;


public class MenuListActivity extends Activity
        implements MenuListFragment.Callbacks {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        getActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method from {@link MenuListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
            switch (id) {
                case "videos": {
                    Intent detailIntent = new Intent(this, VideosListActivity.class);
                    startActivity(detailIntent);
                } return;
                case "apps": {
                    Intent detailIntent = new Intent(this, AppsListActivity.class);
                    startActivity(detailIntent);
                } return;
                case "users": {
                    Intent detailIntent = new Intent(this, UsersListActivity.class);
                    startActivity(detailIntent);
                } return;
                case "feed": {
                    Intent detailIntent = new Intent(this, FeedListActivity.class);
                    startActivity(detailIntent);
                } return;
                case "comments": {
                    Intent detailIntent = new Intent(this, CommentsListActivity.class);
                    startActivity(detailIntent);
                } return;
                default:
                    Intent detailIntent = new Intent(this, FeedListActivity.class);
                    startActivity(detailIntent);
            }

    }
}
