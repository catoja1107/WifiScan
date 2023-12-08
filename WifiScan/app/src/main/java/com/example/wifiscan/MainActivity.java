package com.example.wifiscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        SetupClient();

        if(!((this.checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)
        && (this.checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)
        && (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        && (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))) {
            PermissionsDialog permissionsDialog = new PermissionsDialog();
            permissionsDialog.show(getSupportFragmentManager(), null);
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                if(fragmentManager.findFragmentByTag(item.toString()) != null) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_layout, fragmentManager.findFragmentByTag(item.toString()), item.toString())
                            .setReorderingAllowed(true)
                            .commit();

                    Log.d("WifiScanDebug", String.format("fragment %s was replaced", item.toString()));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                switch (item.toString()) {
                    case "Home":
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_layout, HomeFragment.class, null, item.toString())
                                .setReorderingAllowed(true)
                                .addToBackStack(null)
                                .commit();
                        break;
                    case "Settings":
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_layout, SettingsFragment.class, null, item.toString())
                                .setReorderingAllowed(true)
                                .addToBackStack(null)
                                .commit();
                        break;
                    default:
                        return false;
                }

                Log.d("WifiScanDebug", String.format("fragment %s was created", item.toString()));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_layout, HomeFragment.class, null, "Home")
                .setReorderingAllowed(true)
                .commit();
    }

    private void SetupClient() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        String uid = firebaseAuth.getUid();
        Map<Object, Object> data = new HashMap<>();
        Map<String, Object> friends = new HashMap<>();
        data.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        data.put("friends", friends);

        DocumentReference document = firebaseFirestore.collection("userdata").document(uid);
        if(!document.get().isSuccessful()) {
            firebaseFirestore.collection("userdata").document(uid)
                    .set(data);
        }

        CollectionReference networksRef = firebaseFirestore.collection("userdata").document(firebaseAuth.getUid()).collection("networks");
        networksRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()) {
                    HashMap<String, Object> bucketData = new HashMap<>();
                    bucketData.put("count", 0);

                    DocumentReference bucket = networksRef.document();
                    bucket.set(bucketData);

                    HashMap<String, Object> bucketIndex = new HashMap<>();
                    HashMap<String, Object> bucketIData = new HashMap<>();
                    bucketIData.put("lists", new ArrayList<>());
                    bucketIData.put("count", 0);
                    bucketIndex.put(bucket.getId(), bucketIData);

                    networksRef.document("bucket_index").set(bucketIndex);
                }
            }
        });

        networksRef.document("bucket_index").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(Bucket.DEBUG_TAG, task.getResult().getId());
                    Bucket.bucket_index = (HashMap<String, Object>) task.getResult().getData();

                    assert Bucket.bucket_index != null;
                    for(String key : Bucket.bucket_index.keySet()) {
                        networksRef.document(key).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()) {
                                    Bucket.buckets.put(key, task.getResult().getData());
                                    Log.d(Bucket.DEBUG_TAG, String.format("loaded bucket: %s", key));

                                    //Bucket.createSnapshotListener(key);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}