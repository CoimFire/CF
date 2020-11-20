package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class CreateMemeActivity extends AppCompatActivity {
    EditText et1, et2;
    TextView tv, tvClear;
    ImageView imageView;
    private String parentId, curuserId, userName, profileImage, date, time, postText;
    private FirebaseAuth cfAuth;
    private DatabaseReference cfPostRef, parentRef, userRef;
    private StorageReference cfPostImageRef;
    private String downloadUrl, imageUrl;
    private long counter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meme);
        Utils.setTopBar(getWindow(), getResources());

        tv = findViewById(R.id.done);
        imageView = findViewById(R.id.meme_image);
        et1 = findViewById(R.id.first_text);
        et2 = findViewById(R.id.second_text);
        tvClear = findViewById(R.id.clear_all);
        parentId = getIntent().getStringExtra("MemeId");
        postText = getIntent().getStringExtra("Title");
        imageUrl = getIntent().getStringExtra("CoverImage");
        curuserId = Utils.getCurrentUser();
        cfPostRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts");
        parentRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Memes").child(parentId);
        userRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(curuserId);
        cfPostImageRef = FirebaseStorage.getInstance().getReference().child(RELEASE_TYPE).child("PostImages");

        Picasso.get().load(imageUrl).into(imageView);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

            // Log.d("permission", "permission denied to SEND_SMS - requesting it");
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

            requestPermissions(permissions, 1);

        }
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //   String[] wordList = OffensiveWordlist.wordsList;
                String text1 = et1.getText().toString();
                String text2 = et2.getText().toString();

                if (TextUtils.isEmpty(text1) && TextUtils.isEmpty(text2)) {
                    Toast.makeText(CreateMemeActivity.this, "Empty fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (Utils.filterComment(text1) || Utils.filterComment(text2)) {
                        Toast.makeText(CreateMemeActivity.this, "Ouff...Dude Language..!", Toast.LENGTH_SHORT).show();
                    } else {
                        ConstraintLayout shareLayout = findViewById(R.id.constraintLayout);
                        shareLayout.setDrawingCacheEnabled(true);
                        shareLayout.buildDrawingCache();
                        Bitmap bm = shareLayout.getDrawingCache();
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        Uri uri = getBitmapUri(bm);
                        storeMeme(uri);
                    }
                }
            }

        });
        tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et1.getText().clear();
                et2.getText().clear();
            }
        });
    }

    private void storeMeme(Uri uri) {

        String randomKey = Utils.createRandomId();
        date = Utils.getDate();
        time = Utils.gettime();
        final StorageReference filepath = cfPostImageRef.child(uri.getLastPathSegment() + randomKey + "jpg");
        filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CreateMemeActivity.this, "File Uploaded..", Toast.LENGTH_SHORT).show();

                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            userRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (dataSnapshot.exists()) {
                                            userName = dataSnapshot.child("DisplayName").getValue().toString();
                                            profileImage = dataSnapshot.child("ProfileImage").getValue().toString();
                                            cfPostRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    HashMap postMap = new HashMap();
                                                    postMap.put("UserId", curuserId);
                                                    postMap.put("Counter", dataSnapshot.getChildrenCount());
                                                    postMap.put("PostId", randomKey);
                                                    postMap.put("DisplayName", userName);
                                                    postMap.put("ProfileImage", profileImage);
                                                    postMap.put("Date", date);
                                                    postMap.put("Time", time);
                                                    postMap.put("PostText", postText);
                                                    postMap.put("PostImage", downloadUrl);
                                                    postMap.put("IsChallenge", true);
                                                    postMap.put("ParentId", parentId);
                                                    postMap.put("State", "Running");
                                                    cfPostRef.child(randomKey).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(CreateMemeActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                                Utils.openAnotherActivity(CreateMemeActivity.this, BottomBarActivity.class);
                                                            } else {
                                                                String message = task.getException().getMessage();
                                                                Toast.makeText(CreateMemeActivity.this, "Error.." + message, Toast.LENGTH_SHORT).show();

                                                            }
                                                        }
                                                    });
                                                    parentRef.child("ChildMemes").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                counter = dataSnapshot.getChildrenCount();
                                                            } else {
                                                                counter = 0;
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                    parentRef.child("ChildMemes").child(randomKey).child("PostId").setValue(randomKey);
                                                    parentRef.child("ChildMemes").child(randomKey).child("Counter").setValue(counter);

                                                    userRef.child("MyPosts").child(randomKey).child("PostKey").setValue(randomKey);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    });
                }
            }
        });


    }

    private Uri getBitmapUri(Bitmap mergeBitmap) {

        int quality = 100;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        mergeBitmap.compress(Bitmap.CompressFormat.PNG, quality, os);
        String path = MediaStore.Images.Media.insertImage(CreateMemeActivity.this.getContentResolver(), mergeBitmap, "cf_meme", null);
        return Uri.parse(path);
    }
}