package com.example.vegetable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {

    FirebaseAuth mAuth;
    TextView textView;
    TextInputEditText emailEt, passET;
    AppCompatButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        /*Firebase*/
        mAuth = FirebaseAuth.getInstance();

        /*UI*/
        textView = findViewById(R.id.textView);
        textView.setOnClickListener(v ->{
            Intent i = new Intent(this, SignUp.class);
            startActivity(i);
        });
        passET = findViewById(R.id.passET);
        emailEt = findViewById(R.id.emailEt);
        button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (mAuth != null){
                if (!emailEt.getText().toString().equals("") && !passET.getText().toString().equals("")){
                    String email = emailEt.getText().toString();
                    String pass = passET.getText().toString();

                    mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Intent i = new Intent(SignIn.this, MainActivity.class);
                                startActivity(i);
                                emailEt.clearFocus();
                                passET.clearFocus();
                            } else Toast.makeText(SignIn.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(SignIn.this, "Fill in all details!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SignIn.this, "Please retry.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent i = new Intent(SignIn.this, MainActivity.class);
            startActivity(i);
        }
    }
}