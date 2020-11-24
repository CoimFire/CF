package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.domains.ReplyComments;
import com.nexustech.comicfire.utils.HandleActions;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.HandleActions.deleteComment;
import static com.nexustech.comicfire.utils.HandleActions.editComment;

public class ViewCommentActivity extends AppCompatActivity {

    TextView tvUserName, tvComment, tvEditComment, tvDeleteComment;
    ImageView ivProfileImage,ivSend;
    EditText etComment;
    DatabaseReference commentRef;
    String userName,comment,profileImage,postId,commentId,commenterId,parentUserId;
    RecyclerView rvReply;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comment);
        Utils.setTopBar(this,getWindow(),getResources());



        tvUserName = findViewById(R.id.tv_display_name);
        tvComment = findViewById(R.id.tv_comment);
        ivProfileImage = findViewById(R.id.iv_profile_image);
        tvDeleteComment = findViewById(R.id.tv_delete_comment);
        tvEditComment = findViewById(R.id.tv_edit_comment);
        etComment=findViewById(R.id.et_comment);
        ivSend=findViewById(R.id.iv_send);
        rvReply=findViewById(R.id.rv_reply_comments);

        rvReply.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvReply.setLayoutManager(mLayoutManager);

        Bundle bundle=getIntent().getBundleExtra("Comment");
        postId=bundle.getString("PostId");
        commentId=bundle.getString("CommentId");
        comment=bundle.getString("Comment");
        commenterId=bundle.getString("CommenterId");
        parentUserId=bundle.getString("ParentUserId");
        userName=bundle.getString("ProfileName");
        profileImage=bundle.getString("ProfileImage");
        tvUserName.setText(userName);
        tvComment.setText(comment);
        Picasso.get().load(profileImage).into(ivProfileImage);

        tvComment.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);

        if (commenterId.equals(Utils.createRandomId()) && parentUserId.equals(Utils.getCurrentUser())) {
            tvEditComment.setVisibility(View.VISIBLE);
            tvDeleteComment.setVisibility(View.VISIBLE);
        }else if (commenterId.equals(Utils.getCurrentUser())){
            tvEditComment.setVisibility(View.VISIBLE);
            tvDeleteComment.setVisibility(View.VISIBLE);
        }else if (parentUserId.equals(Utils.getCurrentUser())){
            tvDeleteComment.setVisibility(View.VISIBLE);
        }
        commentRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postId).child("Comments").child(commentId);

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = etComment.getText().toString();
                if (TextUtils.isEmpty(commentText)) {
                    Toast.makeText(ViewCommentActivity.this, "Just type a word!", Toast.LENGTH_SHORT).show();
                } else {


                 // String randomId = Utils.createRandomId();
                    String edittedComment=etComment.getText().toString();
                    HandleActions.replyComment(ViewCommentActivity.this,edittedComment,commentId, commenterId,parentUserId, commentRef);
                    Utils.hideKeyboard(ViewCommentActivity.this);
                }
            }
        });
        tvDeleteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteComment(ViewCommentActivity.this,commentRef);
            }
        });
        tvEditComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editComment(ViewCommentActivity.this,comment,commentRef);
            }
        });

        showReplies();
    }

    private void showReplies() {
        Query searchPeopleAndFriendsQuery = commentRef.child("Replies").orderByKey();

        FirebaseRecyclerAdapter<ReplyComments, ReplyViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ReplyComments, ReplyViewHolder>(
                        ReplyComments.class,
                        R.layout.layout_reply_comment,
                        ReplyViewHolder.class,
                        searchPeopleAndFriendsQuery

                ) {
                    @Override
                    protected void populateViewHolder(ReplyViewHolder replyViewHolder, ReplyComments model, int position) {
                        String searchedUserId = getRef(position).getKey();
                        replyViewHolder.setUserDetails(model.getParentCommentUserId(),model.getRepliedUserId(),model.getCommentText());

                    }
                };
        rvReply.setAdapter(firebaseRecyclerAdapter);
    }
    public static class ReplyViewHolder extends RecyclerView.ViewHolder {
        public View cfView;
        ImageView ivProfile;
        TextView tvComment,tvEdit,tvDelete;
        DatabaseReference userRef;

        String name,repliedUserName, parentCommenterName,profileImage;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;

            ivProfile=cfView.findViewById(R.id.iv_profile_image);
            tvEdit=cfView.findViewById(R.id.tv_edit_comment);
            tvDelete=cfView.findViewById(R.id.tv_delete_comment);
            tvComment=cfView.findViewById(R.id.tv_comment);

            userRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User");
        }

        public void setUserDetails(String parentCommentUserId, String repliedUserId, String commentText) {

            userRef.child(parentCommentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        parentCommenterName=dataSnapshot.child("DisplayName").getValue().toString();
                        userRef.child(repliedUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    profileImage=dataSnapshot.child("ProfileImage").getValue().toString();
                                    repliedUserName=dataSnapshot.child("DisplayName").getValue().toString();
                                    Picasso.get().load(profileImage).into(ivProfile);

                                    tvComment.setText(Html.fromHtml("<B>" + " "+repliedUserName+" " + "</B>"+ "<font color=\"#4682B4\">" + "@"+parentCommenterName+" " + "</font>" + commentText));

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

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
}