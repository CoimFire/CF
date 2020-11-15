package com.nexustech.comicfire.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.domains.Users;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static com.nexustech.comicfire.utils.Constants.CURRENT_STATE;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.Utils.showEmpty;

public class ViewFriendsListActivity extends AppCompatActivity {
    private TextView tvHeading;
    private ImageView ivSearch;
    private EditText etSearchText;
    private RecyclerView rvSearchedFriendsList;
    private RecyclerView rvAllUser;
    private DatabaseReference cfFindFriendsRef;
    private FirebaseAuth cfAuth;
    private String curUserId, friendsType;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friends_list);
        Utils.setTopBar(getWindow(),getResources());

        ivSearch = findViewById(R.id.ivSearchFriends);
        etSearchText = findViewById(R.id.etSearchFriends);
        rvSearchedFriendsList = findViewById(R.id.rvAllUsers);
        curUserId = Utils.getCurrentUser();

        friendsType = getIntent().getStringExtra("TYPE");
        cfFindFriendsRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(curUserId).child(friendsType);
        rvSearchedFriendsList.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvSearchedFriendsList.setLayoutManager(mLayoutManager);
        SearchPeopleAndFriends("");



        showEmpty(getWindow().getDecorView().getRootView(),cfFindFriendsRef);
    }

    private void SearchPeopleAndFriends(String searchInput) {
        Query searchPeopleAndFriendsQuery = cfFindFriendsRef.orderByKey();

        FirebaseRecyclerAdapter<Users, UserViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                        Users.class,
                        R.layout.layout_find_friends,
                        UserViewHolder.class,
                        searchPeopleAndFriendsQuery

                ) {
                    @Override
                    protected void populateViewHolder(UserViewHolder findFriendsViewHolder, Users model, int position) {

                        //findFriendsViewHolder.setDisplayName(model.getDisplayName());
                        //findFriendsViewHolder.setCharacterName(model.getCharacterName());

                        String searchedUserId = getRef(position).getKey();
                        findFriendsViewHolder.showDetails(searchedUserId);
                        findFriendsViewHolder.manageRequestButton(searchedUserId, findFriendsViewHolder.tvRequest);
                        findFriendsViewHolder.tvRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                findFriendsViewHolder.actionHandler(searchedUserId);

                            }
                        });

                        findFriendsViewHolder.profImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                findFriendsViewHolder.intentToProfile(ViewFriendsListActivity.this, searchedUserId);
                            }
                        });

                        findFriendsViewHolder.profName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                findFriendsViewHolder.intentToProfile(ViewFriendsListActivity.this, searchedUserId);

                            }
                        });

                    }
                };
        rvSearchedFriendsList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public View cfView;
        public TextView tvRequest;
        public TextView profName;
        public TextView tvCharacterName;
        //  private String CURRENT_STATE;
        public FirebaseAuth cfAuth;
        public String currentUserId, profileName, profileImage, characterName;
        public DatabaseReference cfFollowerRef;
        public DatabaseReference cfFollowingRef, userRef;
        public ImageView profImage;


        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;
            tvRequest = cfView.findViewById(R.id.tv_request);
            cfAuth = FirebaseAuth.getInstance();
            currentUserId = cfAuth.getCurrentUser().getUid();
            cfFollowerRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(currentUserId).child("Followers");
            cfFollowingRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(currentUserId).child("Followings");
            profImage = cfView.findViewById(R.id.iv_profile);
            profName = cfView.findViewById(R.id.tv_user_name);
            tvCharacterName = cfView.findViewById(R.id.tv_character_name);
        }

        public void showDetails(String userId) {
            userRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(userId);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        profileName = dataSnapshot.child("DisplayName").getValue().toString();
                        profName.setText(profileName);
                        characterName = dataSnapshot.child("CharacterName").getValue().toString();
                        tvCharacterName.setText(profileName);
                        profileImage = dataSnapshot.child("ProfileImage").getValue().toString();
                        Picasso.get().load(profileImage).into(profImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void manageRequestButton(String searchedUserId, TextView tvRequest) {
            String curUserId = cfAuth.getCurrentUser().getUid();
            if (curUserId.equals(searchedUserId)) {
                tvRequest.setVisibility(View.INVISIBLE);
            }
            cfFollowingRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(searchedUserId)) {
                        tvRequest.setText("Unfollow");
                        CURRENT_STATE = "FOLLOWING";

                    } else {
                        tvRequest.setText("Folllow");
                        CURRENT_STATE = "NOT_FOLLOWING";
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        public void actionHandler(String searchedUserId) {
            if (CURRENT_STATE.equals("NOT_FOLLOWING")) {
                followUser(searchedUserId);
            } else {
                unfolowUser(searchedUserId);
            }
        }

        public void intentToProfile(Context context, String searchedUserId) {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("UserId", searchedUserId);
            context.startActivity(intent);


        }

        public void unfolowUser(String searchedUserId) {
            cfFollowingRef.child(searchedUserId).removeValue();
            DatabaseReference cfFollowerRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(searchedUserId).child("Followers");
            cfFollowerRef.child(currentUserId).removeValue();
            CURRENT_STATE = "NOT_FOLLOWING";
        }

        public void followUser(String searchedUserId) {
            cfFollowingRef.child(searchedUserId).child("UserId").setValue(searchedUserId);
            DatabaseReference cfFollowerRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(searchedUserId).child("Followers");
            cfFollowerRef.child(currentUserId).child("UserId").setValue(currentUserId);
            CURRENT_STATE = "FOLLOWING";

        }
    }

}