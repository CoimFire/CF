package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.utils.HandleActions;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class UserProfileActivity extends AppCompatActivity {
    private String userId;
    private ImageView profileImage,characterImage,ivPosts,followers,followings;
    private TextView displayName,characterName,tvRequestButton,tvFollowingCount, tvFollowerCount, tvPostCount;
    private DatabaseReference cfProfileRef;
    private FirebaseAuth cfAuth;
    private String curUserId;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Utils.setTopBar(getWindow(),getResources());
        cfAuth=FirebaseAuth.getInstance();
        curUserId=cfAuth.getCurrentUser().getUid();

        ivPosts=findViewById(R.id.iv_user_posts);
        profileImage=findViewById(R.id.ivMyProfile);
        characterImage=findViewById(R.id.ivMyCharacter);
        displayName=findViewById(R.id.tvMyName);
        characterName=findViewById(R.id.tvMyCharacter);
        followers=findViewById(R.id.iv_followers);
        followings=findViewById(R.id.iv_followings);
        tvRequestButton=findViewById(R.id.tv_request);


        tvFollowerCount = findViewById(R.id.tv_followers);
        tvFollowingCount = findViewById(R.id.tv_followings);
        tvPostCount = findViewById(R.id.tv_user_posts);

        userId=getIntent().getStringExtra("UserId");
        if (!userId.isEmpty()){
            cfProfileRef= FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User");
            showProfileDetails(cfProfileRef);
            HandleActions.manageRequestButton(userId,tvRequestButton);
            countDetails();
        }

        tvRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleActions.actionHandler(UserProfileActivity.this,userId);
            }
        });

        ivPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(UserProfileActivity.this,UsersPostsActivity.class);
                intent.putExtra("UserId",userId);
                startActivity(intent);


            }
        });

        followings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, ViewFriendsListActivity.class);
                intent.putExtra("TYPE", "Followings");
                startActivity(intent);

            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, ViewFriendsListActivity.class);
                intent.putExtra("TYPE", "Followers");
                startActivity(intent);


            }
        });

    }

    private void countDetails() {
        cfProfileRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("MyPosts")) {
                        long posts = dataSnapshot.child("MyPosts").getChildrenCount();
                        tvPostCount.setText("Posts\n" + String.valueOf(posts));

                    } else {
                        tvPostCount.setText("Posts\n" + 0);

                    }
                    if (dataSnapshot.hasChild("Followings")) {

                        long followings = dataSnapshot.child("Followings").getChildrenCount();
                        tvFollowingCount.setText("Followings\n" + String.valueOf(followings));

                    } else {

                        tvFollowingCount.setText("Followings\n" + 0);
                    }
                    if (dataSnapshot.hasChild("Followers")) {

                        long followers = dataSnapshot.child("Followers").getChildrenCount();
                        tvFollowerCount.setText("Followers\n" + String.valueOf(followers));

                    } else {

                        tvFollowerCount.setText("Followers\n" + 0);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showProfileDetails(DatabaseReference cfProfileRef) {
        cfProfileRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String profImageUrl=dataSnapshot.child("ProfileImage").getValue().toString();
                    Picasso.get().load(profImageUrl).into(profileImage);
                    String charImageUrl=dataSnapshot.child("CharacterImage").getValue().toString();
                    Picasso.get().load(charImageUrl).fit().into(characterImage);
                    String userName=dataSnapshot.child("DisplayName").getValue().toString();
                    displayName.setText(userName);
                    String userCharName=dataSnapshot.child("CharacterName").getValue().toString();
                    characterName.setText(userCharName);

                    profileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HandleActions.viewSingleImage(UserProfileActivity.this,profImageUrl,characterImage.getWidth(),characterImage.getWidth());
                        }
                    });
                    characterImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HandleActions.viewSingleImage(UserProfileActivity.this,charImageUrl,characterImage.getWidth(),characterImage.getWidth()/2);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}