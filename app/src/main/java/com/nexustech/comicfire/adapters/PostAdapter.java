package com.nexustech.comicfire.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.nexustech.comicfire.utils.HandleActions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.Constants.LIKE_STATUS;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>{

    List<Posts> mPostList;
    Context context;


    public PostAdapter(Context context) {
        this.mPostList = new ArrayList<>();
        this.context = context;
    }

    public void addAll(List<Posts> newCats){

        int initSize=newCats.size();
        mPostList.addAll(newCats);
        notifyItemRangeChanged(initSize,newCats.size());

    }

    public String getLastItemId(){
        return mPostList.get(mPostList.size()-1).getPostId();
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView= LayoutInflater.from(context).inflate(R.layout.layout_post,parent,false);

        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        holder.tvUserName.setText(mPostList.get(position).getDisplayName());
        holder.tvPostText.setText(mPostList.get(position).getPostText());
        Picasso.get().load(mPostList.get(position).getPostImage()).into(holder.ivPostImage);
        Picasso.get().load(mPostList.get(position).getProfileImage()).into(holder.ivProfileImage);

        holder.manageLikeButton(holder.ivLike,mPostList.get(position).getPostId(),context);
        holder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // holder.likeActionHandle(context,mPostList.get(position).getPostId());
                holder.openVPostViewActivity(context,mPostList.get(position).getPostId());
            }
        });

        holder.ivPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.openVPostViewActivity(context,mPostList.get(position).getPostId());
            }
        });
        holder.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleActions.intentToProfile(context,mPostList.get(position).getUserId());
            }
        });
        holder.tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleActions.intentToProfile(context,mPostList.get(position).getUserId());
            }
        });
        holder.tvCommentsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.openVPostViewActivity(context,mPostList.get(position).getPostId());
            }
        });
        holder.tvlikeCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.openVPostViewActivity(context,mPostList.get(position).getPostId());
            }
        });
        holder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.openVPostViewActivity(context,mPostList.get(position).getPostId());
            }
        });
        holder.ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.openVPostViewActivity(context,mPostList.get(position).getPostId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName,tvPostText,tvlikeCount,tvCommentsCount;
        ImageView ivProfileImage,ivPostImage,ivLike,ivComment;
        ConstraintLayout constraintLayout;
        View view;

        DatabaseReference likeRef;


        private FirebaseAuth cfAuth;
        private String currentUserId;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
            tvUserName=itemView.findViewById(R.id.tvProfileName);
            ivProfileImage=itemView.findViewById(R.id.ivProfile);
            ivPostImage=itemView.findViewById(R.id.ivPostImage);
            tvPostText=itemView.findViewById(R.id.tvDescription);
            tvlikeCount=itemView.findViewById(R.id.tvLikeCount);
            ivLike=itemView.findViewById(R.id.ivLike);
            tvCommentsCount=itemView.findViewById(R.id.tvCommentCount);
            ivComment=itemView.findViewById(R.id.ivComments);

            cfAuth=FirebaseAuth.getInstance();
            currentUserId=cfAuth.getCurrentUser().getUid();

        }

        public void manageLikeButton(ImageView ivLike, String postKey, Context context) {
            likeRef= FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Views");
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        long count=dataSnapshot.getChildrenCount();
                        String ct=String.valueOf(count);
                        tvlikeCount.setText(ct+" Views");
                       /* if (dataSnapshot.hasChild(currentUserId)) {
                            LIKE_STATUS = "LIKED";
                            ivLike.setImageResource(R.drawable.fire_default);
                        } else {
                            LIKE_STATUS = "NOT_LIKED";
                            ivLike.setImageResource(R.drawable.fire_light_gray);
                        }

                        */
                    }
                    else {
                        tvlikeCount.setText(0+" Views");
                        //LIKE_STATUS="NOT_LIKED";
                        //ivLike.setImageResource(R.drawable.fire_light_gray);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            likeRef= FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Comments");
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        long count=dataSnapshot.getChildrenCount();
                        String ct=String.valueOf(count);
                        tvCommentsCount.setText("Comments "+ct);


                       /* if (dataSnapshot.hasChild(currentUserId)) {
                            LIKE_STATUS = "LIKED";
                            ivLike.setImageResource(R.drawable.fire_default);
                        } else {
                            LIKE_STATUS = "NOT_LIKED";
                            ivLike.setImageResource(R.drawable.fire_light_gray);
                        }

                        */
                    }
                    else {
                        tvCommentsCount.setText("Comments "+0);
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
            if(LIKE_STATUS.equals("NOT_LIKED")){
                like(postKey);
            }else {
                unLike(postKey);
            }

        }

        private void unLike(String postKey) {
            likeRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Likes");
            likeRef.child(currentUserId).removeValue();

            LIKE_STATUS="NOT_LIKED";
            manageLikeButton(ivLike,postKey,context);
        }

        private void like(String postKey) {
            likeRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postKey).child("Likes");
            likeRef.child(currentUserId).child("UserId").setValue(currentUserId);
            LIKE_STATUS="LIKED";

            manageLikeButton(ivLike,postKey,context);

        }
        private void openVPostViewActivity(Context context,String postId){
            Intent intent = new Intent(context, ViewSinglePostActivity.class);
            intent.putExtra("REF_KEY", postId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }


    }
}
