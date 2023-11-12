package com.example.wifiscan;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class WifiScanner {
    private Context context;
    private WifiScannerListener listener;
    private WifiManager wifiManager;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public WifiScanner(Context context, WifiScannerListener listener) {
        this.context = context;
        this.listener = listener;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public void executeScanOnce() {
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

        List<ScanResult> scanResults = wifiManager.getScanResults();
        listener.onWifiScanResult(scanResults);
    }
}
