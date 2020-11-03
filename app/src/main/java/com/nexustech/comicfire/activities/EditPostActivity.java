package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class EditPostActivity extends AppCompatActivity {

    TextView tvSelectImage, tvSave, tvCancel;
    EditText etPosttext;
    ImageView ivPostImage, ivProfileImage, ivSendComment;
    String postText, postImage, postId, currentUserId;

    DatabaseReference postRef;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        Utils.setTopBar(getWindow(), getResources());

        etPosttext = findViewById(R.id.et_post_text);
        ivPostImage = findViewById(R.id.ivPostImage);
        tvSelectImage = findViewById(R.id.tv_add_or_change);
        tvSave = findViewById(R.id.tv_save);
        tvCancel = findViewById(R.id.tv_cancel);


        //postText=getIntent().getStringExtra("postText");
        //postImage=getIntent().getStringExtra("postImage");
        postId = getIntent().getStringExtra("REF_KEY");

        currentUserId = Utils.getCurrentUser();
        postRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postId);
        postRef.child("Views").child(currentUserId).child("UserId").setValue(currentUserId);
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    postText = dataSnapshot.child("PostText").getValue().toString();
                    Object postImageUrl = dataSnapshot.child("PostImage").getValue();
                    etPosttext.setText(postText);

                    // if (dataSnapshot.hasChild("PostImage"))
                    if (postImageUrl == null) {
                        ivPostImage.setVisibility(View.GONE);

                    } else {
                        postImage = postImageUrl.toString();
                        Picasso.get().load(postImage).into(ivPostImage);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}