package com.jamillabltd.firebaseauthemailpass;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class UpdateEmail extends AppCompatActivity {
    private EditText editTextOldEmail, editTextPwd, editTextNewEmail;
    private TextView verifyStatus;
    private Button verifyButton, updateButton;
    private ProgressBar progressBar1, progressBar2;
    private String userOldEmail, userNewEmail, userPass;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);


        this.setTitle("Update Your Email");

        //firebase
        firebaseDatabase = FirebaseDatabase.getInstance();

        editTextOldEmail = findViewById(R.id.userEmailEditTextId);
        editTextPwd = findViewById(R.id.userPasswordEditTextId);
        editTextNewEmail = findViewById(R.id.userNewEmailEditTextId);
        TextView profileStatus = findViewById(R.id.verifyTextViewId);
        verifyStatus = findViewById(R.id.verifyStatusId);
        verifyButton = findViewById(R.id.verifyEmailButtonId);
        updateButton = findViewById(R.id.updateEmailButtonId);
        progressBar1 = findViewById(R.id.progressBar1Id);
        progressBar2 = findViewById(R.id.progressBar2Id);

        editTextNewEmail.setEnabled(false);
        updateButton.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        //set old email id on textView
        assert firebaseUser != null;
        userOldEmail = firebaseUser.getEmail();
        profileStatus.setText("You're logged in with: " + userOldEmail);

        if (firebaseUser == null) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        } else {
            reAuthenticate(firebaseUser);
        }


    }

    //Re-Authenticate verify user before updating email
    @SuppressLint("SetTextI18n")
    private void reAuthenticate(FirebaseUser firebaseUser) {
        verifyButton.setOnClickListener(view -> {

            //obtain password for authentication
            userPass = editTextPwd.getText().toString();

            if (TextUtils.isEmpty(userPass)){
                editTextPwd.setError("Password Required!");
                editTextPwd.requestFocus();
            }else{
                progressBar1.setVisibility(View.VISIBLE);

                AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPass);
                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {

                    if (task.isSuccessful()){
                        progressBar1.setVisibility(View.GONE);

                        //show hide linear layout
                        LinearLayout beforeVerifyArea = findViewById(R.id.beforeVerifyAreaId);
                        beforeVerifyArea.setVisibility(View.GONE);
                        LinearLayout afterVerifyArea = findViewById(R.id.afterVerifyAreaId);
                        afterVerifyArea.setVisibility(View.VISIBLE);

                        Toast.makeText(UpdateEmail.this, "Password has been verified."+"Now you can update your email.", Toast.LENGTH_SHORT).show();
                        verifyStatus.setText("Verified! Now you can change your email!");
                        editTextNewEmail.requestFocus();
                        editTextNewEmail.setEnabled(true);
                        updateButton.setEnabled(true);
                        verifyButton.setEnabled(false);
                        editTextOldEmail.setEnabled(false);
                        editTextPwd.setEnabled(false);

                        updateButton.setOnClickListener(view1 -> {
                            userNewEmail = editTextNewEmail.getText().toString();
                            if (TextUtils.isEmpty(userNewEmail)) {
                                editTextNewEmail.setError("Enter your email");
                                editTextNewEmail.requestFocus();
                            } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()) {
                                editTextNewEmail.setError("Enter a valid email");
                                editTextNewEmail.requestFocus();
                            }else if (userOldEmail.matches(userNewEmail)){
                                editTextNewEmail.setError("Please enter a new Email");
                                editTextNewEmail.requestFocus();
                            }else{
                                progressBar2.setVisibility(View.VISIBLE);
                                updateEmail(firebaseUser, userNewEmail);
                            }

                        });


                    }else{
                        progressBar1.setVisibility(View.GONE);
                        try {
                            throw Objects.requireNonNull(task.getException());
                        }catch (Exception e){
                            Toast.makeText(UpdateEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }




                });

            }

        });

    }

    private void updateEmail(FirebaseUser firebaseUser, String userNewEmail) {
        progressBar2.setVisibility(View.VISIBLE);

        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(task -> {
            progressBar2.setVisibility(View.GONE);
            if (task.isComplete()){
                firebaseUser.sendEmailVerification();
                Toast.makeText(UpdateEmail.this, "Email have been updated. Verify the new Email", Toast.LENGTH_SHORT).show();

                finish();

                //change email from realtime database
                databaseReference = firebaseDatabase.getReference("Registered Users");
                firebaseDatabase.getReference("Registered Users")
                        .child("All Users")
                        .child(firebaseUser.getUid())
                        .child("email")
                        .setValue(userNewEmail);



            }else{
                progressBar2.setVisibility(View.GONE);
                try {
                    throw Objects.requireNonNull(task.getException());
                }catch (Exception e){
                    Toast.makeText(UpdateEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }


        });
    }


}