package com.example.wifiscan;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

public class PermissionsDialog extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setCancelable(false);

        View view = inflater.inflate(R.layout.dialog_permissions, container, false);
        view.findViewById(R.id.grantPermissionsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] permissions = {
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };

                requestPermissions(permissions, 200);
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 200) {
            for(int i = 0; i < permissions.length; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getContext(), String.format("Did not grant permissions to ID: %d", grantResults[i]), Toast.LENGTH_LONG).show();
                }

                setCancelable(true);
                dismiss();
            }
        }
    }
}
