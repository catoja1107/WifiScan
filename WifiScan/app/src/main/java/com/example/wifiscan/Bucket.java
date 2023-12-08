package com.example.wifiscan;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bucket {
    public final static String DEBUG_TAG = "WifiScanBucket";
    public static HashMap<String, Object> bucket_index;
    public static HashMap<String, Object> buckets = new HashMap<>();

    public static String isInIndex(String bssid) {
        String bucket_id = null;
        for(String key : bucket_index.keySet()) {
            HashMap<String, Object> index = (HashMap<String, Object>) bucket_index.get(key);
            Long bucketSize = (Long)index.get("count");
//            if(bucketSize == 0) {
//                continue;
//            }

            ArrayList<String> list = (ArrayList<String>)index.get("lists");
            if(list.contains(bssid)) {
                return key;
            }

            if(bucketSize <= 100) {
                bucket_id = key;
                //will need to take care of bucket creation for over 100 count
            }
        }

        return bucket_id;
    }

    public static HashMap<String, Object> getBucket(String key) {
        return (HashMap<String, Object>) buckets.get(key);
    }
}
