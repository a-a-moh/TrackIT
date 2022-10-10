package com.example.trackit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.location.Address;
import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.trackit.databinding.ActivityCustomerTrackBinding;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.example.trackit.databinding.ActivityDriverMapBinding;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class CustomerTrackActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener {
    Marker cMarker;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiclient;
    Location mLastLocation;
    LocationRequest mLocationrequest;
    private FirebaseFirestore fdb = FirebaseFirestore.getInstance();
    private List<Polyline> polylines;
    String customerID = FirebaseAuth.getInstance().getUid();
    FirebaseStorage fsb = FirebaseStorage.getInstance();
    public LatLng latLng;
    ImageView d;
    private Geocoder coder;
    TextView dN, c;
    Button res;
    TextView e;
    Integer distance = 0;
    Integer markerCounter = 0;
    Marker mMarker;
    String address = "";
    Integer customerPackage = 0;
    String packageID = "";
    Address customerAddress = null;
    Location loc = new Location("");
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        com.example.trackit.databinding.ActivityCustomerTrackBinding binding = ActivityCustomerTrackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        coder = new Geocoder(this);
        polylines = new ArrayList<>();
        MarkerOptions m = new MarkerOptions().title("Driver Location")
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        d = findViewById(R.id.imageViewDriver);
        e = findViewById(R.id.eta);
        dN = findViewById(R.id.tvDriverName);
        fdb.collection("customers").document(customerID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                address = documentSnapshot.get("address").toString();
                try {
                    customerAddress = (Address) coder.getFromLocationName(address, 1).get(0);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                if (customerAddress != null){
                    loc.setLatitude(customerAddress.getLatitude());
                    loc.setLongitude(customerAddress.getLongitude());
                }
            }
        });
        c = findViewById(R.id.code);
        fdb.collection("pendingDeliveries").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q : queryDocumentSnapshots){
                    if(q.get("customerID").toString().equals(customerID)){
                        packageID = q.getId();
                        c.setText(q.get("code").toString());
                        customerPackage += 1;
                    }
                }
                if (customerPackage == 0){
                    mGoogleApiclient.disconnect();
                    Toast.makeText(getApplicationContext(),"You have no pending packages",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(CustomerTrackActivity.this, CustomerHomeActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        });
        // Create a reference with an initial file path and name
        StorageReference pathReference = fsb.getInstance().getReference().child("driverImage").child("driverID");

        final long ONE_MEGABYTE = 1024 * 1024 * 5;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bit = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Drawable img = new BitmapDrawable(getResources(), bit);
                d.setBackground(img);
            }

        });
        fdb.collection("drivers").document("driverID").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String driverName = documentSnapshot.get("firstName").toString() + " " + documentSnapshot.get("lastName").toString();
                dN.setText(driverName);
            }
        });

        res = findViewById(R.id.btnReschedule);
        res.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fdb.collection("pendingDeliveries").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot q : queryDocumentSnapshots){
                            if(q.get("customerID").toString().equals(customerID)){
                                Map<String, String> map = new HashMap<>();
                                map.put("customerID", q.get("customerID").toString());
                                map.put("code", q.get("code").toString());
                                fdb.collection("nextDelivery").document(customerID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        fdb.collection("nextDelivery").document(customerID).delete();
                                    }
                                });
                                fdb.collection("rescheduledDeliveries").document(q.getId()).set(map);
                                fdb.collection("pendingDeliveries").document(q.getId()).delete();
                                Toast.makeText(getApplicationContext(),"" + "Package has been rescheduled for another day",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CustomerTrackActivity.this, CustomerHomeActivity.class);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        }
                    }
                });
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiclient();
        mMap.setMyLocationEnabled(true);

    }


    protected synchronized void buildGoogleApiclient(){
        mGoogleApiclient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiclient.connect();

    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        mLastLocation = location;

        if (latLng != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
            cMarker= mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .position(latLng));
            cMarker.showInfoWindow();
            plotDriver();
        }

    }
    public void plotDriver() {

        FirebaseFirestore fdb = FirebaseFirestore.getInstance();
        fdb.collection("driverLocation").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot c : queryDocumentSnapshots) {
                    ArrayList<GeoPoint> g = new ArrayList<>();
                    g.add(c.getGeoPoint("location"));
                    for (GeoPoint ge : g) {
                        double lat = ge.getLatitude();
                        double lon = ge.getLongitude();
                        LatLng dLatLng = new LatLng(lat, lon);
                        if (markerCounter == 0){
                            mMarker = mMap.addMarker(new MarkerOptions()
                                    .title("Driver Location")
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(dLatLng));
                            mMarker.showInfoWindow();
                            markerCounter =+ 1;
                        }else{
                            mMarker.remove();
                            mMarker = mMap.addMarker(new MarkerOptions()
                                    .title("Driver Location")
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(dLatLng));
                            mMarker.showInfoWindow();
                        }
                        if (customerPackage == 1){
                            fdb.collection("customers").document(customerID).collection("delivered").document(packageID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()){
                                        Toast.makeText(getApplicationContext(),"Your package has been delivered",Toast.LENGTH_LONG).show();
                                        mGoogleApiclient.disconnect();
                                        Intent intent = new Intent(CustomerTrackActivity.this, CustomerHomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                        return;
                                    }
                                }
                            });
                        }
                        if (dLatLng != null && latLng != null){
                            getRoute(dLatLng);
                        }
                    }
                }
            }
        });
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationrequest = new LocationRequest();
        mLocationrequest.setInterval(2000);
        mLocationrequest.setFastestInterval(2000);
        mLocationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiclient, mLocationrequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull @org.jetbrains.annotations.NotNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    @Override
    protected void onStop() {
        super.onStop();

    }
    public void getRoute(LatLng r1){
        if (r1 != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(r1, latLng)
                    .key("AIzaSyBmr6powB61QRmcHE_PWhRAKz3Vz7DN8GA")
                    .build();
            routing.execute();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null){
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Something went wrong. Try again later", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }
        polylines = new ArrayList<>();
        //add route(s) to the map.
        fdb.collection("nextDelivery").document(customerID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    e.setText(documentSnapshot.getString("ETA"));
                    distance = new Integer(documentSnapshot.getString("distance"));
                    for (int i = 0; i <route.size(); i++) {

                        PolylineOptions polyOptions = new PolylineOptions();
                        polyOptions.width(10 + i * 3);
                        polyOptions.addAll(route.get(i).getPoints());
                        Polyline polyline = mMap.addPolyline(polyOptions);
                        polylines.add(polyline);
                    }
                }
            }
        });

    }
    @Override
    public void onRoutingCancelled() {

    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(CustomerTrackActivity.this, CustomerHomeActivity.class);
        startActivity(intent);
        finish();
        return;
    }
}