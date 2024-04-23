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

public class SignUp extends AppCompatActivity {

    FirebaseAuth mAuth;
    TextView textView;
    TextInputEditText emailEt, passET, confirmPassEt;
    AppCompatButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();

        /*UI*/
        textView = findViewById(R.id.textView);
        textView.setOnClickListener(v -> {
            finish();
        });
        passET = findViewById(R.id.passET);
        emailEt = findViewById(R.id.emailEt);
        confirmPassEt = findViewById(R.id.confirmPassEt);
        button = findViewById(R.id.button);

        button.setOnClickListener(v -> {
            if (mAuth != null){
                if (!emailEt.getText().toString().equals("") && !passET.getText().toString().equals("") && !confirmPassEt.getText().toString().equals("")){

                    String email = emailEt.getText().toString();
                    String pass = passET.getText().toString();
                    String cpass = confirmPassEt.getText().toString();

                    if (pass.equals(cpass)) {
                        if (pass.length() > 6){

                            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){

                                        Intent i = new Intent(SignUp.this, SignIn.class);
                                        startActivity(i);
                                        emailEt.clearFocus();
                                        passET.clearFocus();
                                        confirmPassEt.clearFocus();
                                        finish();
                                    } else Toast.makeText(SignUp.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(SignUp.this, "Password must be longer than 6 characters.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(SignUp.this, "Ensure confirm password and password is correct.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(SignUp.this, "Fill in all details!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SignUp.this, "Please retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}