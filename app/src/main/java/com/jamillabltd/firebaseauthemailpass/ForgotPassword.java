package com.jamillabltd.firebaseauthemailpass;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class ForgotPassword extends AppCompatActivity {
    private EditText emailEditText;
    private ProgressBar progressBar;

    private TextView checkEmailText;
    private Button loginAgain, checkMail;
    private LinearLayout forgotButtonArea;

    //Exception
    private static final String TAG = "ForgotPassword";

    @SuppressLint({"MissingInflatedId", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        this.setTitle("Forgot Password");

        Button resetButton = findViewById(R.id.resetButtonId);
        emailEditText = findViewById(R.id.emailEditTextId);
        progressBar = findViewById(R.id.progressBarId);


        resetButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Enter your email");
                emailEditText.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Enter a valid email");
                emailEditText.requestFocus();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                resetPassword(email);
            }
        });

        //if send reset link
        checkEmailText = findViewById(R.id.checkEmailTextId);
        loginAgain = findViewById(R.id.loginAgainId);
        checkMail = findViewById(R.id.checkMailId);
        forgotButtonArea = findViewById(R.id.forgotButtonAreaId);

        //check mail
        checkMail.setOnClickListener(v -> {
            //to email app in new window and not within our app
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        //login again
        loginAgain.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });


    }

    //reset password
    private void resetPassword(String email) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //sent reset pass link in email
                progressBar.setVisibility(View.GONE);
                checkEmailText.setVisibility(View.VISIBLE);
                forgotButtonArea.setVisibility(View.VISIBLE);

            } else {
                progressBar.setVisibility(View.GONE);
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidUserException e) {
                    emailEditText.setError("User does not exists or is no longer valid. lease register again.");
                    emailEditText.requestFocus();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(ForgotPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}