package com.example.trackit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DriverHomeActivity extends AppCompatActivity {
    private ImageButton dMap, dRoute, dLogout;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    FirebaseFirestore fdb = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_driver_home);

        mAuth = FirebaseAuth.getInstance();

        dLogout = (ImageButton) findViewById(R.id.cBtnEditDetails);
        dLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fdb.collection("driverLocation").document("driver").delete();
                mAuth.signOut();
                Intent intent = new Intent(DriverHomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        dMap = (ImageButton) findViewById(R.id.cBtnMap);
        dMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverHomeActivity.this, DriverMapActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        dRoute = (ImageButton) findViewById(R.id.cBtnHistory);
        dRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (DriverHomeActivity.this, DriverRouteActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}