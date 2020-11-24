package com.nexustech.comicfire.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.domains.Characters;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.HandleActions.reducePoints;

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
        Utils.setTopBar(this,getWindow(), getResources());

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
                        postViewHolder.setLockIcon(ViewAllCharsActivity.this, points, model.getRequiredPoints(), model.getCharacterName());
                        postViewHolder.ivLock.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(ViewAllCharsActivity.this, "You need " + model.getRequiredPoints() + " Points", Toast.LENGTH_SHORT).show();
                            }
                        });

                        postViewHolder.cfView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                postViewHolder.openPopupForCharacter(ViewAllCharsActivity.this,
                                        model.getAbilities(),
                                        model.getRole(),
                                        model.getRace(),
                                        model.getFranchise(),
                                        model.getCharacterName(),
                                        model.getRequiredPoints(),
                                        model.getCharacterProfile(),
                                        model.getCharImage()
                                );
                            }
                        });

                    }
                };

        rvChars.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CharacterViewHolder extends RecyclerView.ViewHolder {
        View cfView;
        TextView tvCharName, tvSelect, tvShade, tvAbiities, tvRole, tvRace, tvFranchise;
        ImageView ivCharProfImage, ivCharImage, ivLock;
        private String currentUserId;
        DatabaseReference checkReqSent, userRef;
        String state;

        public CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;
            ivCharImage = cfView.findViewById(R.id.iv_char_image);
            ivCharProfImage = cfView.findViewById(R.id.iv_char_profile);
            tvCharName = cfView.findViewById(R.id.tv_character_name);
            ivLock = cfView.findViewById(R.id.iv_char_lock);
            tvSelect = cfView.findViewById(R.id.tv_select_character);
            tvShade = cfView.findViewById(R.id.tv_shade);


            currentUserId = Utils.getCurrentUser();

            userRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(currentUserId);
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

        public void setLockIcon(Context context, int points, String requiredPoints, String characterName) {
            int reqPoint = Integer.parseInt(requiredPoints);
            if (reqPoint <= points) {
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("MyCharacters").exists()) {
                            String myName = dataSnapshot.child("CharacterName").getValue().toString();
                            if (dataSnapshot.child("MyCharacters").hasChild(characterName)) {
                                if (myName.equals(characterName)) {
                                    tvSelect.setText("SELECTED");
                                    tvSelect.setTextColor(context.getResources().getColor(R.color.black));
                                } else {
                                    tvSelect.setText("SELECT");
                                    tvSelect.setTextColor(context.getResources().getColor(R.color.greenColor));
                                }
                            }
                        } else {
                            tvSelect.setText("UNLOCK");
                            tvSelect.setTextColor(context.getResources().getColor(R.color.greenColor));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                ivLock.setVisibility(View.INVISIBLE);
                tvShade.setVisibility(View.INVISIBLE);

            } else {
                tvSelect.setEnabled(false);

                tvSelect.setVisibility(View.INVISIBLE);

            }
        }


        private void openPopupForCharacter(Context context, String abilities, String role, String race, String franchise, String charName,
                                           String reqPoints, String profileImage, String characterImage) {
            View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_view_character, null);
            AlertDialog dialog = Utils.configDialog(context, rowView);
            ImageView ivProfileImage = rowView.findViewById(R.id.ivMyProfile);

            ImageView ivCharacterImage = rowView.findViewById(R.id.ivMyCharacter);
            TextView tvCharacterName = rowView.findViewById(R.id.tvMyCharacter);
            TextView tvRequest = rowView.findViewById(R.id.tv_request);
            TextView tvCancel = rowView.findViewById(R.id.tv_cancel);
            TextView tvRequiredPoints = rowView.findViewById(R.id.tv_required_points);
            tvAbiities = rowView.findViewById(R.id.tv_abilities);
            tvRole = rowView.findViewById(R.id.tv_role);
            tvRace = rowView.findViewById(R.id.tv_race);
            tvFranchise = rowView.findViewById(R.id.tv_franchise);

            tvAbiities.setText(abilities);
            tvRole.setText(role);
            tvRace.setText(race);
            tvFranchise.setText(franchise);
            tvCharacterName.setText(charName);
            Picasso.get().load(profileImage).into(ivProfileImage);
            Picasso.get().load(characterImage).into(ivCharacterImage);
            state = tvSelect.getText().toString().toUpperCase();

            if (state.equals("SELECTED")) {
                tvRequest.setVisibility(View.INVISIBLE);
                tvRequiredPoints.setText("This is your current selected character");

            } else if (state.equals("SELECT")) {

                tvRequiredPoints.setText("You have aldready owned this character");
                tvRequest.setText("SELECT");
                tvRequest.setTextColor(context.getResources().getColor(R.color.greenColor));

            } else if (state.equals("UNLOCK")) {
                tvRequiredPoints.setText("You can unlock for " + reqPoints + " points.");
                tvRequest.setText("UNLOCK");
                tvRequest.setTextColor(context.getResources().getColor(R.color.greenColor));
            } else {

                tvRequiredPoints.setText("You need " + reqPoints + " points to unlock this character");
                tvRequest.setVisibility(View.INVISIBLE);
                tvRequest.setTextColor(context.getResources().getColor(R.color.light_grey));
            }

            tvRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (state.equals("SELECT")) {
                        userRef.child("CharacterName").setValue(charName);
                        userRef.child("ProfileImage").setValue(profileImage);
                        userRef.child("CharacterImage").setValue(characterImage);
                        Toast.makeText(context, "Selected", Toast.LENGTH_SHORT).show();
                        state = "SELECTED";
                        dialog.dismiss();

                    } else if (state.equals("UNLOCK")) {
                        userRef.child("MyCharacters").child(charName)
                                .child("CharacterName").setValue(charName);
                        Toast.makeText(context, "Unlocked", Toast.LENGTH_SHORT).show();
                        state = "SELECT";
                        reducePoints(Integer.parseInt(reqPoints),Utils.getCurrentUser());

                        dialog.dismiss();
                    } else {
                        tvRequest.setText("Need more points");
                        Toast.makeText(context, "Get more points", Toast.LENGTH_SHORT).show();
                    }

                    dialog.dismiss();

                    Utils.goToAllCharActivity(context);
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


    }
}
