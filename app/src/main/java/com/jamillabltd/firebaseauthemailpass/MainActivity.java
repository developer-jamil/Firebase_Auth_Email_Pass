package com.jamillabltd.firebaseauthemailpass;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText loginEmail, loginPass;
    private ProgressBar progressBar;
    private TextView forgotPass;

    //Exception
    private static final String TAG = "MainActivity";
    //keep login
    public static final String SHARED_PREFS = "sharedPrefs";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //make the activity full screen
        //hide title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //hide action bar / app bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        loginEmail = findViewById(R.id.emailEditTextId);
        loginPass = findViewById(R.id.passwordEditTextId);
        progressBar = findViewById(R.id.progressBarId);

        TextView createNewAccount = findViewById(R.id.createNewAccountId);
        createNewAccount.setOnClickListener(this);

        //login user
        Button login = findViewById(R.id.loginButtonId);
        login.setOnClickListener(this);

        forgotPass = findViewById(R.id.forgotPasswordId);
        forgotPass.setOnClickListener(this);
        forgotPass.setVisibility(View.GONE);


        //keep login
        checkBox();

        //internet connection area
        TextView connectionArea = findViewById(R.id.internetConnectionCheckAreaId);
        connectionArea.setOnClickListener(this);

        //internet connection check - if no internet
        if (!CheckNetwork.isInternetAvailable(this)) {
            //if there is no internet do this
            connectionArea.setVisibility(View.VISIBLE);
        } else { //if connected with internet
            //Web view stuff
            // Find the WebView and configure it
            connectionArea.setVisibility(View.GONE);
        }



    }


    //network check
    private static class CheckNetwork {
        static final String TAG = CheckNetwork.class.getSimpleName();

        static boolean isInternetAvailable(Context context) {
            NetworkInfo info = ((ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

            if (info == null) {
                Log.d(TAG, "no internet connection");
                return false;
            } else {
                Log.d(TAG, " internet connection available...");
                return true;
            }
        }
    }

    //check box
    private void checkBox() {
        SharedPreferences getSharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String check = getSharedPreferences.getString("isChecked", "");
        if (check.equals("true")){
            Toast.makeText(this, "Success - Keep LogIn", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }

    //onclick event
    @Override
    public void onClick(View v) {

        //login
        if (v.getId() == R.id.loginButtonId) {
            String textEmail = loginEmail.getText().toString();
            String textPass = loginPass.getText().toString();
            if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                loginEmail.setError("Enter your email...");
                loginEmail.requestFocus();
            } else if (TextUtils.isEmpty(textPass)) {
                loginPass.setError("Enter Password...");
                loginPass.requestFocus();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                loginUser(textEmail, textPass);
            }
        }

        //register
        if (v.getId() == R.id.createNewAccountId) {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        }

        //forget password
        if (v.getId() == R.id.forgotPasswordId) {
            startActivity(new Intent(MainActivity.this, ForgotPassword.class));
        }

        //no internet
        if (v.getId() == R.id.internetConnectionCheckAreaId) {
            this.recreate();
        }

    }

    //login Method
    private void loginUser(String textEmail, String textPass) {

        //keep login
        CheckBox checkBox = findViewById(R.id.checkBoxId);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {});

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(textEmail, textPass).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                //get instance of the current user
                FirebaseUser firebaseUser = auth.getCurrentUser();
                //check if email is verified before user can access their profile
                assert firebaseUser != null;
                if (firebaseUser.isEmailVerified()) {


                    //keep login
                    if (checkBox.isChecked()) {
                        // CheckBox is checked
                        // Save the state of the CheckBox
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("isChecked", "true");
                        editor.apply();

                        //Toast.makeText(MainActivity.this, "checked", Toast.LENGTH_SHORT).show();
                    }

                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                } else {
                    firebaseUser.sendEmailVerification();
                    auth.signOut(); //sign out user
                    showAlertDialog();
                }


            } else {
                progressBar.setVisibility(View.GONE);
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidUserException e) {
                    loginEmail.setError("User does not exist or is no longer valid. Please register again.");
                    loginEmail.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    loginPass.setError("Wrong Password.");
                    loginPass.requestFocus();
                    forgotPass.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //not verified email show alert dialog
    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now, You can't login without email verification");

        //open email app if user Click continue button
        builder.setPositiveButton("Continue", (dialogInterface, i) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //to email app in new window and not within our app
            startActivity(intent);
        });
        //create the alertdialog
        AlertDialog alertDialog = builder.create();
        //show the alertDialog
        alertDialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "onRestart Called", Toast.LENGTH_SHORT).show();
    }



    //backPress
    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}