package com.example.localizacion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button simpleLocation;
    Button google, osm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Inflate
        simpleLocation = findViewById(R.id.simpleLocation);
        google = findViewById(R.id.google);
        osm = findViewById(R.id.osm);


        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GoogleActivity.class);
                startActivity(intent);
            }
        });


        simpleLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), SimpleLocationActivity.class));
            }
        });
    }
}