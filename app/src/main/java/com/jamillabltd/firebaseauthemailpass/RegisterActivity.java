package com.jamillabltd.firebaseauthemailpass;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jamillabltd.firebaseauthemailpass.globalVar.RegisterFunction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText fullName, email, dob, mobile, newPass, confirmPass;
    private ProgressBar progressBar;
    private String selectedGender;
    private Calendar selectedDate;
    private static final String TAG = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // full screen - title bar and action/app bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();

        //initialize all
        fullName = findViewById(R.id.fullNameEditTextId);
        email = findViewById(R.id.emailEditTextId);
        dob = findViewById(R.id.birthDateEditTextId);
        mobile = findViewById(R.id.mobileEditTextId);
        newPass = findViewById(R.id.newPassEditTextId);
        confirmPass = findViewById(R.id.confirmPassEditTextId);

        progressBar = findViewById(R.id.progressBarId);

        //radio group - select gender
        RadioGroup radioGroup = findViewById(R.id.radioGroupId);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = radioGroup.findViewById(checkedId);
            selectedGender = radioButton.getText().toString();
        });

        Button register = findViewById(R.id.registerButtonId);
        register.setOnClickListener(this);

        //selected date
        selectedDate = Calendar.getInstance();
        dob.setOnClickListener(this);
        updateBirthDateEditText();

    }


    //onClick event
    @Override
    public void onClick(View v) {

        //date of birth
        if (v.getId() == R.id.birthDateEditTextId) {
            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this,
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

        //register user
        if (v.getId() == R.id.registerButtonId) {

            // get the selected gender value from the radio group
            RadioGroup radioGroup = findViewById(R.id.radioGroupId);
            int selectedGenderId = radioGroup.getCheckedRadioButtonId();
            if (selectedGenderId == -1) {
                // no RadioButton is selected, display an error message
                Toast.makeText(getApplicationContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
            selectedGender = selectedGenderRadioButton.getText().toString();


            //get all entered data
            String getFullName = fullName.getText().toString().trim();
            String getEmail = email.getText().toString().trim();
            String getDob = dob.getText().toString().trim();
            String getMobile = mobile.getText().toString().trim();
            String getNewPass = newPass.getText().toString().trim();
            String getConfirmedPass = confirmPass.getText().toString().trim();

            //validate mobile number using matcher and pattern
            String mobileRegex = "01[0-9]{9}";
            //First two digits must be 01 and rest 9 digits can be any digit.
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            Matcher mobileMatcher = mobilePattern.matcher(getMobile);

            // perform registration logic here
            if (TextUtils.isEmpty(getFullName)) {
                fullName.setError("Enter your full name!");
                fullName.requestFocus();
            } else if (TextUtils.isEmpty(getEmail)) {
                email.setError("Enter your email");
                email.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(getEmail).matches()) {
                email.setError("Enter a valid email");
                email.requestFocus();
            } else if (TextUtils.isEmpty(getDob)) {
                dob.setError("Date of Birth is required");
                dob.requestFocus();
            } else if (TextUtils.isEmpty(getMobile)) {
                mobile.setError("Enter your mobile");
                mobile.requestFocus();
            } else if (getMobile.length() != 11) {
                mobile.setError("Enter a valid mobile");
                mobile.requestFocus();
            } else if (!mobileMatcher.find()) {
                mobile.setError("Enter a valid mobile");
                mobile.requestFocus();
            } else if (TextUtils.isEmpty(getNewPass)) {
                newPass.setError("Create a new pass");
                newPass.requestFocus();
            } else if (newPass.length() < 6) {
                newPass.setError("Password too weak. Password should be at least 6 digits");
                newPass.requestFocus();
            } else if (TextUtils.isEmpty(getConfirmedPass)) {
                confirmPass.setError("Confirm your pass");
                confirmPass.requestFocus();
            } else if (!getNewPass.equals(getConfirmedPass)) {
                confirmPass.setError("Password not matching");
                confirmPass.requestFocus();
            } else {
                register(getFullName, getEmail, getDob, selectedGender, getMobile, getNewPass);
            }
        }


    }



    //chose dob from user - save previous chosen dob
    private void updateBirthDateEditText() {
        // format selected date as "dd/MM/yyyy"
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String formattedDate = sdf.format(selectedDate.getTime());
        // update birth date EditText
        dob.setText(formattedDate);
    }

    //register method
    private void register(String getFullName, String getEmail, String getDob, String selectedGender, String getMobile, String getNewPass) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(getEmail, getNewPass).addOnCompleteListener(RegisterActivity.this, task -> {
            //if sign up successfully done
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                //access user data
                //Enter user data into the firebase realtime database
                String profile_pic_link = "";
                RegisterFunction registerFunction = new RegisterFunction(getFullName, getEmail, getDob, selectedGender, getMobile, profile_pic_link);

                //Extracting user reference from database for "Registered User"
                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                assert firebaseUser != null;
                referenceProfile.child("All Users").child(firebaseUser.getUid()).setValue(registerFunction).addOnCompleteListener(task1 -> {
                    //send verification email
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(RegisterActivity.this, "Registered Successfully.", Toast.LENGTH_SHORT).show();
                    //open user profile after successfully registered
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });

            } else {
                progressBar.setVisibility(View.GONE);
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthWeakPasswordException e) {
                    newPass.setError("Your password is too week. kindly use a mix of alphabets, number and text");
                    newPass.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    email.setError("Your email is invalid. or already is use, kindly re-enter");
                    email.requestFocus();
                } catch (FirebaseAuthUserCollisionException e) {
                    email.setError("User is already registered with this email. Use another email");
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}