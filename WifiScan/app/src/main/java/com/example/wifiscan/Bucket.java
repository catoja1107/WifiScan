package com.example.wifiscan;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bucket {
    public final static String DEBUG_TAG = "WifiScanBucket";
    public static HashMap<String, Object> bucket_index;
    public static HashMap<String, Object> buckets = new HashMap<>();

    public static String isInIndex(String bssid) {
        for(String key : bucket_index.keySet()) {
            HashMap<String, Object> index = (HashMap<String, Object>) bucket_index.get(key);
            if((Long)index.get("count") == 0) {
                continue;
            }

            ArrayList<String> list = (ArrayList<String>)index.get("lists");
            if(list.contains(bssid)) {
                return key;
            }
        }

        return null;
    }
}
