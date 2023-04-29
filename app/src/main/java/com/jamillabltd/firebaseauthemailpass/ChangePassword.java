package com.jamillabltd.firebaseauthemailpass;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangePassword extends AppCompatActivity {
    private EditText currentPassEditText, newPassEditText, confirmedPassEditText;
    private Button verifyButton, changePass;
    private ProgressBar progressBar1, progressBar2;
    private TextView verifyStatus;
    private String userCurrentPass;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        this.setTitle("Change Password");

        currentPassEditText = findViewById(R.id.userCurrentPassEditTextId);
        newPassEditText = findViewById(R.id.userNewPassEditTextId);
        confirmedPassEditText = findViewById(R.id.userConfirmedPassEditTextId);
        verifyStatus = findViewById(R.id.verifyStatusId);
        verifyButton = findViewById(R.id.verifyPassButtonId);
        changePass = findViewById(R.id.changePassButtonId);
        progressBar1 = findViewById(R.id.progressBar1Id);
        progressBar2 = findViewById(R.id.progressBar2Id);

        newPassEditText.setEnabled(false);
        confirmedPassEditText.setEnabled(false);
        changePass.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        assert firebaseUser != null;
        if (firebaseUser.equals("")){
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ChangePassword.this, ProfileActivity.class));
            finish();
        }else{
            verifyUser(firebaseUser);
        }


    }

    //verify user with input pass
    private void verifyUser(FirebaseUser firebaseUser) {
        verifyButton.setOnClickListener(view -> {

            userCurrentPass = currentPassEditText.getText().toString();

            if (TextUtils.isEmpty(userCurrentPass)){
                currentPassEditText.setError("Please enter your pass. to verify.");
                currentPassEditText.requestFocus();
            }else if (userCurrentPass.length() < 6) {
                currentPassEditText.setError("Entered Password is too short. Pass must be 6 char");
                currentPassEditText.requestFocus();
            }else{
                progressBar1.setVisibility(View.VISIBLE);

                //verify now
                AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(firebaseUser.getEmail()), userCurrentPass);
                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {

                    if (task.isSuccessful()){
                        progressBar1.setVisibility(View.GONE);

                        //show hide linear layout
                        LinearLayout beforeVerifyArea = findViewById(R.id.beforeVerifyAreaId);
                        beforeVerifyArea.setVisibility(View.GONE);
                        LinearLayout afterVerifyArea = findViewById(R.id.afterVerifyAreaId);
                        afterVerifyArea.setVisibility(View.VISIBLE);

                        currentPassEditText.setEnabled(false);
                        verifyButton.setEnabled(false);
                        newPassEditText.setEnabled(true);
                        confirmedPassEditText.setEnabled(true);
                        changePass.setEnabled(true);
                        verifyStatus.setText("Verified! Now you can change your password!");

                        changePass.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view1) {
                                changePassPassMethod(firebaseUser);
                            }
                        });

                    }else {
                        progressBar1.setVisibility(View.GONE);
                        try {
                            throw Objects.requireNonNull(task.getException());
                        }catch (Exception e){
                            currentPassEditText.requestFocus();
                            currentPassEditText.setError("Wrong Password!");
                            Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                });

            }

        });
    }

    //change pass button
    private void changePassPassMethod(FirebaseUser firebaseUser) {

        newPassEditText.requestFocus();

        String textNewPass = newPassEditText.getText().toString();
        String textConfirmPass = confirmedPassEditText.getText().toString();

        if (TextUtils.isEmpty(textNewPass)) {
            newPassEditText.setError("Create a new pass");
            newPassEditText.requestFocus();
        } else if (textNewPass.length() < 6) {
            newPassEditText.setError("Password too weak. Password should be at least 6 digit");
            newPassEditText.requestFocus();
        } else if (TextUtils.isEmpty(textConfirmPass)) {
            confirmedPassEditText.setError("Confirm your pass");
            confirmedPassEditText.requestFocus();
        } else if (!textNewPass.equals(textConfirmPass)) {
            confirmedPassEditText.setError("Password not matching");
            confirmedPassEditText.requestFocus();
        }else{
            progressBar2.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(textNewPass).addOnCompleteListener(task -> {
                progressBar2.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(ChangePassword.this, "Password Changed Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    progressBar2.setVisibility(View.GONE);
                    try {
                        throw Objects.requireNonNull(task.getException());
                    }catch (Exception e){
                        Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }


    }

}