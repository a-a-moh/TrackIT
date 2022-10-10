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
import android.view.Window;
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

public class DriverRegisterDetails extends AppCompatActivity {

    private EditText fName, lName, dNumber;
    private ImageView driverImage;
    private Uri resultImage;
    private Button saveDetails;
    private FirebaseFirestore fdb = FirebaseFirestore.getInstance();
    HashMap<String, String> driver = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_driver_register_details);

        fName = (EditText) findViewById(R.id.registerFName);
        lName = (EditText) findViewById(R.id.registerLName);
        dNumber = (EditText) findViewById(R.id.registerNumber);
        dNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        driverImage = (ImageView) findViewById(R.id.driverimage);
        driverImage.setOnClickListener(new View.OnClickListener() {
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

                saveDriver();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultImage = imageUri;
            driverImage.setImageURI(resultImage);
        }
    }

    private void saveDriver() {

        if (resultImage != null) {

            StorageReference imageReference = FirebaseStorage.getInstance().getReference().child("driverImage").child("driverID");
            Bitmap bMap = null;
            try {
                bMap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream compressImage = new ByteArrayOutputStream();
            bMap.compress(Bitmap.CompressFormat.JPEG, 20, compressImage);
            byte[] images = compressImage.toByteArray();
            imageReference.putBytes(images);

            String firstName = fName.getText().toString();
            String lastName = lName.getText().toString();
            String number = dNumber.getText().toString();

            if (firstName.isEmpty() || lastName.isEmpty() || number.isEmpty()){
                Toast.makeText(this, "Please enter all the details requested", Toast.LENGTH_LONG);
            }else{
                driver.put("firstName", firstName);
                driver.put("lastName", lastName);
                driver.put("number", number);


                fdb.collection("drivers").document("driverID").set(driver);

                Intent intent = new Intent(DriverRegisterDetails.this, DriverHomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }


        }else{
            Toast.makeText(this, "Please enter an image", Toast.LENGTH_LONG);
        }
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(DriverRegisterDetails.this, MainActivity.class);
        startActivity(intent);
        finish();
        return;
    }
}
