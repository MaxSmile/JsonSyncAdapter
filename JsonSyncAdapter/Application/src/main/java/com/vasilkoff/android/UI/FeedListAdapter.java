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
        final int colapp_name = cursor.getColumnIndex("app_name");

        String uid = cursor.getString(colid);
        String author = cursor.getString(colauthor);
        String app_name = (colapp_name>=0)?cursor.getString(colapp_name):null;
        aq.id(R.id.text_name).text(author);
        aq.id(R.id.app_recorded_name).text(uid);
        if (app_name!=null)
            aq.id(R.id.first_comment).text(app_name);

        aq.id(R.id.volley_square_preview)//.progress(R.id.progress)
                .image(cursor.getString(colimage));
    }
}
