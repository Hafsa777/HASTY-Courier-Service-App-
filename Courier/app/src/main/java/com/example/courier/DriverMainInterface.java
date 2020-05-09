package com.example.courier;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class DriverMainInterface extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    //playServices
    private static final int my_permission_request_code = 7000;
    private static final int play_service_request_code = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mApiCLient;
    private Location mLastLocation;

    private int update_interval = 5000;
    private int fastest_interval = 3000;
    private int displacement = 10;

    DatabaseReference drivers;
    GeoFire mgeoFire;

    Marker mcurrent;
    MaterialAnimatedSwitch locatin_switch;

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main_interface);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //iNit view
        locatin_switch = (MaterialAnimatedSwitch) findViewById(R.id.location_swith);
        locatin_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {
                    startLocationUpdates();
                    displayLocation();
                    Toast.makeText(getApplicationContext(), "Online", Toast.LENGTH_SHORT).show();
                } else {

                    stopLocation();
                    mcurrent.remove();
                    Toast.makeText(getApplicationContext(), "You are Offline", Toast.LENGTH_SHORT).show();
                }
            }


        });

        drivers = FirebaseDatabase.getInstance().getReference("Driver Personal Info");
        mgeoFire = new GeoFire(drivers);
        setUpLocation();
    }

    private void setUpLocation() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED))) {

            // Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            }, my_permission_request_code);
        }
        else{
            if (checkPlayServices()){

                buildGoogleApiClient();
                createLocationequest();
                if(locatin_switch.isChecked()){
                    displayLocation();
                }
            }
        }
    }

    private void createLocationequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(update_interval);
        mLocationRequest.setFastestInterval(fastest_interval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(displacement);
    }

    private void buildGoogleApiClient() {
        mApiCLient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mApiCLient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,play_service_request_code).show();
            else {
                Toast.makeText(this,"This Device is not Supported",Toast.LENGTH_SHORT).show();
                finish();
        }
            return false;
        }
        return true;
    }

    private void stopLocation(){
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED))) {
            return;
        }

        FusedLocationApi.removeLocationUpdates(mApiCLient, (com.google.android.gms.location.LocationListener) this);
    }

    private void displayLocation() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED))) {
            return;
        }

        mLastLocation = FusedLocationApi.getLastLocation(mApiCLient);
        if(mLastLocation != null){

            if(locatin_switch.isChecked()){
                final double latitude = mLastLocation.getLatitude();
                final  double longtude = mLastLocation.getLongitude();

                //Update to firebase
                mgeoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longtude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Add a maker
                        if(mcurrent != null)
                            mcurrent.remove();
                        mcurrent = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.carImage))
                                                  .position(new LatLng(latitude,longtude))
                                                    .title("You"));
                        //Move camera to this position
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longtude),15.0f));

                        //Draw Animation rotate marker
                        rotateMarker(mcurrent,-360,mMap);
                    }
                });
            }

        }
    }

    private void rotateMarker(final Marker mcurrent, final int i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final float startRotaion = mcurrent.getRotation();
        final long duration = 1500;
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float)elapsed/duration);
                float rotation = t*i + (1-t) * startRotaion;
                mcurrent.setRotation(-rotation > 180?rotation/2:rotation);
                if(t<1.0){
                    handler.postDelayed(this,16);
                }
            }
        });

    }

    private void startLocationUpdates() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED))) {
            return;
        }

        FusedLocationApi.requestLocationUpdates(mApiCLient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onLocationChanged(Location location) {
            mLastLocation = location;
            displayLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
         displayLocation();
         startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mApiCLient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
