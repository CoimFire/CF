package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class EditPostActivity extends AppCompatActivity {

    TextView tvSelectImage, tvSave, tvCancel;
    EditText etPosttext;
    ImageView ivPostImage, ivProfileImage, ivSendComment;
    String postText,defaultPostText, postImage, postId, currentUserId,downloadUrl;
    private static final int GalleryPick = 1;
    DatabaseReference postRef;
    Uri ImageUri;
    ProgressDialog progressdialog;

    private StorageReference cfPostImageRef;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        Utils.setTopBar(this,getWindow(), getResources());

        etPosttext = findViewById(R.id.et_post_text);
        ivPostImage = findViewById(R.id.ivPostImage);
        tvSelectImage = findViewById(R.id.tv_add_or_change);
        tvSave = findViewById(R.id.tv_save);
        tvCancel = findViewById(R.id.tv_cancel);
        progressdialog = new ProgressDialog(this);
        progressdialog.setMessage("Please Wait....");
        progressdialog.setCancelable(false);
        cfPostImageRef = FirebaseStorage.getInstance().getReference().child(RELEASE_TYPE).child("PostImages");

        //postText=getIntent().getStringExtra("postText");
        //postImage=getIntent().getStringExtra("postImage");
        postId = getIntent().getStringExtra("REF_KEY");

        currentUserId = Utils.getCurrentUser();
        postRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postId);

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    defaultPostText = dataSnapshot.child("PostText").getValue().toString();
                    Object postImageUrl = dataSnapshot.child("PostImage").getValue();
                    etPosttext.setText(defaultPostText);

                    // if (dataSnapshot.hasChild("PostImage"))
                    if (postImageUrl != null) {
                        postImage = postImageUrl.toString();
                        Picasso.get().load(postImage).into(ivPostImage);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        tvSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallary();
            }
        });
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeImage();
            }
        });

    }

    private void storeImage() {
        postText = etPosttext.getText().toString();

        if (ImageUri != null && !postText.isEmpty()) {
            final StorageReference filepath = cfPostImageRef.child(ImageUri.getLastPathSegment() + Utils.createRandomId() + "jpg");
            filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditPostActivity.this, "File Uploaded..", Toast.LENGTH_SHORT).show();

                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (postRef != null) {
                                    downloadUrl = uri.toString();
                                    savePostInformation();

                                }
                            }
                        });
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(EditPostActivity.this, "ERROR" + message, Toast.LENGTH_SHORT).show();
                        progressdialog.dismiss();
                    }
                }
            });


        } else {
            if (postText.isEmpty()) {
                progressdialog.dismiss();
                Toast.makeText(EditPostActivity.this, "Wrong move! You can't leave empty handed..", Toast.LENGTH_SHORT).show();
            } else {
                savePostInformation();
            }

        }
    }
    private void savePostInformation() {

        if (downloadUrl != null && postText != null) {
            postRef.child("PostImage").setValue(downloadUrl);
            if (!defaultPostText.equals(postText)){
                postRef.child("PostText").setValue(postText);
            }

            progressdialog.dismiss();
            onBackPressed();
        } else if (downloadUrl!=null){

            postRef.child("PostImage").setValue(downloadUrl);
            progressdialog.dismiss();
            onBackPressed();

        }
        else if (postText!=null){
            if (!defaultPostText.equals(postText)){
                postRef.child("PostText").setValue(postText);
            }
            progressdialog.dismiss();
            onBackPressed();
        }
        else if(downloadUrl != null && postText != null) {
            Toast.makeText(this, "Nothing changed!", Toast.LENGTH_SHORT).show();
        }
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
            ivPostImage.setImageURI(ImageUri);
        }

    }
}