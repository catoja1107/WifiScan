package com.example.wifiscan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GoogleMap mMap;
    private MapView mMapView;

    public double lat = 0;
    public double longitude = 0;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mMapView.onResume();
        view.findViewById(R.id.scanOnceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WifiScanner wifiScanner = new WifiScanner(getActivity(), new WifiScannerListener() {
                    @Override
                    public void onWifiScanResult(List<ScanResult> scanResults) {
                        if (scanResults.size() == 0) {
                            return;
                        }
                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        HashMap<String, Object> data = new HashMap<>();
                        HashMap<String, Object> networks = new HashMap<>();
                        HashMap<String, Object> count = new HashMap<>();
                        int counter = 0;
                        int maxLevel = 0;
                        int minLevel = 0;
                        for (ScanResult scanResult : scanResults) {


                            HashMap<Object, Object> network = new HashMap<>();
                            network.put("ssid", scanResult.SSID);
                            network.put("db", scanResult.level);
                            network.put("frequency", scanResult.frequency);
                            network.put("capabilities", scanResult.capabilities);
                            network.put("latitude", lat);
                            network.put("longitude", longitude);

                            count.put("" + counter++, network);
                            count.put("timestamp", FieldValue.serverTimestamp());
                            if (scanResult.level>=minLevel){
                                count.put("minLevel", scanResult.level);
                                count.put("edge", "lat: " + lat + ", lng: " + longitude);
                            }
                            if (scanResult.level<=maxLevel){
                                count.put("maxLevel", scanResult.level);
                                count.put("center", "lat: " + lat + ", lng: " + longitude);
                            }

                            networks.put("" + scanResult.BSSID, count);
                            count.remove("" + counter);

                            firebaseFirestore.collection("userdata")
                                    .document(firebaseAuth.getUid())
                                    .set(networks, SetOptions.merge());

                        }

                        data.put("network_list", networks);
                        data.put("length", networks.size());
                        data.put("timestamp", FieldValue.serverTimestamp());

                        JSONObject jsonObject = new JSONObject(networks);
                        String orgJsonData = jsonObject.toString();
                        try {
                            FileWriter file = new FileWriter("map.json");
                            file.write(orgJsonData);
                            file.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                        //FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                        //FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                        //firebaseFirestore.collection("userdata")
                          //      .document(firebaseAuth.getUid())
                           //     .collection().update(data);

                        // Create a reference to the cities collection
                        //CollectionReference citiesRef = firebaseFirestore.collection("userdata")
                         //       .document(firebaseAuth.getUid()).collection();

// Create a query against the collection.
                        //Query query = citiesRef.whereEqualTo("state", "CA");
                    }
                });


                wifiScanner.executeScanOnce();
            }
        });

        return view;


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check and request location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Request permissions here
            return;
        }

        // Enable My Location layer on the map
        mMap.setMyLocationEnabled(true);

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Get the last known location
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        // Get the current location and move the camera to that location
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    }
                });

        // Set up location updates
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100000);




        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    Circle circle = googleMap.addCircle(new CircleOptions()
                            .center(currentLocation)
                            .radius(20)
                            .strokeColor(Color.RED)
                            .fillColor(Color.GREEN));
                    circle.setTag("test123");


                    lat = location.getLatitude();
                    longitude = location.getLongitude();
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));
                    File f = new File("map.json");

                    if (f.exists()) {
                        try {
                            // Read JSON file content as a string
                            String jsonContent = new String(Files.readAllBytes(f.toPath()));

                            // Parse the JSON string into a JSONObject
                            JSONObject jsonObject = new JSONObject(jsonContent);

                            // Get the "locations" array from the JSON object
                            JSONArray locationsArray = jsonObject.getJSONArray("networks");

                            // Iterate over each element in the array
                            for (int i = 0; i < locationsArray.length(); i++) {
                                // Get the current JSON object from the array
                                JSONObject locationObject = locationsArray.getJSONObject(i);


                                // Get latitude and longitude values from the current object
                                double latitude = locationObject.getDouble("latitude");
                                double longitude = locationObject.getDouble("longitude");
                                String bssid = locationObject.getString("bssid");

                                LatLng iterate = new LatLng(latitude, longitude);

                                mMap.addMarker(
                                        new AdvancedMarkerOptions()
                                                .position(iterate)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_background))
                                                .title(bssid));
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, null);
    }
}