package com.example.wifiscan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NetworkDataSource dataSource = new NetworkDataSource(MainActivity.this);
        dataSource.open();
        dataSource.close();
    }
}