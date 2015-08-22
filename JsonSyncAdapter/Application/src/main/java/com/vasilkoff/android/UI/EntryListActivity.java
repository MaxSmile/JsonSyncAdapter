package com.vasilkoff.android.UI;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.vasilkoff.android.R;


/**
 * Activity for holding EntryListFragment.
 */
public class EntryListActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);
    }
}
