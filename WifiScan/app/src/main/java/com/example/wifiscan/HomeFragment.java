package com.example.wifiscan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnCircleClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public HashMap<String, Object> bucketData = new HashMap<>();

    public HashMap<String, Circle> circles = new HashMap<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GoogleMap mMap;
    private MapView mMapView;

    public TextView mTagText;

    public double lat = 0;
    public double longitude = 0;
    private HashMap<String, Object> networks = new HashMap<>();
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
        mTagText = (TextView) view.findViewById(R.id.tag_text);
        mTagText.setText("testing");
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        mMapView.onResume();

        view.findViewById(R.id.scanOnceButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Bucket.bucket_index == null) {
                    Toast.makeText(getActivity(), "bucket_index hasn't been loaded yet", Toast.LENGTH_LONG);
                    return;
                }

                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                String uid = firebaseAuth.getUid();

                WifiScanner wifiScanner = new WifiScanner(getActivity(), new WifiScannerListener() {
                    @Override
                    public void onWifiScanResult(List<ScanResult> scanResults) {
                        if (scanResults.size() == 0) {
                            return;
                        }

                        //theres only going to be one bucket for testing so im just adding placeholder
                        String bucketKey = Bucket.isInIndex("placeholder");
                        ArrayList<String> bssidList = (ArrayList<String>) ((HashMap<String, Object>)Bucket.bucket_index.get(bucketKey)).get("lists");

                        HashMap<String, Object> bucket = Bucket.getBucket(bucketKey);
                        for (ScanResult scanResult : scanResults) {
                            if(!bssidList.contains(scanResult.BSSID)) {
                                bssidList.add(scanResult.BSSID);
                            }

                            bucketKey = Bucket.isInIndex(scanResult.BSSID);
                            if(bucketKey == null) {
                                //need to create new bucket here if over 100
                                break;
                            }


                            HashMap<String, Object> network = new HashMap<>();

                            network.put("ssid", scanResult.SSID);
                            network.put("db", scanResult.level);
                            network.put("latitude", lat);
                            network.put("longitude", longitude);

                            if(bucket.containsKey(scanResult.BSSID)) {
                                bucketData = (HashMap<String, Object>) bucket.get(scanResult.BSSID);
                                Long count = ((Number)bucketData.get("count")).longValue();

                                bucketData.put("" + count, network);

                                if(((Number)bucketData.get("min_level")).longValue() >= scanResult.level) {
                                    bucketData.put("min_level", scanResult.level);
                                    bucketData.put("min_latitude", lat);
                                    bucketData.put("min_longitude", longitude);
                                }

                                if(((Number)bucketData.get("max_level")).longValue() <= scanResult.level) {
                                    bucketData.put("max_level", scanResult.level);
                                    bucketData.put("max_latitude", lat);
                                    bucketData.put("max_longitude", longitude);
                                }

                                count++;
                                bucketData.put("count", count);
                            } else {
                                bucketData.put("0", network);
                                bucketData.put("count", 1);
                                bucketData.put("min_level", scanResult.level); //can only be determined after second data so we're using the first
                                bucketData.put("min_latitude", lat);
                                bucketData.put("min_longitude", longitude);
                                bucketData.put("max_level", scanResult.level);
                                bucketData.put("max_latitude", lat);
                                bucketData.put("max_longitude", longitude);
                                bucket.put(scanResult.BSSID, bucketData);
                            }
                        }

                        if(bucketKey == null) {
                            Log.w(Bucket.DEBUG_TAG, "did not get bucket");
                            return;
                        }

                        Bucket.buckets.put(bucketKey, bucket);

                        firebaseFirestore.collection("userdata").document(uid).collection("networks").document(bucketKey).set(bucket);
                        firebaseFirestore.collection("userdata").document(uid).collection("networks").document("bucket_index").set(Bucket.bucket_index);
                    }
                });

                wifiScanner.executeScanOnce();
            }
        });

        return view;

    }

    private static class CustomTag {
        private final String description;
        private int clickCount;

        public CustomTag(String description) {
            this.description = description;
            clickCount = 0;
        }

        public void incrementClickCount() {
            clickCount++;
        }

        @Override
        public String toString() {
            return "AP " + description + " has been clicked " + clickCount + " times.";
        }
    }

    @Override
    public void onMapReady(GoogleMap mMap) {

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
        // Check and request location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Request permissions here
            return;
        }

        // Enable My Location layer on the map
        mMap.setMyLocationEnabled(true);
        mMap.setOnCircleClickListener(this);

        //Double lat = Double.parseDouble(networks.get("centerLat").toString());
        //Double longitude = Double.parseDouble(networks.get("centerLong").toString());

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Get the last known location
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        // Get the current location and move the camera to that location
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        //mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));
                    }
                });

        // Set up location updates
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    if(Bucket.bucket_index == null) {
                        Log.w(Bucket.DEBUG_TAG, "bucket_index hasn't loaded yet [location]");
                        return;
                    }

                    String bucketKey = Bucket.isInIndex("placeholder");
                    HashMap<String, Object> bucket = (HashMap<String, Object>) Bucket.buckets.get(bucketKey);

                    if(bucket == null) {
                        Log.w(Bucket.DEBUG_TAG, String.format("%s bucket hasn't loaded yet [location]", bucketKey));
                        return;
                    }

                    for(String bssid : bucket.keySet()) {
                        if(bssid.equals("count")) {
                            continue;
                        }
                        HashMap<String, Object> network = (HashMap<String, Object>) bucket.get(bssid);
                        Double min_lat = ((Number) network.get("min_latitude")).doubleValue();
                        Double min_long = ((Number) network.get("min_longitude")).doubleValue();
                        Double max_lat = ((Number) network.get("max_latitude")).doubleValue();
                        Double max_long = ((Number) network.get("max_longitude")).doubleValue();

                        Double delta_lat = max_lat-min_lat;
                        Double delta_long = max_long-min_long;
                        Integer pythag = (int)(Math.sqrt(delta_lat*delta_lat)+(delta_long*delta_long));
                        LatLng apLocation = new LatLng(max_lat, max_long);

                        if (!circles.containsKey(bssid)) {
                            circles.put(bssid, mMap.addCircle(new CircleOptions()
                                    .center(apLocation)
                                    .radius((pythag+2)*5)
                                    .strokeColor(Color.BLUE)
                                    .fillColor(Color.GREEN)));
                            circles.get(bssid).setClickable(true);
                            circles.get(bssid).setTag(new CustomTag(bssid));
                        }

                        if (circles.containsKey(bssid) && circles.get(bssid).getCenter() != apLocation) {
                            circles.get(bssid).setCenter(apLocation);
                        }
                    }

                    lat = location.getLatitude();
                    longitude = location.getLongitude();
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

                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }, null);

    }

    private void onClick(CustomTag tag) {
        tag.incrementClickCount();
        if (tag != null) {
            mTagText.setText(tag.toString());
        } else {
            Log.e("TAG: ", "What in tarnation?");
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onCircleClick(@NonNull Circle circle) {
        onClick((CustomTag) circle.getTag());
        //for (Circle value : circles.values()) {
        //  onClick((CustomTag) circles.get(value).getTag());
        //}
    }
}