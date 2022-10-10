package com.example.trackit;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginActivity extends AppCompatActivity {
    private EditText cEmail, cPassword;
    private Button cLogin, cRegister;

    private FirebaseAuth cAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        cAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if  (user != null){
                    Intent intent = new Intent(CustomerLoginActivity.this, CustomerHomeActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        cLogin = (Button) findViewById(R.id.cBtnLogin);
        cRegister = (Button) findViewById(R.id.cBtnRegister);

        cEmail = (EditText) findViewById(R.id.cEmail);
        cPassword = (EditText) findViewById(R.id.cPassword);

        cRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = cEmail.getText().toString();
                final String password = cPassword.getText().toString();
                cAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(CustomerLoginActivity.this, "Accouint creation error ", Toast.LENGTH_SHORT).show();
                        }else{
                            String UserID = cAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(UserID);
                            current_user_db.setValue(true);

                            Intent intent = new Intent(CustomerLoginActivity.this, CustomerRegisterDetails.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                });
            }
        });

        cLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = cEmail.getText().toString();
                final String password = cPassword.getText().toString();
                cAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(CustomerLoginActivity.this, "Sign in error", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Intent intent = new Intent(CustomerLoginActivity.this, CustomerHomeActivity.class);
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
    protected void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        //mAuth.removeAuthStateListener(firebaseAuthListener);
    }

}
