package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.domains.Posts;
import com.nexustech.comicfire.domains.Winner;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class MemeDetailsActivity extends AppCompatActivity {

    private String coverImageUrl, parentkey, postText, curuserId,state;
    ImageView coverImage;
    TextView accept;
    RecyclerView rvChildList;
    DatabaseReference cfChildernMemes, cfPostRef,cfWinnersRef;
    FirebaseAuth cfAuth;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_details);

        Utils.setTopBar(this,getWindow(), getResources());
        cfAuth = FirebaseAuth.getInstance();
        coverImageUrl = getIntent().getStringExtra("CoverImage");
        parentkey = getIntent().getStringExtra("MemeId");
        postText = getIntent().getStringExtra("Title");

        state=getIntent().getStringExtra("State");
        cfPostRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts");
        cfChildernMemes = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Memes").child(parentkey).child("ChildMemes");
        cfWinnersRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Memes").child(parentkey)
                .child("Winners");


        curuserId = Utils.getCurrentUser();
        coverImage = findViewById(R.id.iv_cover);
        accept = findViewById(R.id.tv_accept);
        Picasso.get().load(coverImageUrl).into(coverImage);
        rvChildList = findViewById(R.id.rv_child);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View rowView = LayoutInflater.from(MemeDetailsActivity.this).inflate(R.layout.alert_dialog_general, null);
                AlertDialog dialog = Utils.configDialog(MemeDetailsActivity.this, rowView);
                TextView tvTitle = rowView.findViewById(R.id.tv_title);
                TextView tvMessage = rowView.findViewById(R.id.tv_message);
                TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
                TextView tvConfirm = rowView.findViewById(R.id.tv_confirm);
                tvTitle.setText("Rules");
                tvConfirm.setText("Continue");
                tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                tvMessage.setText(
                        "1. User can create any number of memes.\n\n" +
                                "2. Each input fields allowed less than 3 lines.\n\n" +
                                "3. Abusive words and 18+ contents are stricktly prohibitted.\n\n" +
                                "4. The winner selected by Comic Fire Admin.\n\n" +
                                "5. User will get points for their responsible result");
                tvConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(MemeDetailsActivity.this, CreateMemeActivity.class);
                        intent.putExtra("MemeId", parentkey);
                        intent.putExtra("Title", postText);
                        intent.putExtra("CoverImage", coverImageUrl);
                        startActivity(intent);
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
        // rvChildList.setHasFixedSize(true);
        // rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(MemeDetailsActivity.this);
        rvChildList.setHasFixedSize(true);

        // Set the layout manager to your recyclerview

        Utils.showEmpty(getWindow().getDecorView().getRootView(), cfChildernMemes);
        if ("Finished".equals(state)){
            showWinners();
            mLayoutManager.setReverseLayout(false);
            //Toast.makeText(this, state, Toast.LENGTH_SHORT).show();
        }else {

            mLayoutManager.setStackFromEnd(true);
            mLayoutManager.setReverseLayout(true);
            showChildMemes();
        }

        rvChildList.setLayoutManager(mLayoutManager);
        //showChildMemes(R.layout.layout_post);
    }


    private void anounceResult(String parentkey) {
        DatabaseReference memRef = FirebaseDatabase.getInstance().getReference().child("Memes").child(parentkey);
        memRef.child("State").setValue("Finished");
    }

    private void showChildMemes() {
        Query postQuery = cfChildernMemes.orderByKey();
        FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostViewHolder>(
                        Posts.class,
                        R.layout.layout_post,
                        PostViewHolder.class,
                        postQuery

                ) {
                    @Override
                    protected void populateViewHolder(PostViewHolder postViewHolder, Posts model, int position) {
                        String postKey = getRef(position).getKey();
                        cfPostRef.child(postKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    postViewHolder.setUserDetails(dataSnapshot.child("UserId").getValue().toString());
                                    postViewHolder.setPostImage(dataSnapshot.child("PostImage").getValue().toString());
                                    postViewHolder.setPostText(dataSnapshot.child("PostText").getValue().toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        postViewHolder.manageLikeButton(postViewHolder.ivLike, postKey, MemeDetailsActivity.this);

                    }
                };

        rvChildList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        View cfView;
        TextView profName, tvPostText, likeCount, tvChatacterName;
        ImageView profImage, ivPostImage, ivLike, tribleDot;
        private FirebaseAuth cfAuth;
        private String currentUserId, postId;
        DatabaseReference checkReqSent, likeRef, cfMemeRef;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;
            profImage = cfView.findViewById(R.id.ivProfile);
            profName = cfView.findViewById(R.id.tvProfileName);
            ivPostImage = cfView.findViewById(R.id.ivPostImage);
            tvPostText = cfView.findViewById(R.id.tvDescription);
            ivLike = cfView.findViewById(R.id.ivLike);
            tribleDot = cfView.findViewById(R.id.trible_dot);
            tvChatacterName = cfView.findViewById(R.id.tv_character_name);
            cfAuth = FirebaseAuth.getInstance();
            currentUserId = cfAuth.getCurrentUser().getUid();
            likeCount = cfView.findViewById(R.id.tvLikeCount);
            cfMemeRef = FirebaseDatabase.getInstance().getReference().child("Memes");
            tribleDot.setVisibility(View.INVISIBLE);

        }


        public void setUserDetails(String userId) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("CharacterName").getValue().toString();
                        String displayName = dataSnapshot.child("DisplayName").getValue().toString();
                        String profile = dataSnapshot.child("ProfileImage").getValue().toString();

                        tvChatacterName.setText(name);
                        profName.setText(displayName);

                        Picasso.get().load(profile).into(profImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        public void setPostText(String postText) {
            tvPostText.setText(postText);
        }

        public void setPostImage(String postImage) {
            Picasso.get().load(postImage).into(ivPostImage);
        }


        public void manageLikeButton(ImageView ivLike, String postKey, Context context) {
            likeRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Views");
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        long count = dataSnapshot.getChildrenCount();
                        String ct = String.valueOf(count);
                        likeCount.setText(ct);

                    } else {
                        likeCount.setText("0");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
    private void showWinners() {

        Query query= cfWinnersRef.orderByChild("Rank");
        FirebaseRecyclerAdapter<Winner, WinnerViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Winner, WinnerViewHolder>(
                        Winner.class,
                        R.layout.layout_winner,
                        WinnerViewHolder.class,
                        query

                ) {
                    @Override
                    protected void populateViewHolder(WinnerViewHolder postViewHolder, Winner model, int position) {
                        String postKey = getRef(position).getKey();
                        cfPostRef.child(postKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    postViewHolder.setUserDetails(dataSnapshot.child("UserId").getValue().toString(),model.getRank(),MemeDetailsActivity.this);
                                    // postViewHolder.setPostImage(dataSnapshot.child("PostImage").getValue().toString());
                                    //postViewHolder.setPostText(dataSnapshot.child("PostText").getValue().toString());

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        // postViewHolder.manageLikeButton(postViewHolder.ivLike, postKey, MemeDetailsActivity.this);

                    }
                };

        rvChildList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class WinnerViewHolder extends RecyclerView.ViewHolder {
        View cfView;
        TextView profName, tvRank;
        ImageView profImage,ivStroke,ivLayer;


        public WinnerViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;
            profImage = cfView.findViewById(R.id.iv_profile_image);
            profName = cfView.findViewById(R.id.tv_user_name);
            tvRank=cfView.findViewById(R.id.tv_rank);
            ivStroke=cfView.findViewById(R.id.iv_profile_stroke);
            ivLayer=cfView.findViewById(R.id.iv_gold_label);
        }


        public void setUserDetails(String userId, String rank, Context context) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                     //   String name = dataSnapshot.child("CharacterName").getValue().toString();
                        String displayName = dataSnapshot.child("DisplayName").getValue().toString();
                        String profile = dataSnapshot.child("ProfileImage").getValue().toString();

                        //tvChatacterName.setText(name);
                        profName.setText(displayName);

                        Picasso.get().load(profile).into(profImage);
                        tvRank.setText(rank);
                        if (rank.equals("01")){
                            changeColor(context.getResources().getColor(R.color.goldenrod));
                        }else if (rank.equals("02")){
                            changeColor(context.getResources().getColor(R.color.silver));
                        }else {
                            changeColor(context.getResources().getColor(R.color.peach_puff));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void changeColor(int color) {
            ivStroke.setColorFilter(color);
            ivLayer.setColorFilter(color);
            tvRank.getBackground().setTint(color);
            profName.getBackground().setTint(color);

        }

    }
}