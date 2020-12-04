package com.nexustech.comicfire.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.dynamic.IFragmentWrapper;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static com.nexustech.comicfire.utils.Constants.CURRENT_STATE;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class HandleActions {

    public static String curUserId = Utils.getCurrentUser();
    public static String reason;
    public static String oldPoints;
    public static Boolean isMeme;

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
        TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
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
                    Picasso.get().load(charImageUrl).transform(new RoundedCorners(8,0)).into(ivCharacterImage);
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

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    private static void unfolowUser(String searchedUserId) {
        cfFollowingRef.child(searchedUserId).removeValue();
        DatabaseReference cfFollowerRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(searchedUserId).child("Followers");
        cfFollowerRef.child(curUserId).removeValue();
        reducePoints(10,searchedUserId);
        CURRENT_STATE = "NOT_FOLLOWING";
    }

    private static void followUser(String searchedUserId) {
        cfFollowingRef.child(searchedUserId).child("UserId").setValue(searchedUserId);
        DatabaseReference cfFollowerRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(searchedUserId).child("Followers");
        cfFollowerRef.child(curUserId).child("UserId").setValue(curUserId);
        updatePoints(10,searchedUserId);
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

                postRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        isMeme=(Boolean)dataSnapshot.child("IsMeme").getValue();
                        if (isMeme){
                            Toast.makeText(context, "You cannot delete this post!", Toast.LENGTH_SHORT).show();
                        }else {
                            postRef.child(postId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        DatabaseReference myPostRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE)
                                                .child("User").child(Utils.getCurrentUser()).child("MyPosts").child(postId);
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        };
        handler.post(runnable);
    }

    public static void openPopupForOwn(ImageView ivPostImage, int position, String postId, String postText, Context context) {
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
                    try {
                        shareImageToSocialMedia(ivPostImage,postId,postText,context);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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


    public static void openPopupForOthers(ImageView ivPostImage, int position, String postId , String postText, Context context) {
        View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_general, null);
        AlertDialog dialog = Utils.configDialog(context, rowView);
        TextView tvTitle = rowView.findViewById(R.id.tv_title);
        TextView tvMessage = rowView.findViewById(R.id.tv_message);
        TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = rowView.findViewById(R.id.tv_confirm);
        tvTitle.setText("Share");
        tvMessage.setText("Do you want share to this posts in other social media?");
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    shareImageToSocialMedia(ivPostImage,postId,postText,context);
                } catch (IOException e) {
                    e.printStackTrace();
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

    public static void shareImageToSocialMedia(ImageView ivPostImage, String postId , String postText, Context context) throws IOException {
        BitmapDrawable drawable = (BitmapDrawable) ivPostImage.getDrawable();

        // Bitmap bitmap = ivPostImage.getDrawingCache();


        if (drawable != null) {
            Bitmap bitmap = drawable.getBitmap();
            //   Toast.makeText(context, "hghjhj", Toast.LENGTH_SHORT).show();
            File sdCard = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File dir = new File(sdCard.getAbsolutePath() + "/ComicFire");
            dir.mkdir();
            String fname = postId + Constants.SCREENSHOT_JPG_STRING;
            File outFile = new File(dir, fname);
            // File file = new File(myDir, sdCard);

            FileOutputStream out = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            out.flush();
            out.close();


            Bitmap appIconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.comicfire_logo);

            Bitmap resizedAppIconBitmap = Bitmap.createScaledBitmap(appIconBitmap, 125, 125, false);
            Bitmap resizedScreenBitmap = Bitmap.createScaledBitmap(bitmap, ivPostImage.getWidth(), ivPostImage.getHeight(), false);

            Bitmap mergeBitmap = mergeTwoBitmap(resizedScreenBitmap, resizedAppIconBitmap, context);
            Uri mergeUri = getBitmapUri(mergeBitmap, context, postId);
            Shareable shareInstance = new Shareable.Builder(context)
                    .message(postText+"\n\n #ComicFire")
                    .socialChannel(Shareable.Builder.ANY)
                    .image(mergeUri)
                    .url("www.comicfire.com")
                    .build();
            shareInstance.share();
        }else {
            Shareable shareInstance = new Shareable.Builder(context)
                    .message(postText+"\n\n #ComicFire")
                    .socialChannel(Shareable.Builder.ANY)
                    .url("www.comicfire.com")
                    .build();
            shareInstance.share();
        }
    }

    private static Bitmap mergeTwoBitmap(Bitmap resizedScreenBitmap, Bitmap resizedAppIconBitmap, Context context) {
        int resizedScreenWidth = resizedScreenBitmap.getWidth();
        int resizedScreenHeight = resizedScreenBitmap.getHeight();
        int left = 5;
        int top = 5;
        Bitmap mergeBitmap = Bitmap.createBitmap(resizedScreenWidth, resizedScreenHeight, resizedScreenBitmap.getConfig());
        Canvas canvas = new Canvas(mergeBitmap);
        canvas.drawBitmap(resizedScreenBitmap, new Matrix(), null);
        canvas.drawBitmap(resizedAppIconBitmap, left, top, null);
        return mergeBitmap;
    }

    private static Uri getBitmapUri(Bitmap mergeBitmap, Context context, String postId) {
        int quality = 100;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        mergeBitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), mergeBitmap, postId, null);
        return Uri.parse(path);
    }

    public static void openReportPostPopup(int i, String postId, String userId, Context context){
        View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_report, null);
        AlertDialog dialog = Utils.configDialog(context, rowView);
        TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = rowView.findViewById(R.id.tv_confirm);
        Utils.setDialogPosition(dialog);

        RadioGroup rg = rowView.findViewById(R.id.radio);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId){
                    case R.id.radio_abusive:
                        reason="Abusive";
                        break;
                    case R.id.radio_sexual_content:
                        reason="Sexual Content";
                        break;
                    case R.id.radio_irrelevent:
                        reason="Irrelevent";
                        break;
                    case R.id.radio_something_else:
                        reason="Something Else";
                        break;
                }

            }
        });

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id=Utils.createRandomId();
                String type;
                if (i==1)
                    type="Post";
                else
                    type="User";

                HashMap hashMap=new HashMap();
                hashMap.put("ReportId",id);
                hashMap.put("PostId",postId);
                hashMap.put("UserId",userId);
                hashMap.put("Type",type);
                hashMap.put("Reason",reason);
                FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Reports").child(id)
                        .updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Toast.makeText(context, "Reported successfully", Toast.LENGTH_SHORT).show();
                    }
                });
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

    public static void editComment(Context context,String commentText,DatabaseReference commentRef){
        View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_edit_comment, null);
        AlertDialog dialog = Utils.configDialog(context, rowView);
        EditText etComment = rowView.findViewById(R.id.et_change_comment);
        TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = rowView.findViewById(R.id.tv_confirm);
        etComment.setText(commentText);

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String edittedComment=etComment.getText().toString();
                if (!commentText.equals(edittedComment)) {
                  commentRef.child("Comment").setValue(edittedComment);
                    dialog.dismiss();

                }else {
                    Toast.makeText(context, "Nothing changed!", Toast.LENGTH_SHORT).show();
                }


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


    public static void replyCommentPopup(Context context,String postId,String parentCommentId,String parentCommentUserId,String postUserId){
        View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_edit_comment, null);
        AlertDialog dialog = Utils.configDialog(context, rowView);
        EditText etComment = rowView.findViewById(R.id.et_change_comment);
        TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = rowView.findViewById(R.id.tv_confirm);
        TextView tvTitle=rowView.findViewById(R.id.tv_title);
        tvTitle.setText("Reply comment");
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference commentRef;
                String edittedComment=etComment.getText().toString();
                commentRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postId).child("Comments").child(parentCommentId);
                //commentRef.child("CommentText").setValue(edittedComment);
                replyComment(context,edittedComment,parentCommentId,parentCommentUserId,postUserId,commentRef);

                dialog.dismiss();
              //  Intent intent=new Intent(context,ViewSinglePostActivity.class);
               // intent.putExtra("REF_KEY",postId);
                //context.startActivity(intent);


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

    public static void replyComment(Context context, String commentText, String parentCommentId, String parentCommentUserId, String postUserId,DatabaseReference commentRef) {
        String randomId = Utils.createRandomId();


        HashMap hashMap = new HashMap();
        hashMap.put("CommentId", randomId);
        hashMap.put("RepliedUserId", Utils.getCurrentUser());
        hashMap.put("CommentText", commentText);
        hashMap.put("ParentCommentId", parentCommentId);
        hashMap.put("ParentCommentUserId", parentCommentUserId);
        hashMap.put("ParentUserId", postUserId);

        commentRef.child("Replies").child(randomId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Done!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void deleteComment(Context context, DatabaseReference commentRef){
        View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_general, null);
        AlertDialog dialog = Utils.configDialog(context, rowView);
        TextView tvTitle = rowView.findViewById(R.id.tv_title);
        TextView tvMessage = rowView.findViewById(R.id.tv_message);
        TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = rowView.findViewById(R.id.tv_confirm);
        tvTitle.setText("Delete Comment");
        tvMessage.setText("Are you sure you want to delete this comment?");
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             commentRef.removeValue();
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
    public static void updatePoints(int newPoints, String userId){

        DatabaseReference userRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(userId).child("Points");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    oldPoints=dataSnapshot.getValue().toString();
                    int oPoints = Integer.parseInt(oldPoints);
                    int totalPoints = oPoints + newPoints;
                    userRef.setValue(String.valueOf(totalPoints));

                }else {
                    userRef.setValue(String.valueOf(newPoints));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public static void reducePoints(int newPoints, String userId) {
        DatabaseReference userRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(userId).child("Points");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    oldPoints=dataSnapshot.getValue().toString();
                    int oPoints = Integer.parseInt(oldPoints);
                    if (oPoints >= 10) {
                        int totalPoints = oPoints - newPoints;
                        userRef.setValue(String.valueOf(totalPoints));
                    }

                }else {
                    userRef.setValue("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
