package com.example.wifiscan;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class NetworkDataSource {
    private SQLiteDatabase database;
    private DBNetworkHelper dbNetworkHelper;
    private Context context;

    public NetworkDataSource(Context context) {
        dbNetworkHelper = new DBNetworkHelper(context);
    }

    public void open() throws SQLException {
        database = dbNetworkHelper.getWritableDatabase();
    }

    public void close() {
        dbNetworkHelper.close();
    }

    public void insert(Network network) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("time_created", String.format("%d", System.currentTimeMillis() / 1000));
        //will use time_detector service with their intent to get unix time instead of this method later
        initialValues.put("ssid", network.getSsid());
        initialValues.put("latitude", network.getLatitude());
        initialValues.put("longitude", network.getLongitude());
        initialValues.put("mac_address", network.getMac_address());
        initialValues.put("wpa", network.getWpa());
        initialValues.put("strength", network.getStrength());

        if(network.getElevation() != -1) {
            initialValues.put("elevation", network.getElevation());
        }
    }
}
