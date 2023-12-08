package com.example.wifiscan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WifiListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ListView wifiListView;

    public WifiListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WifiListFragment newInstance(String param1, String param2) {
        WifiListFragment fragment = new WifiListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_wifi_list, container, false);
        wifiListView = view.findViewById(R.id.wifiList);
        ArrayList<String> bssidList = new ArrayList<>();
        HashMap<String, Object> bucket = null;

        for(String key : Bucket.buckets.keySet()) {
            bucket = Bucket.getBucket(key);
            for(String bssid : bucket.keySet()) {
                if(bssid.equals("count")) continue;
                bssidList.add(bssid);
            }
        }

        if(bucket == null) {
            Toast.makeText(getActivity(), "could not load bucket", Toast.LENGTH_LONG);
            return view;
        }

        wifiListView.setAdapter(new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, bssidList));
        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //temporary solution. will need to create tuples to get bucket key. this will only work with 1 bucket
                HashMap<String, Object> bucket = null;

                for(String key : Bucket.buckets.keySet()) {
                    bucket = Bucket.getBucket(key);
                }

                if(bucket == null) {
                    Toast.makeText(getActivity(), "could not load bucket inside handler", Toast.LENGTH_LONG);
                    return;
                }

                String bssid = bssidList.get(position);
                HashMap<String, Object> data = (HashMap<String, Object>) bucket.get(bssid);
                Long index = ((Number)data.get("count")).longValue() - 1;
                HashMap<String, Object> network = (HashMap<String, Object>) data.get("" + index);

                String ssid = (String) network.get("ssid");
                String latitude = network.get("latitude").toString();
                String longitude = network.get("longitude").toString();
                String db = network.get("db").toString();

                new AlertDialog.Builder(getActivity())
                        .setTitle(bssid)
                        .setMessage(String.format("BSSID %s\nSSID: %s\nLatitude: %s\nLongitude: %s\nDB: %s\n", bssid, ssid, latitude, longitude, db))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create().show();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}