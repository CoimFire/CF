package com.nexustech.comicfire.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.activities.MainActivity;
import com.nexustech.comicfire.activities.UserProfileActivity;
import com.nexustech.comicfire.activities.UsersPostsActivity;
import com.nexustech.comicfire.activities.ViewAllCharsActivity;
import com.nexustech.comicfire.activities.ViewFriendsListActivity;
import com.nexustech.comicfire.activities.ViewSinglePostActivity;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class ProfileFragment extends Fragment {

    private DatabaseReference cfProfileRef;
    private FirebaseAuth cfAuth;
    private String curUserId,points;
    private TextView myPoints;
    int total;

    private TextView charName, myName, logout, tvFollowingCount, tvFollowerCount, tvPostCount;
    private ImageView charImage, profileImage, myPost, following, follower, heroShop;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        cfAuth = FirebaseAuth.getInstance();
        curUserId = Utils.getCurrentUser();
        cfProfileRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(curUserId);
        charName = root.findViewById(R.id.tvMyCharacter);
        myName = root.findViewById(R.id.tvMyName);
        charImage = root.findViewById(R.id.ivMyCharacter);
        profileImage = root.findViewById(R.id.ivMyProfile);
        myPost = root.findViewById(R.id.iv_user_posts);
        logout = root.findViewById(R.id.tvLogout);
        following = root.findViewById(R.id.iv_followings);
        follower = root.findViewById(R.id.iv_followers);
        myPoints = root.findViewById(R.id.tvMyPoints);
        heroShop = root.findViewById(R.id.hero_shop_layer);

        tvFollowerCount = root.findViewById(R.id.tv_followers);
        tvFollowingCount = root.findViewById(R.id.tv_followings);
        tvPostCount = root.findViewById(R.id.tv_user_posts);


        clickOnViews();
        showProfile();
        countDetails();
        total=Utils.getMyPoints();
        heroShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewAllCharsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Points", total);
                startActivity(intent);

            }
        });


        return root;
    }

    private void countDetails() {
        cfProfileRef.addValueEventListener(new ValueEventListener() {
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
                        tvFollowingCount.setText("Followings\n" + String.valueOf(followings-1));

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

    private void clickOnViews() {
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View rowView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_general, null);
                AlertDialog dialog = Utils.configDialog(getContext(), rowView);
                TextView tvTitle = rowView.findViewById(R.id.tv_title);
                TextView tvMessage = rowView.findViewById(R.id.tv_message);
                TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
                TextView tvConfirm = rowView.findViewById(R.id.tv_confirm);
                tvTitle.setText("Confirm Sign out");
                tvMessage.setText("Are you sure you want to signout?");
                tvConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cfAuth.signOut();
                        Utils.openAnotherActivity(getContext(), MainActivity.class);
                    }
                });
                tvCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });

        myPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UsersPostsActivity.class);
                intent.putExtra("UserId", Utils.getCurrentUser());
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ViewFriendsListActivity.class);
                intent.putExtra("UserId",Utils.getCurrentUser());
                intent.putExtra("TYPE", "Followings");
                startActivity(intent);

            }
        });

        follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ViewFriendsListActivity.class);
                intent.putExtra("TYPE", "Followers");
                intent.putExtra("UserId",Utils.getCurrentUser());
                startActivity(intent);


            }
        });

    }


    private void showProfile() {
        cfProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("DisplayName").getValue().toString();
                    String characterName = dataSnapshot.child("CharacterName").getValue().toString();
                    String profImage = dataSnapshot.child("ProfileImage").getValue().toString();
                    String characterImage = dataSnapshot.child("CharacterImage").getValue().toString();
                    myName.setText(name);
                    charName.setText(characterName);
                    Picasso.get().load(profImage).into(profileImage);
                    Picasso.get().load(characterImage).fit().into(charImage);

                    if (dataSnapshot.hasChild("Points")) {
                        points=dataSnapshot.child("Points").getValue().toString();
                        myPoints.setText(points);

                    } else {
                        myPoints.setText("0");
                        points="0";
                    }
                    SharedPreferences preferences=getContext().getSharedPreferences("USER", Context.MODE_PRIVATE);
                    String oldPoints=preferences.getString("POINTS","0");
                    if (!oldPoints.equals(points)){
                        cfProfileRef.child("Points").setValue(points);
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putString("POINTS",points);
                        editor.apply();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}