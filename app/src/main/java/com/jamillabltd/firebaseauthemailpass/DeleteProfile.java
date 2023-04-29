package com.jamillabltd.firebaseauthemailpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jamillabltd.firebaseauthemailpass.globalVar.RegisterFunction;

import java.util.Objects;

public class DeleteProfile extends AppCompatActivity {

    //initialize
    private EditText passwordEditText;
    private Button verifyButton, deleteAccount;
    private ProgressBar progressBar1, progressBar2;
    private TextView verifyStatus;
    private String userPassword;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    DatabaseReference databaseReference;

    //keep login logout
    public static final String SHARED_PREFS = "sharedPrefs";

    Uri imageUri;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);


        this.setTitle("Delete Your Profile");

        //firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();


        //initialize
        passwordEditText = findViewById(R.id.userPassEditTextId);
        verifyButton = findViewById(R.id.verifyPassButtonId);
        deleteAccount = findViewById(R.id.deleteAccountButtonId);
        verifyStatus = findViewById(R.id.verifyStatusId);
        progressBar1 = findViewById(R.id.progressBar1Id);
        progressBar2 = findViewById(R.id.progressBar2Id);


        //get email from firebase
        String userId = firebaseUser.getUid();

        //database databaseReference
        databaseReference = firebaseDatabase.getReference("Registered Users");
        databaseReference.child("All Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //access data
                RegisterFunction registerFunction = snapshot.getValue(RegisterFunction.class);
                if (registerFunction != null) {
                    String email = registerFunction.email;
                    TextView profileStatus = findViewById(R.id.verifyTextViewId);
                    profileStatus.setText("You are logged in with: "+email);
                }else {
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        //firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();

        deleteAccount.setEnabled(false);

        if (firebaseUser == null) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DeleteProfile.this, ProfileActivity.class));
            finish();
        } else {
            verifyUser(firebaseUser);
        }


    }

    //verify user with input pass
    @SuppressLint("SetTextI18n")
    private void verifyUser(FirebaseUser firebaseUser) {
        verifyButton.setOnClickListener(view -> {

            userPassword = passwordEditText.getText().toString();

            if (TextUtils.isEmpty(userPassword)) {
                passwordEditText.setError("Please enter your pass. to verify.");
                passwordEditText.requestFocus();
            } else if (userPassword.length() < 6) {
                passwordEditText.setError("Entered Password is too short. Pass must be 6 char");
                passwordEditText.requestFocus();
            } else {
                progressBar1.setVisibility(View.VISIBLE);

                //verify now
                AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(firebaseUser.getEmail()), userPassword);
                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        progressBar1.setVisibility(View.GONE);

                        passwordEditText.setEnabled(false);
                        verifyButton.setEnabled(false);
                        deleteAccount.setEnabled(true);
                        verifyStatus.setText("Verified! Now you can delete your account!");

                        deleteAccount.setOnClickListener(view1 -> showAlertDialog());

                        LinearLayout afterVerifyArea = findViewById(R.id.afterVerifyAreaId);
                        afterVerifyArea.setVisibility(View.VISIBLE);

                        LinearLayout beforeVerify = findViewById(R.id.beforeVerifyId);
                        beforeVerify.setVisibility(View.GONE);

                    } else {
                        progressBar1.setVisibility(View.GONE);
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (Exception e) {
                            passwordEditText.requestFocus();
                            passwordEditText.setError("Wrong Password!");
                            Toast.makeText(DeleteProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                });

            }

        });
    }


    //delete user dialog
    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Want to Delete User and Related Data?");
        builder.setMessage("Do your really want to delete your profile and related data? Deleted data can't be undo.");

        //open email app if user Click continue button
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            progressBar2.setVisibility(View.VISIBLE);

            //delete user from authentication
            deleteUser(firebaseUser);


        });

        builder.setNegativeButton("No", (dialogInterface, i) -> finish());

        //create the alertdialog
        AlertDialog alertDialog = builder.create();

        //yes button color
        alertDialog.setOnShowListener(dialogInterface -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red)));

        //show the alertDialog
        alertDialog.show();
    }

    //delete user method
    private void deleteUser(FirebaseUser firebaseUser) {
        firebaseUser.delete().addOnCompleteListener(task -> {
            progressBar2.setVisibility(View.GONE);
            if (task.isSuccessful()) {

                Toast.makeText(DeleteProfile.this, "Your profile is deleted!", Toast.LENGTH_SHORT).show();

                // Clear SharedPreferences - keep login
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("isChecked", "");
                editor.apply();

                // Close all previous activities and start MainActivity
                Intent intent = new Intent(DeleteProfile.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

                //delete data - image and realtime database
                deleteUserData();


            } else {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (Exception e) {
                    Toast.makeText(DeleteProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    //delete user data - image and realtime data
    private void deleteUserData() {

        //realtime database
        firebaseDatabase.getReference("Registered Users")
                .child("All Users")
                .child(firebaseUser.getUid())
                .removeValue();

        //remove storage
        deleteSelectedImage();

    }

    // Delete image from Firebase Storage
    private void deleteSelectedImage() {
        //Firebase Storage
        final StorageReference reference = firebaseStorage.getReference("Registered Users Image").child("All Users").child(firebaseUser.getUid());
        reference.delete().addOnSuccessListener(aVoid -> {
            //RealtimeDatabase
            firebaseDatabase.getReference("Registered Users")
                    .child("All Users")
                    .child(firebaseUser.getUid())
                    .child("profile_pic_link")
                    .removeValue().addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Successfully Deleted!", Toast.LENGTH_SHORT).show();
                        recreate();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to delete profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


}

