package com.example.small.onetouchbutton10;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {

    private GoogleMap mMap;
    private Button mPlace;
    private Button mRegister;

    final int PLACE_PICKER_REQUEST = 1;

    LocationManager mLocMan;
    GoogleApiClient googleApiClient;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    static LatLng latLng = new LatLng(0,0);
    static LatLng register = new LatLng(0,0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mPlace = (Button)findViewById(R.id.button_place);
        mRegister = (Button)findViewById(R.id.button_register);
        mLocMan = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        isGPSEnabled = mLocMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                try{
                    Intent intent = intentBuilder.build(MapsActivity.this);
                    startActivityForResult(intent,PLACE_PICKER_REQUEST);
                }catch(GooglePlayServicesRepairableException e){
                    Log.d("Google Place", "GooglePlayServicesRepairableException이얌");
                }catch (GooglePlayServicesNotAvailableException e){
                    Log.d("Google Place", "GooglePlayServicesNotAvailableException 이얌");
                }
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MapsActivity", "클릭 리스너는 작동됨");
                Intent result = new Intent();
                result.putExtra("Lat",register.latitude);
                result.putExtra("Lng",register.longitude);
                setResult(RESULT_OK,result);
                finish();
            }
        });


    }

    @Override
    protected  void onStart(){
        super.onStart();
        if(googleApiClient != null)
            googleApiClient.connect();
    }

    @Override
    protected void onStop(){
        if(googleApiClient != null && googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK){
            final Place place = PlacePicker.getPlace(this,data);

            latLng = place.getLatLng();
            setMapPostion(latLng);
        }else
        {
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(MapsActivity.this, "ACCESS_COARSE_LOCATIOIN 퍼미션 체크",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){}

    @Override
    public void onConnectionSuspended(int t){}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {return;}

        // Add a marker in Sydney and move the camera
        LatLng seoul = new LatLng(37.519576, 126.940245);
        CameraPosition seo = new CameraPosition.Builder().target((seoul)).zoom(16).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(seo));
        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point));
                register = point;
            }
        });
    }

    private void setMapPostion(LatLng latLng){
        CameraPosition cp = new CameraPosition.Builder().target((latLng)).zoom(18).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    }
}
