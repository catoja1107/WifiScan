package com.example.wifiscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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
        String uid = FirebaseAuth.getInstance().getUid();

        Map<Object, Object> data = new HashMap<>();
        Map<String, Object> friends = new HashMap<>();
        data.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        data.put("friends", friends);

        DocumentReference document = firebaseFirestore.collection("userdata").document(uid);
        if(!document.get().isSuccessful()) {
            firebaseFirestore.collection("userdata").document(uid)
                    .set(data);
        }
    }
}