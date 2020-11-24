package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
import com.nexustech.comicfire.domains.Contestants;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.Utils.showEmpty;

public class QuizDetailsActivity extends AppCompatActivity {
    private String coverImageUrl, parentkey, postText, curuserId;
    ImageView coverImage;
    TextView accept, tvTitle;
    RecyclerView rvChildList;
    DatabaseReference cfChildernMemes, cfPostRef, cfUserRef;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_details);

        Utils.setTopBar(this,getWindow(), getResources());
        coverImageUrl = getIntent().getStringExtra("CoverImage");
        parentkey = getIntent().getStringExtra("QuizId");
        postText = getIntent().getStringExtra("Title");
        cfPostRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("QuizDetails");
        cfChildernMemes = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("QuizDetails").child(parentkey).child("Contestants");
        cfUserRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User");
        curuserId = Utils.getCurrentUser();
        coverImage = findViewById(R.id.iv_cover);
        accept = findViewById(R.id.tv_accept);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(postText);
        Picasso.get().load(coverImageUrl).into(coverImage);
        rvChildList = findViewById(R.id.rv_lead_board);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               cfChildernMemes.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if (dataSnapshot.hasChild(Utils.getCurrentUser())){
                           Toast.makeText(QuizDetailsActivity.this, "Sorry! You have already participated!", Toast.LENGTH_LONG).show();
                       }else {
                           openConfirmPopup();
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {

                   }
               });


            }
        });
        // rvChildList.setHasFixedSize(true);
        // rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(QuizDetailsActivity.this);
        rvChildList.setHasFixedSize(true);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        // Set the layout manager to your recyclerview
        rvChildList.setLayoutManager(mLayoutManager);

        showEmpty(getWindow().getDecorView().getRootView(),cfChildernMemes);
        showChildMemes();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void openConfirmPopup() {
        View rowView = LayoutInflater.from(QuizDetailsActivity.this).inflate(R.layout.alert_dialog_general, null);
        AlertDialog dialog = Utils.configDialog(QuizDetailsActivity.this, rowView);
        TextView tvTitle = rowView.findViewById(R.id.tv_title);
        TextView tvMessage = rowView.findViewById(R.id.tv_message);
        TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = rowView.findViewById(R.id.tv_confirm);
        tvTitle.setText("Rules");
        tvConfirm.setText("Continue");
        tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        tvMessage.setText(
                "1. You have got only one chance to participate in rhis quiz.\n\n" +
                        "2. User will get points for their responsible result");
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(QuizDetailsActivity.this, QuizCompetitionActivity.class);
                intent.putExtra("REF_KEY", parentkey);
                intent.putExtra("POSITION", 1);
                intent.putExtra("POINTS", 0);
                intent.putExtra("IMAGE", coverImageUrl);
                intent.putExtra("TITLE", postText);
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

    private void showChildMemes() {
        Query postQuery = cfChildernMemes.orderByKey();
        FirebaseRecyclerAdapter<Contestants, PointsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Contestants, PointsViewHolder>(
                        Contestants.class,
                        R.layout.layout_find_friends,
                        PointsViewHolder.class,
                        postQuery

                ) {
                    @Override
                    protected void populateViewHolder(PointsViewHolder postViewHolder, Contestants model, int position) {
                        String postKey = getRef(position).getKey();
                        postViewHolder.tvPoints.setText(""+Integer.parseInt(model.getPoints()));
                        cfUserRef.child(model.getUserId()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    postViewHolder.setDisplayName(dataSnapshot.child("DisplayName").getValue().toString());
                                    postViewHolder.setProfileImage(dataSnapshot.child("ProfileImage").getValue().toString());
                                    postViewHolder.setCharacterName(dataSnapshot.child("CharacterName").getValue().toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                };

        rvChildList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PointsViewHolder extends RecyclerView.ViewHolder {
        View cfView;
        TextView tvProfName, tvCharacterName, tvPoints;
        ImageView ivProfImage;
        private String currentUserId, postId;
        DatabaseReference checkReqSent, likeRef, cfMemeRef;


        public PointsViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;
            ivProfImage = cfView.findViewById(R.id.iv_profile);
            tvProfName = cfView.findViewById(R.id.tv_user_name);
            tvPoints = cfView.findViewById(R.id.tv_request);
            tvCharacterName = cfView.findViewById(R.id.tv_character_name);

        }

        public void setProfileImage(String ProfileImage) {
            Picasso.get().load(ProfileImage).into(ivProfImage);
        }

        public void setDisplayName(String DisplayName) {
            tvProfName.setText(DisplayName);
        }

        public void setCharacterName(String postText) {
            tvCharacterName.setText(postText);
        }

    }
}