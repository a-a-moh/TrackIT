package com.example.trackit;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.jetbrains.annotations.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomerRegisterDetails extends AppCompatActivity {

    private EditText fName, lName, cNumber, cAddress;
    private ImageView customerImage;
    private Uri resultImage;
    private Button saveDetails;
    private FirebaseFirestore fdb = FirebaseFirestore.getInstance();
    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    HashMap<String, String> customer = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register_details);

        fName = (EditText) findViewById(R.id.registerFName);
        lName = (EditText) findViewById(R.id.registerLName);
        cNumber = (EditText) findViewById(R.id.registerNumber);
        cAddress = (EditText) findViewById(R.id.registerAddress);
        cNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        customerImage = (ImageView) findViewById(R.id.customerImage);
        customerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);

            }
        });

        saveDetails = (Button) findViewById(R.id.saveDetails);
        saveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveCustomer();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultImage = imageUri;
            customerImage.setImageURI(resultImage);
        }
    }

    private void saveCustomer(){

        if(resultImage != null){

            StorageReference imageReference = FirebaseStorage.getInstance().getReference().child("customerImage").child(userID);
            Bitmap bMap = null;
            try {
                bMap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultImage);
            }catch (IOException e){
                e.printStackTrace();
            }
            ByteArrayOutputStream compressImage = new ByteArrayOutputStream();
            bMap.compress(Bitmap.CompressFormat.JPEG, 20, compressImage);
            byte[] images = compressImage.toByteArray();
            imageReference.putBytes(images);


            String firstName = fName.getText().toString();
            String lastName = lName.getText().toString();
            String number = cNumber.getText().toString();
            String address = cAddress.getText().toString();
            if (firstName.isEmpty() || lastName.isEmpty() || number.isEmpty()){
                Toast.makeText(this, "Please enter an image", Toast.LENGTH_LONG);
            }else{
                customer.put("firstName", firstName);
                customer.put("lastName", lastName);
                customer.put("number", number);
                customer.put("address", address);
                fdb.collection("customers").document(userID).set(customer);

                Intent intent = new Intent(CustomerRegisterDetails.this, CustomerHomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }else{
            Toast.makeText(this, "Please enter an image", Toast.LENGTH_LONG);
        }
    }
}