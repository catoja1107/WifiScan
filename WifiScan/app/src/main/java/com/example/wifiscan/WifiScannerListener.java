package com.example.wifiscan;

import android.net.wifi.ScanResult;

import java.util.List;

public interface WifiScannerListener {
    void onWifiScanResults(List<ScanResult> scanResults);
}
