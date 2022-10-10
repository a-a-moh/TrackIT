package com.example.trackit;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginActivity extends AppCompatActivity {
    private EditText dEmail, dPassword;
    private Button dLogin, dRegister;

    private FirebaseAuth dAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_driver_login);

        dAuth = FirebaseAuth.getInstance();

        dLogin = (Button) findViewById(R.id.btnDriverLogin);
        dRegister = (Button) findViewById(R.id.btnDriverRegister);

        dEmail = (EditText) findViewById(R.id.dEmail);
        dPassword = (EditText) findViewById(R.id.dPassword);

        dRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = dEmail.getText().toString();
                final String password = dPassword.getText().toString();
                dAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(DriverLoginActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                        }else{
                            String UserID = dAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(UserID);
                            current_user_db.setValue(true);

                            Intent intent = new Intent(DriverLoginActivity.this, DriverRegisterDetails.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                });
            }
        });

        dLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = dEmail.getText().toString();
                final String password = dPassword.getText().toString();
                dAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(DriverLoginActivity.this, "sign in error", Toast.LENGTH_SHORT).show();
                        }else{
                            Intent intent = new Intent(DriverLoginActivity.this, DriverHomeActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                });

            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(DriverLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        return;
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

}
