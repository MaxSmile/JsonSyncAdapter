package com.vasilkoff.android.Sync.db;

/**
 * Created by maxim.vasilkov@gmail.com on 22/08/15.
 */
public class Video {
    public final String id;
    public final String title;
    public final String link;
    public final long published;



    public Video(String id, String title, String link, long published) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.published = published;
    }
}
