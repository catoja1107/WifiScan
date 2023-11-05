package com.example.wifiscan;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WifiScanner {
    private Context context;
    private WifiScannerListener listener;
    private WifiManager wifiManager;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public WifiScanner(Context context) {
        this.context = context;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();

        this.listener = new WifiScannerListener() {
            @Override
            public void onWifiScanResult(ScanResult scanResult) {
                Map<String, Object> network = new HashMap<>();
                network.put("ssid", scanResult.SSID);
                network.put("bssid", scanResult.BSSID);
                network.put("frequency", scanResult.frequency); //convert to rssi
                network.put("uid", firebaseAuth.getCurrentUser().getUid());
                network.put("timestamp", FieldValue.serverTimestamp());

                firebaseFirestore.collection("network")
                        .add(network)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("WifiScanDebug", String.format("added document id: %s", documentReference.getId()));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("WifiScanDebug", "failed to add document");
                            }
                        });
            }
        };
    }

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

        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            listener.onWifiScanResult(scanResult);
        }
    }
}
