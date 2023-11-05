package com.example.wifiscan;

import android.net.wifi.ScanResult;

public interface WifiScannerListener {
    void onWifiScanResult(ScanResult scanResults);
}
