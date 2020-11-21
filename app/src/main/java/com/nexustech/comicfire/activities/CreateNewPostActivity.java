package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class CreateNewPostActivity extends AppCompatActivity {

    private ImageView postImage, selectImage;
    private TextView uploadImage;
    private EditText etPostText;
    private static final int GalleryPick = 1;
    private DatabaseReference cfPostRef, cfUserRef;
    private StorageReference cfPostImageRef;
    FirebaseAuth cfAuth;
    private long countPosts = 0;
    String downloadUrl, curUserId, curDate, curTime, userFullname, userProfImage, randomId, postText;
    Uri ImageUri;
    ProgressDialog progressdialog;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_post);
        Utils.setTopBar(getWindow(), getResources());

        postImage = findViewById(R.id.post_image);
        selectImage = findViewById(R.id.iv_select);
        uploadImage = findViewById(R.id.tv_upload);
        etPostText = findViewById(R.id.et_post_text);

        progressdialog = new ProgressDialog(this);
        progressdialog.setMessage("Please Wait....");
        progressdialog.setCancelable(false);

        cfPostImageRef = FirebaseStorage.getInstance().getReference().child(RELEASE_TYPE).child("PostImages");
        cfPostRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts");
        cfUserRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User");

        cfAuth = FirebaseAuth.getInstance();
        curUserId = cfAuth.getCurrentUser().getUid();

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallary();
            }
        });
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeImage();
            }
        });
    }

    private void storeImage() {

        progressdialog.show();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        curDate = dateFormat.format(new Date());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        curTime = timeFormat.format(new Date());
        // Date cDate=dateFormat.parse(date);

        //SimpleDateFormat df = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
        // df.setTimeZone(TimeZone.getDefault());
        randomId = Utils.createRandomId();

        postText = etPostText.getText().toString();

        if (ImageUri != null) {
            final StorageReference filepath = cfPostImageRef.child(ImageUri.getLastPathSegment() + randomId + "jpg");
            filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CreateNewPostActivity.this, "File Uploaded..", Toast.LENGTH_SHORT).show();

                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (cfPostRef != null) {
                                    downloadUrl = uri.toString();
                                    savePostInformation();

                                }
                            }
                        });
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(CreateNewPostActivity.this, "ERROR" + message, Toast.LENGTH_SHORT).show();
                        progressdialog.dismiss();
                    }
                }
            });


        } else if (!postText.isEmpty()) {
            progressdialog.dismiss();
            savePostInformation();
       } else {
            Toast.makeText(CreateNewPostActivity.this, "Wrong move! You can't leave empty handed..", Toast.LENGTH_SHORT).show();
            progressdialog.dismiss();
        }

    }

    private void savePostInformation() {

        cfPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    countPosts = dataSnapshot.getChildrenCount();
                } else {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        cfUserRef.child(curUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.exists()) {
                        userFullname = dataSnapshot.child("DisplayName").getValue().toString();
                        if (dataSnapshot.child("ProfileImage").exists()) {
                            userProfImage = dataSnapshot.child("ProfileImage").getValue().toString();
                        }
                    }


                    if (downloadUrl == null && postText == null) {
                        Toast.makeText(CreateNewPostActivity.this, "Please add any content..(Image/Text)", Toast.LENGTH_SHORT).show();

                    } else {

                        HashMap postMap = new HashMap();
                        postMap.put("UserId", curUserId);
                        postMap.put("Date", curDate);
                        postMap.put("Time", curTime);
                        postMap.put("PostText", postText);
                        postMap.put("PostId", randomId);
                        postMap.put("PostImage", downloadUrl);
                        postMap.put("Counter", countPosts);
                        cfPostRef.child(randomId).updateChildren(postMap)
                                .addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            progressdialog.dismiss();
                                            sendUserToHomeActivity();
                                            Toast.makeText(CreateNewPostActivity.this, "Post Published..", Toast.LENGTH_SHORT).show();
                                        } else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(CreateNewPostActivity.this, "Error.." + message, Toast.LENGTH_SHORT).show();
                                            progressdialog.dismiss();
                                        }

                                    }
                                });


                        HashMap postsMap = new HashMap();
                        postsMap.put("PostKey", randomId);

                        cfUserRef.child(curUserId).child("MyPosts").child(randomId).updateChildren(postsMap);


                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToHomeActivity() {
        Intent intent = new Intent(CreateNewPostActivity.this, BottomBarActivity.class);
        startActivity(intent);
    }

    private void openGallary() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            postImage.setImageURI(ImageUri);
        }

    }
}