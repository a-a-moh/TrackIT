package com.example.trackit;

import android.location.Address;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    String name;
    String number;
    String address;
    String customerID;
    Address latLng;
    Location loc = new Location("");
}
