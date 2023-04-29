package com.jamillabltd.firebaseauthemailpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jamillabltd.firebaseauthemailpass.globalVar.RegisterFunction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText fullNameEditText, emailEditText, dobEditText, mobileEditText;
    private ProgressBar progressBar;
    private String selectedGender;
    private Calendar selectedDate;
    private RadioGroup radioGroup;

    //retrieve data
    private String profile_pic_link;

    //firebase
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        fullNameEditText = findViewById(R.id.fullNameEditTextId);
        emailEditText = findViewById(R.id.emailEditTextId);
        emailEditText.setEnabled(false);
        emailEditText.setFocusable(false);
        emailEditText.setFocusableInTouchMode(false);

        dobEditText = findViewById(R.id.birthDateEditTextId);
        mobileEditText = findViewById(R.id.mobileEditTextId);
        progressBar = findViewById(R.id.progressBarId);

        Button updateInfo = findViewById(R.id.updateButtonId);
        updateInfo.setOnClickListener(this);

        //firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        //radio group - select gender
        radioGroup = findViewById(R.id.radioGroupId);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = radioGroup.findViewById(checkedId);
            selectedGender = radioButton.getText().toString();
        });


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
                        String email = registerFunction.email;
                        String mobile = registerFunction.mobile;
                        String dob = registerFunction.dob;
                        String gender = registerFunction.gender;
                        profile_pic_link = registerFunction.profile_pic_link;

                        fullNameEditText.setText(fullName);
                        emailEditText.setText(email);
                        dobEditText.setText(dob);
                        mobileEditText.setText(mobile);


                        // Set radio button based on gender value
                        radioGroup = findViewById(R.id.radioGroupId);
                        if (gender.equals("Male")) {
                            radioGroup.check(R.id.maleId);
                        } else if (gender.equals("Female")) {
                            radioGroup.check(R.id.femaleId);
                        }

                    } else {
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

        //selected date
        selectedDate = Calendar.getInstance();
        dobEditText.setOnClickListener(this);
        updateBirthDateEditText();

    }


    //onClick event
    @Override
    public void onClick(View v) {

        //dob
        if (v.getId() == R.id.birthDateEditTextId) {
            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog = new DatePickerDialog(EditProfileActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        // update selected date
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, monthOfYear);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        // update birth date EditText
                        updateBirthDateEditText();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            // show date picker dialog
            datePickerDialog.show();
        }

        //update button
        if (v.getId() == R.id.updateButtonId) {
            // get the selected gender value from the radio group
            radioGroup = findViewById(R.id.radioGroupId);
            int selectedGenderId = radioGroup.getCheckedRadioButtonId();
            if (selectedGenderId == -1) {
                // no RadioButton is selected, display an error message
                Toast.makeText(getApplicationContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
            selectedGender = selectedGenderRadioButton.getText().toString();

            //get all entered data
            String getFullName = fullNameEditText.getText().toString().trim();
            String getEmail = emailEditText.getText().toString().trim();
            String getDob = dobEditText.getText().toString().trim();
            String getMobile = mobileEditText.getText().toString().trim();

            //validate mobile number using matcher and pattern
            String mobileRegex = "01[0-9]{9}";
            //First two digits must be 01 and rest 9 digits can be any digit.
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            Matcher mobileMatcher = mobilePattern.matcher(getMobile);

            // perform registration logic here
            if (TextUtils.isEmpty(getFullName)) {
                fullNameEditText.setError("Enter your full name!");
                fullNameEditText.requestFocus();
            } else if (TextUtils.isEmpty(getEmail)) {
                emailEditText.setError("Enter your email");
                emailEditText.setEnabled(true);
                emailEditText.setFocusable(true);
                emailEditText.setFocusableInTouchMode(true);
                emailEditText.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(getEmail).matches()) {
                emailEditText.setError("Enter a valid email");
                emailEditText.requestFocus();
            } else if (TextUtils.isEmpty(getDob)) {
                dobEditText.setError("Date of Birth is required");
                dobEditText.requestFocus();
            } else if (TextUtils.isEmpty(getMobile)) {
                mobileEditText.setError("Enter your mobile");
                mobileEditText.requestFocus();
            } else if (getMobile.length() != 11) {
                mobileEditText.setError("Enter a valid mobile");
                mobileEditText.requestFocus();
            } else if (!mobileMatcher.find()) {
                mobileEditText.setError("Enter a valid mobile");
                mobileEditText.requestFocus();
            } else {
                update(getFullName, getEmail, getDob, selectedGender, getMobile);
            }


        }


    }

    //chose dob from user - save previous chosen dob
    private void updateBirthDateEditText() {
        // format selected date as "dd/MM/yyyy"
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String formattedDate = sdf.format(selectedDate.getTime());
        // update birth date EditText
        dobEditText.setText(formattedDate);
    }


    //update method
    private void update(String getFullName, String getEmail, String getDob, String selectedGender, String getMobile) {
        progressBar.setVisibility(View.VISIBLE);

        RegisterFunction registerFunction = new RegisterFunction(getFullName, getEmail, getDob, selectedGender, getMobile, profile_pic_link);

        //Extracting user reference from database for "Registered User"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child("All Users").child(firebaseUser.getUid()).setValue(registerFunction).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                //setting new display name
                Toast.makeText(EditProfileActivity.this, "Successfully Updated!", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });


    }


}