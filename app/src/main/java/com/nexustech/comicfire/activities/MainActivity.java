package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.utils.Utils;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class MainActivity extends AppCompatActivity {
    private RecyclerView postList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar cfToolbar;
    private FirebaseAuth cfAuth;
    private DatabaseReference cfUserRef;
    private String userId;
    Button btnLogout;
    String curUserId;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.setTopBar(getWindow(), getResources());

        cfAuth = FirebaseAuth.getInstance();
        cfUserRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE);


        if (cfAuth.getCurrentUser() != null) {
            curUserId = cfAuth.getCurrentUser().getUid().toString();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                // Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

                requestPermissions(permissions, 1);

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (curUserId == null) {
                    SendUserToLoginActivity();
                } else {
                    SendUserToHomeActivity();
                    // CheckUserExistence();

                }
            }
        }, 2000);

    }

    private void SendUserToHomeActivity() {
        String currentUser=Utils.getCurrentUser();
        DatabaseReference userRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User")
                .child(currentUser).child("Followings").child(currentUser);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()){
                    userRef.child("UserId").setValue(currentUser);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Intent intent = new Intent(MainActivity.this, BottomBarActivity.class);
        startActivity(intent);


    }

    private void CheckUserExistence() {

        cfUserRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(curUserId).exists()) {
                    if (dataSnapshot.child(curUserId).hasChild("ProfileCreated")) {
                        //SendUserToHomeActivity();
                        SendUserToLoginActivity();
                    } else {
                        // SendUserToSetupActivity();
                        SendUserToLoginActivity();
                    }
                } else {
                    SendUserToLoginActivity();
                }


            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SendUserToLoginActivity() {
        Intent setupIntent = new Intent(MainActivity.this, LoginActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
    }
}