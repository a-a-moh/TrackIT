package com.example.trackit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CustomerDeliveryHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore fdb = FirebaseFirestore.getInstance();
    private String customerID = FirebaseAuth.getInstance().getUid();
    ArrayList<DeliveredPackage> packageList = new ArrayList<>();
    ListView parcelDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_delivery_history);
        parcelDetails = (ListView) findViewById(R.id.parcelDetails);

        fdb.collection("customers").document(customerID).collection("delivered").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot q : queryDocumentSnapshots){
                    DeliveredPackage d = new DeliveredPackage(q.getId(), q.get("code").toString(), q.get("timestamp").toString());
                    packageList.add(d);
                }
                setL();
            }
        });

    }
    public void setL(){
        ParcelAdapter adapter = new ParcelAdapter(this, R.layout.adapter_view_layout, packageList);
        parcelDetails.setAdapter(adapter);
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(CustomerDeliveryHistoryActivity.this, CustomerHomeActivity.class);
        startActivity(intent);
        finish();
        return;
    }
}