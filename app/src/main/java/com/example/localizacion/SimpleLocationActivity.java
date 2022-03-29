package com.example.localizacion;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class SimpleLocationActivity extends AppCompatActivity {
    TextView latitude, longitude, elevation;
    TextView distance;
    FusedLocationProviderClient fusedLocationProviderClient;
    boolean settingsOK=false;
    public static final double RADIUS_EARTH=6371, PUJ_LAT=4.628553003284733, PUJ_LON=-74.06467244207825;
    //locationRequest con google
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    //solicitar permiso
    ActivityResultLauncher<String> requestPermission= registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    //updateUI();
                    startLocationUpdates();
                }
            }
    );
    //encender GPS
    ActivityResultLauncher<IntentSenderRequest> getLocationSettings = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.i("LOCATION","Result from settings:"+result.getResultCode());
                    if(result.getResultCode()==RESULT_OK){
                        settingsOK=true;
                        startLocationUpdates();
                    }else{
                        elevation.setText("GPS is off");
                    }
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_location);
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        latitude = findViewById(R.id.textView5);
        longitude=findViewById(R.id.textView6);
        elevation=findViewById(R.id.textView7);
        locationRequest=createLocationRequest();
        locationCallback=createLocationCallBack();
        distance= findViewById(R.id.distance);
        requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);

       // updateUI();
        checkLocationSettings();
    }
    private void checkLocationSettings(){
        LocationSettingsRequest.Builder builder = new
                LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i("..", "GPS is ON");
                settingsOK = true;
                startLocationUpdates();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(((ApiException) e).getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED){
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    IntentSenderRequest isr = new IntentSenderRequest.Builder(resolvable.getResolution()).build();
                    getLocationSettings.launch(isr);
                }else {
                    elevation.setText("No GPS available");
                }
            }
        });
    }
    private LocationRequest createLocationRequest(){
        LocationRequest request= LocationRequest.create().setFastestInterval(5000).setInterval(10000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return request;
    }
    private LocationCallback createLocationCallBack(){
        return new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location lastLocation= locationResult.getLastLocation();
                if(lastLocation!=null){
                    Log.i("LOCATION", "Latitude:"+lastLocation.getLatitude());
                    latitude.setText(String.valueOf(lastLocation.getLatitude()));
                    longitude.setText(String.valueOf(lastLocation.getLongitude()));
                    elevation.setText(String.valueOf(lastLocation.getAltitude()));
                    double dist= distance(lastLocation.getLatitude(),lastLocation.getLongitude(),PUJ_LAT,PUJ_LON);
                    distance.setText(String.valueOf(dist));
                }
            }
        };
    }
    private void startLocationUpdates(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(settingsOK){
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);//looper: cada cuanto quiere que lo haga
            }
        }
    }
    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = RADIUS_EARTH * c;
        return Math.round(result*100.0)/100.0;
    }
    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }
    /*private void updateUI(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.i("LOCATION","onSucces location");
                    if(location!=null){
                        Log.i("LOCATION","Longitud"+location.getLongitude());
                        Log.i("LOCATION","Latitud"+location.getLatitude());
                        latitude.setText(String.valueOf(location.getLatitude()));
                        longitude.setText(String.valueOf(location.getLongitude()));
                        elevation.setText(String.valueOf(location.getAltitude()));
                    }
                }
            });
        }
    }*/
}