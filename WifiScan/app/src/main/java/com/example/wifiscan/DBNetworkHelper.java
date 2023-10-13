package com.example.wifiscan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBNetworkHelper extends SQLiteOpenHelper {
    private static final String db_name = "wifiscan.db";
    private static final int db_ver = 1;
    private static final String network_table = "create table networks(_id integer primary key autoincrement, time_created text not null, ssid text not null, latitude real not null, longitude real not null, elevation integer, mac_address text not null, wpa text not null, strength real not null);";

    public DBNetworkHelper(Context context) {
        super(context, db_name, null, db_ver);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(network_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
}