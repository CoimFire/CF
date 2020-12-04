package com.nexustech.comicfire.activities;

import android.app.AlertDialog;
import android.content.Context;
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
import static com.nexustech.comicfire.utils.HandleActions.curUserId;
import static com.nexustech.comicfire.utils.HandleActions.reducePoints;

public class MyCharactersActivity extends AppCompatActivity {

    RecyclerView rvChars;
    DatabaseReference cfCharRef;

    String userId;
    int points;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_characters);
        Utils.setTopBar(this,getWindow(), getResources());

        rvChars = findViewById(R.id.rv_list_chars);

        rvChars.setHasFixedSize(true);
        // rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager = new GridLayoutManager(this, 2);
        cfCharRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(curUserId).child("MyCharacters");

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
                        postViewHolder.setDetails(MyCharactersActivity.this,postKey,points);

                    }
                };

        rvChars.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CharacterViewHolder extends RecyclerView.ViewHolder {
        View cfView;
        TextView tvCharName, tvSelect, tvShade, tvAbiities, tvRole, tvRace, tvFranchise;
        ImageView ivCharProfImage, ivCharImage, ivLock;
        private String currentUserId;
        DatabaseReference charRef, userRef;
        String state,characterName,characterImage,requiredPoints,characterProfile,abilities, role, race, franchise;

        public CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;
            ivCharImage = cfView.findViewById(R.id.iv_char_image);
            ivCharProfImage = cfView.findViewById(R.id.iv_char_profile);
            tvCharName = cfView.findViewById(R.id.tv_character_name);
            ivLock = cfView.findViewById(R.id.iv_char_lock);
            tvSelect = cfView.findViewById(R.id.tv_select_character);
            tvShade = cfView.findViewById(R.id.tv_shade);

            ivLock.setVisibility(View.INVISIBLE);
            tvShade.setVisibility(View.INVISIBLE);

            currentUserId = Utils.getCurrentUser();

            charRef=FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Characters");
            userRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(currentUserId);
        }


        public void setDetails(Context context,String postKey,int points) {
            charRef.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        characterName=dataSnapshot.child("CharacterName").getValue().toString();
                        characterImage=dataSnapshot.child("CharImage").getValue().toString();
                        characterProfile=dataSnapshot.child("CharacterProfile").getValue().toString();
                        requiredPoints=dataSnapshot.child("RequiredPoints").getValue().toString();
                        abilities=dataSnapshot.child("Abilities").getValue().toString();
                        franchise=dataSnapshot.child("Franchise").getValue().toString();
                        role=dataSnapshot.child("Role").getValue().toString();
                        race=dataSnapshot.child("Race").getValue().toString();

                        tvCharName.setText(characterName);
                        Picasso.get().load(characterProfile).into(ivCharProfImage);
                        Picasso.get().load(characterImage).into(ivCharImage);


                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child("MyCharacters").exists()) {
                                    String myName = dataSnapshot.child("CharacterName").getValue().toString();
                                    if (myName.equals(characterName)) {
                                        tvSelect.setText("SELECTED");
                                        tvSelect.setTextColor(context.getResources().getColor(R.color.black));
                                    } else {
                                        tvSelect.setText("SELECT");
                                        tvSelect.setTextColor(context.getResources().getColor(R.color.greenColor));
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

                        cfView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
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
                                tvCharacterName.setText(characterName);
                                Picasso.get().load(characterProfile).into(ivProfileImage);
                                Picasso.get().load(characterImage).into(ivCharacterImage);
                                state = tvSelect.getText().toString().toUpperCase();

                                if (state.equals("SELECTED")) {
                                    tvRequest.setVisibility(View.INVISIBLE);
                                    tvRequiredPoints.setText("This is your current selected character");

                                } else if (state.equals("SELECT")) {
                                    tvRequiredPoints.setVisibility(View.INVISIBLE);
                                    tvRequest.setText("SELECT");
                                    tvRequest.setTextColor(context.getResources().getColor(R.color.greenColor));

                                }

                                tvRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (state.equals("SELECT")) {
                                            userRef.child("CharacterName").setValue(characterName);
                                            userRef.child("ProfileImage").setValue(characterProfile);
                                            userRef.child("CharacterImage").setValue(characterImage);
                                            Toast.makeText(context, "Selected", Toast.LENGTH_SHORT).show();
                                            state = "SELECTED";
                                            dialog.dismiss();

                                        }

                                        dialog.dismiss();

                                        Utils.goToAllCharActivity(context, MyCharactersActivity.class);
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


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
}
