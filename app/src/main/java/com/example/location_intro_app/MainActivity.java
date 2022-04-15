package com.example.location_intro_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.button);


        btn.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, MapActivity.class);
            startActivity(i);
        });
    }
}