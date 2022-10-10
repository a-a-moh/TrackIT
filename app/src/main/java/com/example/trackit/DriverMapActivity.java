package com.example.trackit;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
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
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener {
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiclient;
    Location mLastLocation;
    LocationRequest mLocationrequest;
    Marker cMarker;
    private List<Polyline> polylines;
    private FirebaseFirestore fdb = FirebaseFirestore.getInstance();
    TextView cName, cAddress, dis, et;
    Button vKey;
    EditText eKey;
    public ArrayList<Packages> packages = new ArrayList<>();
    public ArrayList<Customer> customers = new ArrayList<>();
    private Geocoder coder;
    private ImageView customerImageView;
    HashMap<String, String> route = new HashMap<>();
    FirebaseStorage fsb = FirebaseStorage.getInstance();
    Map<String, Object> driverL = new HashMap<>();
    String distance = "";
    String eta = "";
    Integer distanceMeters = 0;
    Map<String, Object> customerDelivery = new HashMap<>();
    Integer mark = 0;
    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        coder = new Geocoder(this);
        super.onCreate(savedInstanceState);

        com.example.trackit.databinding.ActivityDriverMapBinding binding = ActivityDriverMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        customerImageView = findViewById(R.id.imageViewCustomer);
        cName = findViewById(R.id.tvCustomerName);
        dis = findViewById(R.id.distance);
        et = findViewById(R.id.arrival);
        cAddress = findViewById(R.id.tvCustomerAddress);
        vKey = findViewById(R.id.btnSubmitCode);
        vKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelivery();
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        polylines = new ArrayList<>();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiclient();
        mMap.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleApiclient() {
        mGoogleApiclient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiclient.connect();

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverLocation");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
        GeoPoint g = new GeoPoint((location.getLatitude()), location.getLongitude());
        driverL.put("location", g);
        fdb.collection("driverLocation").document("driver").set(driverL);
        plotCustomers();

    }

    public void plotCustomers() {
        fdb.collection("pendingDeliveries").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.size() == 0){
                    if (customers.size() != 0){
                        String cs = customers.get(0).customerID;
                    }
                    Toast.makeText(getApplicationContext(), "Nore more packages to deliver", Toast.LENGTH_LONG).show();
                    mGoogleApiclient.disconnect();
                    Intent intent = new Intent(DriverMapActivity.this, DriverHomeActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                if(packages.size() != queryDocumentSnapshots.size()){
                    packages.clear();
                    if (mark == 1){
                        mMap.clear();
                    }
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        Packages p = new Packages();
                        p.packageID = q.getId();
                        p.customerID = q.get("customerID").toString();
                        p.code = q.get("code").toString();
                        packages.add(p);
                    }
                }
            }
        });
        fdb.collection("customers").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (customers.size() != packages.size()){
                    customers.clear();
                    for (QueryDocumentSnapshot customer : queryDocumentSnapshots) {
                        for (Packages p : packages) {
                            if (p.customerID.equals(customer.getId())) {
                                Customer c = new Customer();
                                c.customerID = customer.getId();
                                c.name = customer.get("firstName").toString();
                                c.number = customer.get("number").toString();
                                c.address = customer.get("address").toString();
                                if (c.address != null) {
                                    try {
                                        c.latLng = (Address) coder.getFromLocationName(c.address, 1).get(0);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (c.latLng != null) {
                                    c.loc.setLatitude(c.latLng.getLatitude());
                                    c.loc.setLongitude(c.latLng.getLongitude());
                                }
                                customers.add(c);

                            }
                        }
                    }
                }
            }
        });
        //Set customer marker on map
        for (Customer c : customers) {
            LatLng al = new LatLng(c.latLng.getLatitude(), c.latLng.getLongitude());
            cMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .position(new LatLng(al.latitude, al.longitude)));
            cMarker.showInfoWindow();
            mark = 1;
        }
        Collections.sort(customers, (a, b) -> Integer.compare(Math.round(a.loc.distanceTo(mLastLocation)), Math.round(b.loc.distanceTo(mLastLocation))));
        if (customers.size() != 0){
            route.put("customerID", customers.get(0).customerID);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("customerImage").child(customers.get(0).customerID);
            final long i = 1024 * 1024;
            storageReference.getBytes(i).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    customerImageView.setImageBitmap(bmp);
                }
            });
            cName.setText(customers.get(0).name);
            cAddress.setText(customers.get(0).address);
            StorageReference pathReference = fsb.getInstance().getReference().child("customerImage/").child(customers.get(0).customerID);

            final long ONE_MEGABYTE = 150 * 150;
            pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bit = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Drawable img = new BitmapDrawable(getResources(), bit);
                    customerImageView.setBackground(img);
                }

            });
            LatLng dest = new LatLng(customers.get(0).loc.getLatitude(), customers.get(0).loc.getLongitude());
            getRoute(dest);
        }
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


    public void getRoute(LatLng r1){
        if (r1 != null){
        LatLng driv = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(driv, r1)
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
        for (int i = 0; i <route.size(); i++) {
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            distance = route.get(i).getDistanceText();
            eta = route.get(i).getDurationText();
            distanceMeters = route.get(i).getDistanceValue();
        }
        if (customers.size() != 0){
            dis.setText(distance);
            et.setText(eta);
            customerDelivery.put("ETA", eta);
            customerDelivery.put("distance", distanceMeters.toString());
            fdb.collection("nextDelivery").document().delete();
            fdb.collection("nextDelivery").document(customers.get(0).customerID).set(customerDelivery);
        }
    }
    @Override
    public void onRoutingCancelled() {

    }

    private void confirmDelivery(){
        eKey = findViewById(R.id.enterKey);
        String key = eKey.getText().toString();
        fdb.collection("pendingDeliveries").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q : queryDocumentSnapshots){
                    if (customers.get(0).customerID.equals(q.get("customerID"))){
                        if(key.equals(q.get("code"))){
                            Calendar c = Calendar.getInstance();
                            Map<String, String> packageDetails = new HashMap<>();
                            Calendar cal = Calendar.getInstance();
                            SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String timeStamp = date.format(cal.getTime());
                            packageDetails.put("code", q.get("code").toString());
                            packageDetails.put("timestamp", timeStamp);
                            fdb.collection("pendingDeliveries").document(q.getId()).delete();
                            Toast.makeText(getApplicationContext(),"" + "Package delivered",Toast.LENGTH_SHORT).show();
                            fdb.collection("customers").document(customers.get(0).customerID).collection("delivered").document(q.getId()).set(packageDetails);
                            fdb.collection("nextDelivery").document(customers.get(0).customerID).delete();
                            packages.clear();
                            customers.remove(0);
                            mMap.clear();
                            customerImageView.setImageResource(0);
                            cName.setText("Customer Name");
                            cAddress.setText("Customer Address");
                            plotCustomers();
                        }else{
                            Toast.makeText(getApplicationContext(),"" + "Invalid code",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                    }
                }
            }
        });
    }
    @Override
    public void onBackPressed(){
        mGoogleApiclient.disconnect();
        if (customers.size() != 0){
            String cs = customers.get(0).customerID;
            fdb.collection("nextDelivery").document(cs).delete();
        }
        fdb.collection("driverLocation").document("driver").delete();
        System.out.println(customers.get(0).customerID);
        Intent intent = new Intent(DriverMapActivity.this, DriverHomeActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}

