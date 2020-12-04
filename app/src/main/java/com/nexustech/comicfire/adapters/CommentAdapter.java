package com.nexustech.comicfire.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.activities.ViewCommentActivity;
import com.nexustech.comicfire.activities.ViewSinglePostActivity;
import com.nexustech.comicfire.domains.Comments;
import com.nexustech.comicfire.utils.HandleActions;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.HandleActions.deleteComment;
import static com.nexustech.comicfire.utils.HandleActions.editComment;
import static com.nexustech.comicfire.utils.HandleActions.replyCommentPopup;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {


    List<Comments> mCommentList;
    Context context;
    String profileName,profileImage;

    public CommentAdapter(Context context) {
        this.mCommentList = new ArrayList<>();
        this.context = context;
    }

    public void addAll(List<Comments> newComments) {

        int initSize = newComments.size();
        mCommentList.addAll(newComments);
        notifyItemRangeChanged(initSize, newComments.size());

    }

    public String getLastItemId() {
        return mCommentList.get(mCommentList.size() - 1).getCommenterId();
    }


    @NonNull
    @Override
    public CommentAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_comment, parent, false);

        return new CommentAdapter.CommentViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CommentViewHolder holder, int position) {
        String userId = mCommentList.get(position).getCommenterId();
        FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    profileName=dataSnapshot.child("DisplayName").getValue().toString();
                    profileImage=dataSnapshot.child("ProfileImage").getValue().toString();
                    holder.tvUserName.setText(profileName);
                    Picasso.get().load(profileImage).into(holder.ivProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.tvComment.setText(mCommentList.get(position).getComment());
        holder.tvComment.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);

        holder.tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleActions.intentToProfile(context, mCommentList.get(position).getCommenterId());
            }
        });
        holder.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleActions.intentToProfile(context, mCommentList.get(position).getCommenterId());
            }
        });
        DatabaseReference replyRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts")
                .child(mCommentList.get(position).getPostId()).child("Comments")
                .child(mCommentList.get(position).getCommentId()).child("Replies");
        holder.setRplyCount(replyRef);


        if (mCommentList.get(position).getCommenterId().equals(Utils.createRandomId()) && mCommentList.get(position).getParentUserId().equals(Utils.getCurrentUser())) {
            holder.tvEditComment.setVisibility(View.VISIBLE);
            holder.tvDeleteComment.setVisibility(View.VISIBLE);
        }else if (mCommentList.get(position).getCommenterId().equals(Utils.getCurrentUser())){
            holder.tvEditComment.setVisibility(View.VISIBLE);
            holder.tvDeleteComment.setVisibility(View.VISIBLE);
        }else if (mCommentList.get(position).getParentUserId().equals(Utils.getCurrentUser())){
            holder.tvDeleteComment.setVisibility(View.VISIBLE);
        }

        holder.tvEditComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(mCommentList.get(position).getPostId()).child("Comments").child(mCommentList.get(position).getCommentId());

                        editComment(context,mCommentList.get(position).getComment(),commentRef);

                        Intent intent=new Intent(context,ViewSinglePostActivity.class);
                        intent.putExtra("REF_KEY",mCommentList.get(position).getPostId());
                        context.startActivity(intent);
                    }
                };
                handler.post(runnable);
         }
        });

        holder.tvDeleteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference commentRef;
                commentRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE)
                        .child("Posts").child(mCommentList.get(position).getPostId()).child("Comments")
                        .child(mCommentList.get(position).getCommentId());

                deleteComment(context,commentRef);
                Intent intent=new Intent(context, ViewSinglePostActivity.class);
                intent.putExtra("REF_KEY",mCommentList.get(position).getPostId());
                context.startActivity(intent);
            }
        });
        holder.tvReplyComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyCommentPopup(context,mCommentList.get(position).getPostId(),mCommentList.get(position).getCommentId(),
                        mCommentList.get(position).getCommenterId(),mCommentList.get(position).getParentUserId());
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle=new Bundle();
                bundle.putString("PostId",mCommentList.get(position).getPostId());
                bundle.putString("CommentId",mCommentList.get(position).getCommentId());
                bundle.putString("Comment",mCommentList.get(position).getComment());
                bundle.putString("CommenterId",mCommentList.get(position).getCommenterId());
                bundle.putString("ProfileName",profileName);
                bundle.putString("ProfileImage",profileImage);
                bundle.putString("ParentUserId",mCommentList.get(position).getParentUserId());

                Intent intent=new Intent(context, ViewCommentActivity.class);
                intent.putExtra("Comment",bundle);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        View view;

        TextView tvUserName, tvComment, tvEditComment, tvDeleteComment,tvReplyComment,tvReplyCount;
        ImageView ivProfileImage;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            tvUserName = itemView.findViewById(R.id.tv_display_name);
            tvComment = itemView.findViewById(R.id.tv_comment);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            tvDeleteComment = itemView.findViewById(R.id.tv_delete_comment);
            tvEditComment = itemView.findViewById(R.id.tv_edit_comment);
            tvReplyComment = itemView.findViewById(R.id.tv_reply_comment);
            tvReplyCount = itemView.findViewById(R.id.tv_reply_count);
        }

        public void setRplyCount(DatabaseReference replyRef) {
            replyRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if (dataSnapshot.exists()){
                       long count = dataSnapshot.getChildrenCount();
                       String ct = String.valueOf(count);
                       tvReplyCount.setVisibility(View.VISIBLE);
                       tvReplyCount.setText(ct + " Replies");
                   }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }
}
