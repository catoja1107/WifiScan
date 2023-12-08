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
    private List<ScanResult> lastScanResults;

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

        // ...

        // Initialize the ListView
        wifiListView = view.findViewById(R.id.wifiList);

        view.findViewById(R.id.loadbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


                firebaseFirestore.collection("userdata")
                        .document(firebaseAuth.getUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Data found, update the WiFi ListView
                                HashMap<String, Object> data = (HashMap<String, Object>) documentSnapshot.getData();
                                updateWifiListViewFromFirebase(data);
                            } else {
                                // No data found
                                // Handle this case as needed
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle failures (e.g., network issues, permission issues)
                            e.printStackTrace();
                        });
            }
        });

        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the clicked WiFi network details
                String ssid = (String) wifiListView.getItemAtPosition(i);
                ScanResult clickedNetwork = findClickedNetwork(ssid);


                if (clickedNetwork != null) {
                    showNetworkDetailsDialog(clickedNetwork);
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
    // Method to update ListView with scan results
    private void updateWifiListViewFromFirebase(HashMap<String, Object> data) {
        // Extract WiFi network names (SSIDs) from the data
        List<String> wifiNetworks = new ArrayList<>();
        if (data.containsKey("networks")) {
            HashMap<String, Object> networks = (HashMap<String, Object>) data.get("networks");
            for (String bssid : networks.keySet()) {
                HashMap<String, Object> networkData = (HashMap<String, Object>) networks.get(bssid);
                wifiNetworks.add(networkData.get("ssid").toString());

            }
        }

        // Use an ArrayAdapter to populate the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, wifiNetworks);
        wifiListView.setAdapter(adapter);
    }

    // Method to find the clicked network in the scan results
    private ScanResult findClickedNetwork(String ssid) {

        for (ScanResult scanResult : lastScanResults) {
            if (scanResult.SSID.equals(ssid)) {
                return scanResult;
            }
        }
        return null;
    }

    // Method to show more details in a dialog
    private void showNetworkDetailsDialog(ScanResult scanResult) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Network Details");
        builder.setMessage("SSID: " + scanResult.SSID + "\n"
                + "BSSID: " + scanResult.BSSID + "\n"
                + "Signal Strength: " + scanResult.level + " dBm" + "\n"
                + "Frequency: " + scanResult.frequency + " MHz" + "\n"
                + "Capabilities: " + scanResult.capabilities +"\n"
                + scanResult.channelWidth);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle OK button click if needed
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}