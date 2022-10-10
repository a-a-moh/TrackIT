package com.example.trackit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Array;
import java.sql.Driver;
import java.util.ArrayList;

public class DriverRouteActivity extends AppCompatActivity {

    private FirebaseFirestore fdb = FirebaseFirestore.getInstance();
    ArrayList<DriverRoute> routeList = new ArrayList<>();
    ListView routeDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_route);
        routeDetails = (ListView) findViewById(R.id.routeList);

        fdb.collection("pendingDeliveries").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q : queryDocumentSnapshots){
                    String s = q.get("customerID").toString();
                    fdb.collection("customers").document(s).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String cID = documentSnapshot.getId();
                            String cN = documentSnapshot.get("firstName").toString() + " " + documentSnapshot.get("lastName").toString();
                            String cD = documentSnapshot.get("address").toString();
                            DriverRoute r = new DriverRoute(cID, cN, cD);
                            routeList.add(r);
                            setL();
                        }
                    });
                }
            }
        });
    }
    public void setL(){
        RouteAdapter adapter = new RouteAdapter(this, R.layout.route_adapter_view_layout, routeList);
        routeDetails.setAdapter(adapter);
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(DriverRouteActivity.this, DriverHomeActivity.class);
        startActivity(intent);
        finish();
        return;
    }
}