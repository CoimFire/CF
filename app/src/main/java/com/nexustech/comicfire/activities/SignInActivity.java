package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.utils.Constants;
import com.nexustech.comicfire.utils.Utils;

import java.util.HashMap;

import static com.nexustech.comicfire.utils.Constants.DEFAULT_CHARACTER;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class SignInActivity extends AppCompatActivity {
    private TextView signin;
    private EditText userName, password, reTypePassword, et_name;
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "Login ";
    private FirebaseAuth cfAuth;
    private DatabaseReference userRef, charRef;
    private String curUserId;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Utils.setTopBar(getWindow(), getResources());

        userRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User");
        charRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Characters").child(DEFAULT_CHARACTER);

        initializeViews();
        clickListeners();

    }

    private void clickListeners() {
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();

            }
        });
    }

    private void initializeViews() {
        userName = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        reTypePassword = findViewById(R.id.etReTypePassword);
        signin = findViewById(R.id.tvCreateAccount);
        et_name = findViewById(R.id.et_name);

    }

    private void CreateNewAccount() {

        String email = userName.getText().toString();
        String pass = password.getText().toString();
        String confirmPassword = reTypePassword.getText().toString();
        String name = et_name.getText().toString();


        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your Name..", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your E mail..", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Please enter your Password..", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Confim your password..", Toast.LENGTH_SHORT).show();

        } else if (!pass.equals(confirmPassword)) {
            Toast.makeText(this, "Password not match..", Toast.LENGTH_SHORT).show();

        } else {

            cfAuth = FirebaseAuth.getInstance();

            cfAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                cfAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        saveUserInfo(cfAuth, name);
                                    }
                                });
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(SignInActivity.this, "Error Ocured" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }

    private void saveUserInfo(FirebaseAuth cfAuth, String name) {

        charRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentUser = cfAuth.getCurrentUser().getUid();
                    String date = Utils.getDate();
                    String profileImage, characterImage, profileName;
                    profileImage = dataSnapshot.child("CharacterProfile").getValue().toString();
                    characterImage = dataSnapshot.child("CharImage").getValue().toString();
                    profileName = dataSnapshot.child("CharacterName").getValue().toString();

                    HashMap hashMap = new HashMap();
                    hashMap.put("UserId", currentUser);
                    hashMap.put("CreatedDate", date);
                    hashMap.put("DisplayName", name);
                    hashMap.put("CharacterName", profileName);
                    hashMap.put("CharacterImage", characterImage);
                    hashMap.put("ProfileImage", profileImage);
                    userRef.child(currentUser).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                userRef.child(currentUser).child("Followings").child(currentUser).child("UserId").setValue(currentUser);
                                Toast.makeText(SignInActivity.this, "Open your email and verify", Toast.LENGTH_SHORT).show();
                                sendUserToHomeActivity();
                            } else {
                                Toast.makeText(SignInActivity.this, "ERROR : Creating account", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendUserToHomeActivity() {
        Intent intent = new Intent(SignInActivity.this, BottomBarActivity.class);
        startActivity(intent);

    }
}