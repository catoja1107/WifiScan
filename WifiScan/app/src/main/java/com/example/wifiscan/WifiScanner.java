package com.example.wifiscan;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;

import androidx.core.app.ActivityCompat;

public class WifiScanner {
    private Context context;
    private WifiScannerListener listener;
    private WifiManager wifiManager;

    public WifiScanner(Context context, WifiScannerListener listener) {
        this.context = context;
        this.listener = listener;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void startScan() {
        wifiManager.startScan();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        listener.onWifiScanResults(wifiManager.getScanResults());
    }
}
