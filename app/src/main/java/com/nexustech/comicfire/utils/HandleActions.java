package com.nexustech.comicfire.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.activities.BottomBarActivity;
import com.nexustech.comicfire.activities.EditPostActivity;
import com.nexustech.comicfire.activities.UserProfileActivity;
import com.nexustech.comicfire.activities.ViewSinglePostActivity;
import com.squareup.picasso.Picasso;

import static com.nexustech.comicfire.utils.Constants.CURRENT_STATE;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class HandleActions {

    public static String curUserId = Utils.getCurrentUser();
    public static DatabaseReference cfFollowingRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(curUserId).child("Followings");

    public static void manageRequestButton(String searchedUserId, TextView tvRequest) {
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


    public static void actionHandler(Context context, String searchedUserId) {
        if (CURRENT_STATE.equals("NOT_FOLLOWING")) {
            popupForFollowOrUnfollow(context, searchedUserId, "FOLLOW");
        } else {
            popupForFollowOrUnfollow(context, searchedUserId, "UNFOLLOW");
        }
    }

    public static void popupForFollowOrUnfollow(Context context, String searchedUserId, String type) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_follow_or_unfollow, null);
        AlertDialog dialog = Utils.configDialog(context, rowView);
        ImageView ivProfileImage = rowView.findViewById(R.id.ivMyProfile);

        ImageView ivCharacterImage = rowView.findViewById(R.id.ivMyCharacter);
        TextView tvCharacterName = rowView.findViewById(R.id.tvMyCharacter);
        TextView tvDisplayName = rowView.findViewById(R.id.tvMyName);
        TextView tvFollowingCount = rowView.findViewById(R.id.tv_followings);
        TextView tvFollowerCount = rowView.findViewById(R.id.tv_followers);
        TextView tvPostCount = rowView.findViewById(R.id.tv_user_posts);
        TextView tvRequest = rowView.findViewById(R.id.tv_request);
        DatabaseReference cfProfileRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User");

        if (type.equals("FOLLOW")) {
            tvRequest.setText("Follow");
        } else {
            tvRequest.setText("Unfollow");
        }
        cfProfileRef.child(searchedUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profImageUrl = dataSnapshot.child("ProfileImage").getValue().toString();
                    Picasso.get().load(profImageUrl).into(ivProfileImage);
                    String charImageUrl = dataSnapshot.child("CharacterImage").getValue().toString();
                    Picasso.get().load(charImageUrl).into(ivCharacterImage);
                    String userName = dataSnapshot.child("DisplayName").getValue().toString();
                    tvDisplayName.setText(userName);
                    String userCharName = dataSnapshot.child("CharacterName").getValue().toString();
                    tvCharacterName.setText(userCharName);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        cfProfileRef.child(searchedUserId).addValueEventListener(new ValueEventListener() {
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


        tvRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equals("FOLLOW")) {
                    followUser(searchedUserId);
                } else {
                    unfolowUser(searchedUserId);
                }
                dialog.dismiss();
            }
        });
        dialog.show();

    }


    private static void unfolowUser(String searchedUserId) {
        cfFollowingRef.child(searchedUserId).removeValue();
        DatabaseReference cfFollowerRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(searchedUserId).child("Followers");
        cfFollowerRef.child(curUserId).removeValue();
        CURRENT_STATE = "NOT_FOLLOWING";
    }

    private static void followUser(String searchedUserId) {
        cfFollowingRef.child(searchedUserId).child("UserId").setValue(searchedUserId);
        DatabaseReference cfFollowerRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(searchedUserId).child("Followers");
        cfFollowerRef.child(curUserId).child("UserId").setValue(curUserId);
        CURRENT_STATE = "FOLLOWING";

    }

    public static void intentToProfile(Context context, String searchedUserId) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra("UserId", searchedUserId);
        context.startActivity(intent);

    }

    public static void viewSingleImage(Context context, String imageUrl, int x, int y) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_view_image, null);
        AlertDialog dialog = Utils.configDialog(context, rowView);
        ImageView ivClose = rowView.findViewById(R.id.iv_close);
        ImageView ivImage = rowView.findViewById(R.id.iv_common_image);
        TextView tvClose = rowView.findViewById(R.id.tv_close);
        Picasso.get().load(imageUrl).resize(x, y).into(ivImage);
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void deletePost(Context context, String postId) {

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts");
                postRef.child(postId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference myPostRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE)
                                    .child("User").child(Utils.getCurrentUser()).child("MyPosts");
                            myPostRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Utils.openAnotherActivity(context, BottomBarActivity.class);
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                });
            }
        };
        handler.post(runnable);
    }

    public static void openPopupForOwn(int position, String postId, Context context) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_general, null);
        AlertDialog dialog = Utils.configDialog(context, rowView);
        TextView tvTitle = rowView.findViewById(R.id.tv_title);
        TextView tvMessage = rowView.findViewById(R.id.tv_message);
        TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = rowView.findViewById(R.id.tv_confirm);

        if (position == 0) {
            tvTitle.setText("Edit");
            tvMessage.setText("Do you want edit this post?");
        } else if (position == 1) {
            tvTitle.setText("Delete");
            tvMessage.setText("Are you sure you want delete this post?");
        } else if (position == 2) {
            tvTitle.setText("Share");
            tvMessage.setText("Do you want share to this posts in other social media?");
        }
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    openPostEditActivity(context, postId);
                } else if (position == 1) {
                    deletePost(context, postId);
                } else if (position == 2) {
                    //sharePost();
                }
                dialog.dismiss();
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

    public static void openPopupForOthers(int position, String postId, Context context) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_general, null);
        AlertDialog dialog = Utils.configDialog(context, rowView);
        TextView tvTitle = rowView.findViewById(R.id.tv_title);
        TextView tvMessage = rowView.findViewById(R.id.tv_message);
        TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = rowView.findViewById(R.id.tv_confirm);

        if (position == 0) {
            tvTitle.setText("Share");
            tvMessage.setText("Do you want share to this posts in other social media?");
        } else if (position == 1) {
            tvTitle.setText("Report");
            tvMessage.setText("Do you want report this post?");
        } else if (position == 2) {
            tvTitle.setText("Report");
            tvMessage.setText("Do you want report this user?");
        }
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    //sharePost();
                } else if (position == 1) {
                    //reportPost();
                    Toast.makeText(context, "Reported", Toast.LENGTH_SHORT).show();
                } else if (position == 2) {
                    //reportUser();
                    Toast.makeText(context, "Reported", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
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

    public static void openVPostViewActivity(Context context, String postId) {
        Intent intent = new Intent(context, ViewSinglePostActivity.class);
        intent.putExtra("REF_KEY", postId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void openPostEditActivity(Context context, String postId) {
        Intent intent = new Intent(context, EditPostActivity.class);
        intent.putExtra("REF_KEY", postId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

}
