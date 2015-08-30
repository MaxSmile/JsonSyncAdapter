package com.vasilkoff.android.Sync.model;

import android.database.Cursor;

import java.lang.reflect.Field;

/**
 * Created by maxim.vasilkov@gmail.com on 24/08/15.
 */
public class ModelObject {

    public String id;

    public boolean isTheSame(Cursor record) {
        Field[] fields = VideoObject.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            try {
                if(!f.get(this).equals(record.getString(i)))return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static String[] getProjection(Class cl) {
        Field[] fields = cl.getFields();
        String[] strings = new String[fields.length+1];
        strings[0] = "_id";
        for (int i = 0; i < fields.length; i++) {
            strings[i + 1] = fields[i].getName();
        }
        return strings;
    }
}
