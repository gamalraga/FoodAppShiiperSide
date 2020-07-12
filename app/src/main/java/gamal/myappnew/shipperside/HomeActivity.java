
package gamal.myappnew.shipperside;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import gamal.myappnew.shipperside.Common.Common;
import gamal.myappnew.shipperside.Moduel.Request;
import gamal.myappnew.shipperside.Moduel.ViewHolderRequest;
import gamal.myappnew.shipperside.Notification.LisientServer;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class HomeActivity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks ,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
//    LocationRequest locationRequest;
//    LocationCallback locationCallback;
//    FusedLocationProviderClient fusedLocationProviderClient;
// Location currentlocation;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private final static int PLAY_SERVIECS_REQUESTS=1000;
    private final static int LOCATION_SERVIECS_REQUESTS=1001;
    private Location mLastloaction;
    GoogleApiClient mgoogleApiClient;
    LocationRequest mlocationRequest;
    private static int UPDATE_INTERVAL=1500;
    private static int FAST_INTERVAL=5000;
    private static int DISPLAYCEMINT=10;
//IGoCoordinates mServices;
    RecyclerView recyclerView;

    String lat,lng;
    TextView empty;
    DatabaseReference orderRef;
    FirebaseRecyclerAdapter<Request, ViewHolderRequest> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        recyclerView=findViewById(R.id.recyclerview);
        empty=findViewById(R.id.empty);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        Intent intent=new Intent(getApplicationContext(), LisientServer.class);
        startService(intent);
        LoadAllOrders();
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
                                Manifest.permission.CALL_PHONE
                        }
                        , 1);

            } else {
                if (CheckPlayServiec()) {
                    buildGoogleApiClient();
                    CreateLocationRequests();
                }
            }
            displayLocation();
        }


}
    private void displayLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED&&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }
                        , 1);

            } else {

                mLastloaction=LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);
                if (mLastloaction!=null)
                {
                    lat= String.valueOf(mLastloaction.getLatitude());
                    lng = String.valueOf(mLastloaction.getLongitude());



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
        LoadAllOrders();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter!=null)
        {
            adapter.startListening();
        }
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
                        Toast.makeText(HomeActivity.this, "Waiting ", Toast.LENGTH_SHORT).show();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(HomeActivity.this, 1);
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
    public void LoadAllOrders()
    {
        orderRef= FirebaseDatabase.getInstance().getReference(Common.ORDERNEEDSHIP_TABLE).
                child(Common.CURRENTSHIPER.getPhone());
        FirebaseRecyclerOptions<Request> listorders=new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderRef,Request.class)
                .build();
        if (listorders==null)
        {
            empty.setVisibility(View.VISIBLE);
        }else {
            empty.setVisibility(View.GONE);
        }
        adapter=new FirebaseRecyclerAdapter<Request, ViewHolderRequest>(listorders) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolderRequest holder, final int position, @NonNull final Request request) {


                request.setRequest_id(orderRef.getKey());
                holder.address.setText(request.getAdress());
                holder.phone.setText(request.getPhone());
                holder.status.setText(Common.ConvertStatusts(request.getStatus()));
                holder.total.setText(request.getTotal());
                Glide.with(getApplicationContext()).load(request.getImageurl()).into(holder.imageprofile);
                holder.username.setText(request.getName());
                TextDrawable drawable = TextDrawable.builder()
                        .buildRound("" + request.getFoods().size(), Color.RED);
                holder.count.setImageDrawable(drawable);
                holder.btnshipping.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (lat.isEmpty() && lng.isEmpty()) {

                        } else{
                             Common.CreateShipperOrder(adapter.getRef(position).getKey()
                             ,Common.CURRENTSHIPER.getPhone(),
                                     mLastloaction);
                             Common.CURRENTLOCATION=mLastloaction;
                             Common.CURRENTREQUEST=request;
                             Common.CurrentKey=adapter.getRef(position).getKey();
                            startActivity(new Intent(HomeActivity.this,MapsActivity.class));
                        }
                    }
                });
                holder.comment.setText(request.getComment());
                long date=Long.parseLong(request.getRequest_id());

                holder.date.setText(Common.getDate(date));
            }

            @NonNull
            @Override
            public ViewHolderRequest onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolderRequest(LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.lauout_item_request,parent,false));
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

}
