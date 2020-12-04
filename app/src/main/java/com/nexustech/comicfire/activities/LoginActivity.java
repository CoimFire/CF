package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.utils.Utils;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class LoginActivity extends AppCompatActivity {
    private ImageView google, facebook;
    private TextView login, tvNewAccount;
    private EditText userName, password;
    private static final int RC_SIGN_IN = 5;
    private GoogleApiClient mGoogleSignInClient;
    private static final String TAG = "Login ";
    private FirebaseAuth cfAuth;
    private DatabaseReference profileRef;
    private String curUserId;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Utils.setTopBar(this,getWindow(), getResources());
        cfAuth = FirebaseAuth.getInstance();
        profileRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User");
        initializeViews();
        clickListeners();
        signinOptions();

        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }


    }


    private void signinOptions() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, "Connection to google sign in failed..", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void clickListeners() {
/*        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cfAuth.signOut();
                Toast.makeText(LoginActivity.this, ""+curUserId, Toast.LENGTH_SHORT).show();
            }
        });

 */
        login.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                allowingUsetToLogin();
            }
        });
        tvNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initializeViews() {
        //  google=findViewById(R.id.googleLogo);
        //  facebook=findViewById(R.id.facebookLogo);
        login = findViewById(R.id.tvLogin);
        userName = findViewById(R.id.etUsername);
        password = findViewById(R.id.etPassword);
        tvNewAccount = findViewById(R.id.tvNewAccount);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(this, "Please wait while getting Auth result..", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Can't get auth result", Toast.LENGTH_SHORT).show();
            }


        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        cfAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(LoginActivity.this, "Authentication Success..", Toast.LENGTH_SHORT).show();

                            validateProfile();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed..", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }

    private void validateProfile() {

        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                curUserId = cfAuth.getCurrentUser().getUid().toString();
                if (dataSnapshot.hasChild(curUserId)) {
                    Intent intent = new Intent(LoginActivity.this, BottomBarActivity.class);
                    startActivity(intent);

                } else {
                    Intent intent = new Intent(LoginActivity.this, BottomBarActivity.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void allowingUsetToLogin() {
        String mail = userName.getText().toString();
        String passwordText = password.getText().toString();

        if (TextUtils.isEmpty(mail)) {
            Toast.makeText(this, "Please enter your mail..", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(passwordText)) {
            Toast.makeText(this, "Please enter your password..", Toast.LENGTH_SHORT).show();
        } else {
            cfAuth.signInWithEmailAndPassword(mail, passwordText)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(LoginActivity.this, BottomBarActivity.class);
                                startActivity(intent);
                                validateProfile();
                                // SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error.." + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


}