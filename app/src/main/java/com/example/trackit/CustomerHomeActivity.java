package com.example.trackit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Random;

public class CustomerHomeActivity extends AppCompatActivity  {
    private ImageButton cMap, cDetails, cHistory, cLogout;
    Random random = new Random();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseFirestore fdb = FirebaseFirestore.getInstance();
    GoogleApiClient mGoogleApiclient;
    Location mLastLocation;
    LocationRequest mLocationrequest;
    String customerID = FirebaseAuth.getInstance().getUid();
    Integer n = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        mAuth = FirebaseAuth.getInstance();

        String UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cLogout = (ImageButton) findViewById(R.id.cBtnLogout);
        cLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(CustomerHomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        cMap = (ImageButton) findViewById(R.id.cBtnMap);
        cMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fdb.collection("driverLocation").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() != 0){
                            Intent intent = new Intent(CustomerHomeActivity.this, CustomerTrackActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Driver is not currently making deliveries",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        cDetails = (ImageButton) findViewById(R.id.cBtnEditDetails);
        cDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerHomeActivity.this, CustomerDetailsActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        cHistory = (ImageButton) findViewById(R.id.cBtnHistory);
        cHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerHomeActivity.this, CustomerDeliveryHistoryActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

}