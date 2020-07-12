package gamal.myappnew.shipperside;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gamal.myappnew.shipperside.Common.Common;
import info.hoang8f.widget.FButton;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    //    LocationRequest locationRequest;
//    LocationCallback locationCallback;
//    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private final static int PLAY_SERVIECS_REQUESTS = 1000;
    private final static int LOCATION_SERVIECS_REQUESTS = 1001;
    private Location mLastloaction;
    GoogleApiClient mgoogleApiClient;
    LocationRequest mlocationRequest;
    private static int UPDATE_INTERVAL = 1500;
    private static int FAST_INTERVAL = 5000;
    private static int DISPLAYCEMINT = 10;
    public LatLng locationorder;
    FButton btncall, btnshipped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btncall = findViewById(R.id.btncall);
        btnshipped = findViewById(R.id.btnshipped);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_COARSE_LOCATION}
                        , 1);

            } else {
                if (CheckPlayServiec()) {
                    buildGoogleApiClient();
                    CreateLocationRequests();
                }
            }
            displayLocation();
        }
        btnshipped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShippedOrder();

            }
        });
        btncall.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Common.CURRENTREQUEST.getPhone()));
                if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                startActivity(intent);
            }
        });

    }

    private void ShippedOrder() {
        FirebaseDatabase.getInstance().getReference(Common.ORDERNEEDSHIP_TABLE)
                .child(Common.CURRENTSHIPER.getPhone())
                .child(Common.CurrentKey)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                HashMap<String,Object> hashMap=new HashMap<>();
                hashMap.put("status","3");
                FirebaseDatabase.getInstance().getReference("Request")
                        .child(Common.CurrentKey).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseDatabase.getInstance().getReference(Common.ShipperInfo_TABLE)
                                .child(Common.CurrentKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MapsActivity.this, "Shipped!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        boolean issuccess=mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_style));
        if(!issuccess)
            Log.i("ERROR","Error in stylr");

        // Add a marker in Sydney and move the camera


    }
    private void displayLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED&&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED&&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.CALL_PHONE}
                        , 1);

            } else {

                mLastloaction= LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);
                if (mLastloaction!=null)
                {
                    Geocoder geocoder=new Geocoder(getApplicationContext());
                    try {

                        List<Address> addressList=  geocoder.getFromLocationName(Common.CURRENTREQUEST.getAdress(), 1);
                        if (addressList!=null&&addressList.size()>0) {
                            Address address = addressList.get(0);
                       locationorder=new LatLng(address.getLatitude(),address.getLongitude());
                            MarkerOptions marker=new MarkerOptions()
                                    .title("Order of "+Common.CURRENTREQUEST.getPhone())
                                    .snippet("Name is :"+Common.CURRENTREQUEST.getName())
                                    .position(locationorder)
                                    .icon(BitmapDescriptorFactory.defaultMarker());

                            mMap.addMarker(marker);
                            Log.i("jfkewjbkew","Done");
                        }else {
                            Log.i("jfkewjbkew","null");

                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i("jfkewjbkew","ERROR");
                    }

                    LatLng shipper = new LatLng(mLastloaction.getLatitude(), mLastloaction.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(shipper).title("Your Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .snippet( Common.CURRENTSHIPER.getName()+" , you is a Shipper"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(shipper));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f));
                    Common.Updateshippinginfo(Common.CurrentKey,mLastloaction);

                    Polyline polyline=mMap.addPolyline(new PolylineOptions()
                            .add(shipper,locationorder)
                            .width(5)
                            .color(Color.BLUE));


                }else {
                    Log.i("ERRROR","coud't get location");
                }
            }
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startlocationUpdates();
    }

    private void startlocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED&&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return;

            }
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient,mlocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mgoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }

    @Override
    public void onLocationChanged(Location location) {
        mLastloaction=location;
        displayLocation();

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length>0) {
                    if (CheckPlayServiec())
                    {
                        buildGoogleApiClient();
                        CreateLocationRequests();
                        displayLocation();
                    }

                } else {
                    // Permission Denied
                    Toast.makeText(this, "Can't Access your location", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void CreateLocationRequests() {
        mlocationRequest=new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL);
        mlocationRequest.setFastestInterval(FAST_INTERVAL);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(DISPLAYCEMINT);
    }

    protected synchronized void buildGoogleApiClient() {
        mgoogleApiClient=new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API).build();
        mgoogleApiClient.connect();

    }

    private boolean CheckPlayServiec() {
        int resualtcode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resualtcode!=ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resualtcode)) {
                GooglePlayServicesUtil.getErrorDialog(resualtcode, this, PLAY_SERVIECS_REQUESTS).show();
            } else {
                Toast.makeText(this, "This device not support ", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        CheckPlayServiec();
    }


    @Override
    protected void onStart() {
        super.onStart();
        displayLocationSettingsRequest(getApplicationContext());
        if (mgoogleApiClient!=null)
        {
            mgoogleApiClient.connect();
        }
    }
    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, 1);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }
}
