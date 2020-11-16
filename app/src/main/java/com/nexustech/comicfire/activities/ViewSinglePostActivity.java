package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.adapters.CommentAdapter;
import com.nexustech.comicfire.domains.Comments;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class ViewSinglePostActivity extends AppCompatActivity {

    DatabaseReference listCatsRef;
    final int ITEM_LOAD_COUNT = 5;
    int tota_item = 0, last_visible_item;
    CommentAdapter commentAdapter;
    boolean isLoading = false, isMaxData = false;
    String last_node = "", last_key = "";
    DatabaseReference commentRef;


    EditText etComment;
    TextView tvPosttext, tvHeading, tvViews, tvProfileName, tvCommentCount;
    ImageView ivPostImage, ivProfileImage, ivSendComment;
    String views, postText, postImage, postId, userId, currentUserId, profileName, profileImage;
    RecyclerView rvComments;

    DatabaseReference postRef, userRef;
    FirebaseAuth cfAuth;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_post);
        Utils.setTopBar(getWindow(), getResources());

        tvPosttext = findViewById(R.id.tv_post_text);
        ivPostImage = findViewById(R.id.ivPostImage);
        tvHeading = findViewById(R.id.tvHeading);
        tvViews = findViewById(R.id.tvLikeCount);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvCommentCount = findViewById(R.id.tvCommentCount);
        etComment = findViewById(R.id.et_comment);
        ivSendComment = findViewById(R.id.iv_send);
        rvComments = findViewById(R.id.rv_comments);
        ivProfileImage = findViewById(R.id.ivProfile);

        //postText=getIntent().getStringExtra("postText");
        //postImage=getIntent().getStringExtra("postImage");
        postId = getIntent().getStringExtra("REF_KEY");

        cfAuth = FirebaseAuth.getInstance();
        currentUserId = cfAuth.getCurrentUser().getUid();
        postRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts").child(postId);
        userRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User");

        postRef.child("Views").child(currentUserId).child("UserId").setValue(currentUserId);
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    postText = dataSnapshot.child("PostText").getValue().toString();
                    Object postImageUrl = dataSnapshot.child("PostImage").getValue();
                    tvPosttext.setText(postText);
                    userId = dataSnapshot.child("UserId").getValue().toString();

                    // if (dataSnapshot.hasChild("PostImage"))
                    if (postImageUrl == null) {
                        ivPostImage.setVisibility(View.GONE);
                        int counted = postText.length();
                        if (counted < 100) {
                            tvPosttext.setTextSize(35);
                            tvPosttext.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            tvPosttext.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                        }

                    } else {
                        postImage = postImageUrl.toString();
                        Picasso.get().load(postImage).into(ivPostImage);
                    }
                    showuserDetails(userId);
                    showViews();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ivSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String commentText = etComment.getText().toString();
                if (TextUtils.isEmpty(commentText)) {
                    Toast.makeText(ViewSinglePostActivity.this, "Just type a word!", Toast.LENGTH_SHORT).show();
                } else {
                    String commentId = Utils.createRandomId();


                    HashMap hashMap = new HashMap();
                    hashMap.put("CommentId", commentId);
                    hashMap.put("CommenterId", currentUserId);
                    hashMap.put("Comment", commentText);
                    hashMap.put("PostId", postId);
                    hashMap.put("HasReply", commentId);
                    hashMap.put("ParentUserId", userId);

                    postRef.child("Comments").child(commentId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ViewSinglePostActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                                etComment.getText().clear();
                              Utils.hideKeyboard(ViewSinglePostActivity.this);
                            }
                        }
                    });
                }
            }
        });


        getLastItem();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(ViewSinglePostActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        // Set the layout manager to your recyclerview

        rvComments.setLayoutManager(mLayoutManager);


        commentAdapter = new CommentAdapter(ViewSinglePostActivity.this);
        rvComments.setAdapter(commentAdapter);
        getComments();

        rvComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                tota_item = mLayoutManager.getItemCount();
                last_visible_item = mLayoutManager.findLastCompletelyVisibleItemPosition();

                if (!isLoading && tota_item <= ((last_visible_item + ITEM_LOAD_COUNT))) {
                    getComments();
                    isLoading = true;
                }

            }
        });

        commentAdapter.notifyDataSetChanged();

    }

    private void getComments() {

        if (!isMaxData) {
            Query query;
            if (TextUtils.isEmpty(last_node))

                query = postRef.child("Comments")
                        .orderByKey()
                        .limitToFirst(ITEM_LOAD_COUNT);

            else

                query = postRef.child("Comments")
                        .orderByKey()
                        .startAt(last_node)
                        .limitToFirst(ITEM_LOAD_COUNT);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()) {
                        List<Comments> newComments = new ArrayList<>();
                        for (DataSnapshot catSnapShot : snapshot.getChildren()) {
                            newComments.add(catSnapShot.getValue(Comments.class));
                        }
                        last_node = newComments.get(newComments.size() - 1).getCommentId();

                        if (!last_node.equals(last_key))
                            newComments.remove(newComments.size() - 1);
                        else
                            last_node = "end";

                        commentAdapter.addAll(newComments);
                        isLoading = false;

                    } else {
                        isLoading = false;
                        isMaxData = true;
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    isLoading = false;

                }
            });
        }
    }

    private void getLastItem() {

        Query getLastKey = postRef.child("Comments")
                .orderByKey()
                .limitToLast(1);
        getLastKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot lastKey : snapshot.getChildren())
                    last_key = lastKey.getKey();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(ViewSinglePostActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showViews() {
        postRef.child("Views").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long count = dataSnapshot.getChildrenCount();
                    String ct = String.valueOf(count);
                    tvViews.setText(ct + " Views");

                } else {
                    tvViews.setText(0 + " Views");
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
                    tvCommentCount.setText("Comments " + ct);

                } else {
                    tvCommentCount.setText("Comments 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showuserDetails(String userId) {
        userRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    profileName = dataSnapshot.child("DisplayName").getValue().toString();
                    profileImage = dataSnapshot.child("ProfileImage").getValue().toString();
                    tvProfileName.setText(profileName);
                    Picasso.get().load(profileImage).into(ivProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Utils.openAnotherActivity(this, BottomBarActivity.class);
    }
}