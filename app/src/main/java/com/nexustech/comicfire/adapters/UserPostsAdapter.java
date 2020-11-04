package com.nexustech.comicfire.adapters;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.activities.ViewSinglePostActivity;
import com.nexustech.comicfire.domains.Posts;
import com.nexustech.comicfire.domains.UserPosts;
import com.nexustech.comicfire.utils.HandleActions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.Constants.LIKE_STATUS;

public class UserPostsAdapter extends RecyclerView.Adapter<UserPostsAdapter.PostViewHolder> {

    List<UserPosts> mPostList;
    Context context;


    public UserPostsAdapter(Context context) {
        this.mPostList = new ArrayList<>();
        this.context = context;
    }

    public void addAll(List<UserPosts> newCats) {

        int initSize = newCats.size();
        mPostList.addAll(newCats);
        notifyItemRangeChanged(initSize, newCats.size());

    }

    public String getLastItemId() {
        return mPostList.get(mPostList.size() - 1).getPostKey();
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_post, parent, false);

        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        // holder.tvUserName.setText(mPostList.get(position).getDisplayName());
        //holder.tvPostText.setText(mPostList.get(position).getPostText());
        //Picasso.get().load(mPostList.get(position).getPostImage()).into(holder.ivPostImage);
        //Picasso.get().load(mPostList.get(position).getProfileImage()).into(holder.ivProfileImage);

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(mPostList.get(position).getPostKey());
        postRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String postText = dataSnapshot.child("PostText").getValue().toString();
                    Object postImageUrl = dataSnapshot.child("PostImage").getValue();
                    holder.tvPostText.setText(postText);

                    String userId = dataSnapshot.child("UserId").getValue().toString();
                    holder.showuserDetails(userId);
                    holder.showViews(postRef);

                    if (postImageUrl == null) {
                        holder.ivPostImage.setVisibility(View.GONE);
                        int counted = postText.length();
                        if (counted < 100) {
                            holder.tvPostText.setTextSize(35);
                            holder.tvPostText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            holder.tvPostText.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                        }

                    }else {
                       String postImage=postImageUrl.toString();
                        Picasso.get().load(postImage).into(holder.ivPostImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.manageLikeButton(holder.ivLike, mPostList.get(position).getPostKey(), context);
        holder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // holder.likeActionHandle(context,mPostList.get(position).getPostId());
                holder.openVPostViewActivity(context, mPostList.get(position).getPostKey());
            }
        });

        holder.ivPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.openVPostViewActivity(context, mPostList.get(position).getPostKey());
            }
        });
        holder.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleActions.intentToProfile(context, mPostList.get(position).getPostKey());
            }
        });
        holder.tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleActions.intentToProfile(context, mPostList.get(position).getPostKey());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvPostText, tvlikeCount, tvCommentsCount;
        ImageView ivProfileImage, ivPostImage, ivLike;
        ConstraintLayout constraintLayout;
        View view;

        DatabaseReference likeRef;


        private FirebaseAuth cfAuth;
        private String currentUserId;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvUserName = itemView.findViewById(R.id.tvProfileName);
            ivProfileImage = itemView.findViewById(R.id.ivProfile);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvPostText = itemView.findViewById(R.id.tvDescription);
            tvlikeCount = itemView.findViewById(R.id.tvLikeCount);
            ivLike = itemView.findViewById(R.id.ivLike);
            tvCommentsCount = itemView.findViewById(R.id.tvCommentCount);

            cfAuth = FirebaseAuth.getInstance();
            currentUserId = cfAuth.getCurrentUser().getUid();

        }

        public void manageLikeButton(ImageView ivLike, String postKey, Context context) {
            likeRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Views");
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        long count = dataSnapshot.getChildrenCount();
                        String ct = String.valueOf(count);
                        tvlikeCount.setText(ct + " Views");
                       /* if (dataSnapshot.hasChild(currentUserId)) {
                            LIKE_STATUS = "LIKED";
                            ivLike.setImageResource(R.drawable.fire_default);
                        } else {
                            LIKE_STATUS = "NOT_LIKED";
                            ivLike.setImageResource(R.drawable.fire_light_gray);
                        }

                        */
                    } else {
                        tvlikeCount.setText(0 + " Views");
                        //LIKE_STATUS="NOT_LIKED";
                        //ivLike.setImageResource(R.drawable.fire_light_gray);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            likeRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Comments");
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        long count = dataSnapshot.getChildrenCount();
                        String ct = String.valueOf(count);
                        tvCommentsCount.setText("Comments " + ct);


                       /* if (dataSnapshot.hasChild(currentUserId)) {
                            LIKE_STATUS = "LIKED";
                            ivLike.setImageResource(R.drawable.fire_default);
                        } else {
                            LIKE_STATUS = "NOT_LIKED";
                            ivLike.setImageResource(R.drawable.fire_light_gray);
                        }

                        */
                    } else {
                        tvCommentsCount.setText("Comments " + 0);
                        //LIKE_STATUS="NOT_LIKED";
                        //ivLike.setImageResource(R.drawable.fire_light_gray);
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void likeActionHandle(Context context, String postKey) {
            if (LIKE_STATUS.equals("NOT_LIKED")) {
                like(postKey);
            } else {
                unLike(postKey);
            }

        }

        private void unLike(String postKey) {
            likeRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Likes");
            likeRef.child(currentUserId).removeValue();

            LIKE_STATUS = "NOT_LIKED";
            manageLikeButton(ivLike, postKey, context);
        }

        private void like(String postKey) {
            likeRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Likes");
            likeRef.child(currentUserId).child("UserId").setValue(currentUserId);
            LIKE_STATUS = "LIKED";

            manageLikeButton(ivLike, postKey, context);

        }

        private void openVPostViewActivity(Context context, String postId) {
            Intent intent = new Intent(context, ViewSinglePostActivity.class);
            intent.putExtra("REF_KEY", postId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }


        public void showuserDetails(String userId) {
            FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(userId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String profileName = dataSnapshot.child("DisplayName").getValue().toString();
                                String profileImage = dataSnapshot.child("ProfileImage").getValue().toString();
                                tvUserName.setText(profileName);
                                Picasso.get().load(profileImage).into(ivProfileImage);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        public void showViews(DatabaseReference postRef) {
            postRef.child("Views").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        long count = dataSnapshot.getChildrenCount();
                        String ct = String.valueOf(count);
                        tvlikeCount.setText(ct + " Views");

                    } else {
                        tvlikeCount.setText(0 + " Views");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            postRef.child("Comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        long count = dataSnapshot.getChildrenCount();
                        String ct = String.valueOf(count);
                        tvCommentsCount.setText("Comments " + ct);

                    } else {
                        tvCommentsCount.setText("Comments 0");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}

