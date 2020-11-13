package com.nexustech.comicfire.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.domains.Comments;
import com.nexustech.comicfire.utils.HandleActions;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.HandleActions.deletComment;
import static com.nexustech.comicfire.utils.HandleActions.editComment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {


    List<Comments> mCommentList;
    Context context;


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

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CommentViewHolder holder, int position) {
        String userId = mCommentList.get(position).getCommenterId();
        FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    holder.tvUserName.setText(dataSnapshot.child("DisplayName").getValue().toString());
                    Picasso.get().load(dataSnapshot.child("ProfileImage").getValue().toString()).into(holder.ivProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.tvComment.setText(mCommentList.get(position).getComment());
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
                        editComment(context,mCommentList.get(position).getPostId(),mCommentList.get(position).getCommentId(),mCommentList.get(position).getComment());
                    }
                };
                handler.post(runnable);
         }
        });

        holder.tvDeleteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletComment(context,mCommentList.get(position).getPostId(),mCommentList.get(position).getCommentId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        View view;

        TextView tvUserName, tvComment, tvEditComment, tvDeleteComment;
        ImageView ivProfileImage;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            tvUserName = itemView.findViewById(R.id.tv_display_name);
            tvComment = itemView.findViewById(R.id.tv_comment);
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            tvDeleteComment = itemView.findViewById(R.id.tv_delete_comment);
            tvEditComment = itemView.findViewById(R.id.tv_edit_comment);
        }
    }
}
