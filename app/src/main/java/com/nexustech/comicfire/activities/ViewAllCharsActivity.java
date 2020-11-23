package com.nexustech.comicfire.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.domains.Characters;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class ViewAllCharsActivity extends AppCompatActivity {

    RecyclerView rvChars;
    DatabaseReference cfCharRef;

    String userId;
    int points;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_chars);
        Utils.setTopBar(getWindow(), getResources());

        rvChars = findViewById(R.id.rv_list_chars);

        rvChars.setHasFixedSize(true);
        // rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager = new GridLayoutManager(this, 2);
        cfCharRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Characters");

        // Set the layout manager to your recyclerview
        rvChars.setLayoutManager(mLayoutManager);
        userId = Utils.getCurrentUser();
        points = getIntent().getIntExtra("Points", 0);
        showAllChars();
    }

    private void showAllChars() {
        Query searchPeopleAndFriendsQuery = cfCharRef.orderByChild("Priority");
        FirebaseRecyclerAdapter<Characters, CharacterViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Characters, CharacterViewHolder>(
                        Characters.class,
                        R.layout.layout_character,
                        CharacterViewHolder.class,
                        searchPeopleAndFriendsQuery

                ) {
                    @Override
                    protected void populateViewHolder(CharacterViewHolder postViewHolder, Characters model, int position) {
                        String postKey = getRef(position).getKey();
                        postViewHolder.setCharImage(model.getCharImage());
                        postViewHolder.setCharacterProfile(model.getCharacterProfile());
                        postViewHolder.setCharacterName(model.getCharacterName());
                        postViewHolder.setRequiredPoints(model.getRequiredPoints());
                        postViewHolder.setLockIcon(points, model.getRequiredPoints());
                        postViewHolder.ivLock.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(ViewAllCharsActivity.this, "You need " + model.getRequiredPoints() + " Points", Toast.LENGTH_SHORT).show();
                            }
                        });

                        postViewHolder.tvSelect.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                postViewHolder.setCharacter(model.getCharacterName(), model.getCharacterProfile(), model.getCharImage());
                            }
                        });

                    }
                };

        rvChars.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CharacterViewHolder extends RecyclerView.ViewHolder {
        View cfView;
        TextView tvCharName, tvSelect,tvShade;
        ImageView ivCharProfImage, ivCharImage, ivLock;
        private FirebaseAuth cfAuth;
        private String currentUserId;
        DatabaseReference checkReqSent, userRef;


        public CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;
            ivCharImage = cfView.findViewById(R.id.iv_char_image);
            ivCharProfImage = cfView.findViewById(R.id.iv_char_profile);
            tvCharName = cfView.findViewById(R.id.tv_character_name);
            ivLock = cfView.findViewById(R.id.iv_char_lock);
            tvSelect = cfView.findViewById(R.id.tv_select_character);
            tvShade=cfView.findViewById(R.id.tv_shade);


            cfAuth = FirebaseAuth.getInstance();
            currentUserId = cfAuth.getCurrentUser().getUid();
        }

        public void setCharacterName(String CharacterName) {
            tvCharName.setText(CharacterName);

        }

        public void setCharImage(String CharImage) {
            Picasso.get().load(CharImage).into(ivCharImage);
        }

        public void setRequiredPoints(String postText) {


        }

        public void setCharacterProfile(String charProfile) {
            Picasso.get().load(charProfile).into(ivCharProfImage);
        }

        public void setLockIcon(int points, String requiredPoints) {
            int reqPoint = Integer.parseInt(requiredPoints);
            if (reqPoint < points) {
                ivLock.setVisibility(View.INVISIBLE);
                tvShade.setVisibility(View.INVISIBLE);
            } else {
                tvSelect.setEnabled(false);

                tvSelect.setVisibility(View.INVISIBLE);

            }
        }

        public void setCharacter(String characterName, String characterProfile, String charImage) {
            userRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(currentUserId);
            userRef.child("CharacterName").setValue(characterName);
            userRef.child("ProfileImage").setValue(characterProfile);
            userRef.child("CharacterImage").setValue(charImage);
        }
    }
}
