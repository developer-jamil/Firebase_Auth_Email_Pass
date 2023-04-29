package com.jamillabltd.firebaseauthemailpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jamillabltd.firebaseauthemailpass.globalVar.RegisterFunction;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    //keep login - logout
    public static final String SHARED_PREFS = "sharedPrefs";

    //firebase
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        //initialize all textview
        TextView userName = findViewById(R.id.userNameId);
        TextView userProfileEmail = findViewById(R.id.emailProfileId);

        TextView userFullName = findViewById(R.id.fullNameId);
        TextView userMobile = findViewById(R.id.mobileId);
        TextView userEmail = findViewById(R.id.emailId);
        TextView userDob = findViewById(R.id.dobId);
        TextView userGender = findViewById(R.id.genderId);
        ProgressBar progressBar = findViewById(R.id.progressBarId);

        //edit profile image
        Button editProfileImage = findViewById(R.id.editProfileImageId);
        editProfileImage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), EditProfileImage.class)));

        //edit profile image
        Button editProfileInfo = findViewById(R.id.editProfileInfoId);
        editProfileInfo.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), EditProfileActivity.class)));

        //firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();


        //get user details
        try {
            //user id
            assert firebaseUser != null;
            String userId = firebaseUser.getUid();

            //database databaseReference
            databaseReference = firebaseDatabase.getReference("Registered Users");
            databaseReference.child("All Users").child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    //access data
                    RegisterFunction registerFunction = snapshot.getValue(RegisterFunction.class);
                    if (registerFunction != null) {
                        String fullName = registerFunction.fullName;
                        String mobile = registerFunction.mobile;
                        String email = registerFunction.email;
                        String dob = registerFunction.dob;
                        String gender = registerFunction.gender;

                        userName.setText(fullName);
                        userProfileEmail.setText(email);

                        userFullName.setText(fullName);
                        userMobile.setText(mobile);
                        userEmail.setText(email);
                        userDob.setText(dob);
                        userGender.setText(gender);


                    }else {
                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        } catch (Exception e) {
            Log.d("error:", "Name Locate");
        }


        //retrieve profile picture from firebase
        CircleImageView circleImageView = findViewById(R.id.profileImageViewId);

        firebaseDatabase.getReference("Registered Users")
                .child("All Users")
                .child(firebaseUser.getUid())
                .child("profile_pic_link").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String image = snapshot.getValue(String.class);
                        if (image == null || image.isEmpty()) {
                            // If CircleImageView is empty or has no image, hide the ProgressBar
                            progressBar.setVisibility(View.GONE);
                        } else {
                            // If CircleImageView has an image, show the ProgressBar while loading
                            progressBar.setVisibility(View.VISIBLE);
                            Picasso.with(ProfileActivity.this)
                                    .load(image)
                                    .placeholder(R.drawable.image_placeholder)
                                    .into(circleImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            // Hide the ProgressBar once the image is loaded
                                            progressBar.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onError() {
                                            // If there is an error loading the image, hide the ProgressBar
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle database error
                    }
                });


        //if user's email not verified
        if (firebaseUser.isEmailVerified()) {
            Toast.makeText(this, "Email Verified Account", Toast.LENGTH_SHORT).show();
        }else{
            showAlertDialog();
        }

    }


    //not verified email show alert dialog
    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now, You can't login without email verification");

        //open email app if user Click continue button
        builder.setPositiveButton("Continue", (dialogInterface, i) -> {
            firebaseUser.sendEmailVerification();

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //to email app in new window and not within our app
            startActivity(intent);
        });
        builder.setNegativeButton("LogIn Again", (dialog, which) -> {
            // Close all previous activities and start MainActivity
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        //create the alertdialog
        AlertDialog alertDialog = builder.create();
        //show the alertDialog
        alertDialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
        Toast.makeText(this, "onRestart Called", Toast.LENGTH_SHORT).show();
    }


    //option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu items
        getMenuInflater().inflate(R.menu.option_menu_layout, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //when any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        //update profile
        if (id == R.id.menuUpdateProfileId) {
            startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
        }

        //update email
        if (id == R.id.menuUpdateEmailId) {
            startActivity(new Intent(getApplicationContext(), UpdateEmail.class));
        }

        //change password
        if (id == R.id.menuChangePasswordId) {
            startActivity(new Intent(getApplicationContext(), ChangePassword.class));
        }

        //Delete Profile
        if (id == R.id.menuDeleteProfileId) {
            startActivity(new Intent(getApplicationContext(), DeleteProfile.class));
        }

        //log out
        if (id == R.id.menuLogOutId) {
            // Clear SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("isChecked", "");
            editor.apply();

            // Close all previous activities and start MainActivity
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }
}