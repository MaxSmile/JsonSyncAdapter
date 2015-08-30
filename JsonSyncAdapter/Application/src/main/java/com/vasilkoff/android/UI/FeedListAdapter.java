package com.vasilkoff.android.UI;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;

import com.androidquery.AQuery;
import com.vasilkoff.android.R;

/**
 * Created maxim.vasilkov@gmail.com on 27/08/15.
 */
public class FeedListAdapter extends ResourceCursorAdapter {
    public FeedListAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        AQuery aq = new AQuery(view);
        final int colid = cursor.getColumnIndex("id");
        final int colauthor = cursor.getColumnIndex("author");
        final int colimage = cursor.getColumnIndex("bitmap_preview");
        String uid = cursor.getString(colid);
        String author = cursor.getString(colauthor);
        aq.id(R.id.textView1).text(uid);
        aq.id(R.id.textView2).text(author);

        aq.id(R.id.image).progress(R.id.progress).image(cursor.getString(colimage));
    }
}
