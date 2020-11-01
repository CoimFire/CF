package com.nexustech.comicfire.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.domains.Users;
import com.nexustech.comicfire.utils.HandleActions;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class FriendsFragment extends Fragment {
    private TextView tvHeading;
    private ImageView ivSearch;
    private EditText etSearchText;
    private RecyclerView rvSearchedFriendsList;
    private RecyclerView rvAllUser;
    private DatabaseReference cfFindFriendsRef;
    private FirebaseAuth cfAuth;
    private String curUserId;

    public static DatabaseReference cfFollowingRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(Utils.getCurrentUser()).child("Followings");
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_friends, container, false);


        ivSearch = rootView.findViewById(R.id.ivSearchFriends);
        etSearchText = rootView.findViewById(R.id.etSearchFriends);
        rvSearchedFriendsList = rootView.findViewById(R.id.rvSearchFriends);
        curUserId = Utils.getCurrentUser();

        cfFindFriendsRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User");
        rvSearchedFriendsList.setHasFixedSize(true);
        rvSearchedFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        SearchPeopleAndFriends("");
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etSearchText.getText().toString();
                SearchPeopleAndFriends(text);
            }
        });
        return rootView;
    }

    private void SearchPeopleAndFriends(String searchInput) {
        Query searchPeopleAndFriendsQuery = cfFindFriendsRef.orderByChild("DisplayName")
                .startAt(searchInput)
                .endAt(searchInput + "\uf8ff");

        FirebaseRecyclerAdapter<Users, UserViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                        Users.class,
                        R.layout.layout_find_friends,
                        UserViewHolder.class,
                        searchPeopleAndFriendsQuery

                ) {
                    @Override
                    protected void populateViewHolder(FriendsFragment.UserViewHolder findFriendsViewHolder, Users model, int position) {

                        findFriendsViewHolder.setDisplayName(model.getDisplayName());
                        findFriendsViewHolder.setCharacterName(model.getCharacterName());
                        findFriendsViewHolder.setProfileImage(model.getProfileImage());
                        String searchedUserId = getRef(position).getKey();

                        HandleActions.manageRequestButton(searchedUserId,findFriendsViewHolder.tvRequest);
                        findFriendsViewHolder.tvRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HandleActions.actionHandler(getContext(),searchedUserId);

                            }
                        });

                        findFriendsViewHolder.profImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HandleActions.intentToProfile(getContext(), searchedUserId);
                            }
                        });

                        findFriendsViewHolder.profName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HandleActions.intentToProfile(getContext(), searchedUserId);

                            }
                        });
                        findFriendsViewHolder.cfView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HandleActions.intentToProfile(getContext(), searchedUserId);
                            }
                        });

                    }
                };
        rvSearchedFriendsList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        View cfView;
        TextView tvRequest, profName, characterName;
        //  private String CURRENT_STATE;
        private FirebaseAuth cfAuth;
        private String currentUserId;
        DatabaseReference cfFollowerRef;
     //   DatabaseReference cfFollowingRef;
        ImageView profImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;
            tvRequest = cfView.findViewById(R.id.tv_request);
            cfAuth = FirebaseAuth.getInstance();
            currentUserId = cfAuth.getCurrentUser().getUid();
            cfFollowerRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(currentUserId).child("Followers");
          //  cfFollowingRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(currentUserId).child("Followings");
            profImage = cfView.findViewById(R.id.iv_profile);
            profName = cfView.findViewById(R.id.tv_user_name);
            characterName = cfView.findViewById(R.id.tv_character_name);
        }

        public void setProfileImage(String ProfileImage) {
            Picasso.get().load(ProfileImage).into(profImage);
        }

        public void setDisplayName(String DisplayName) {
            profName.setText(DisplayName);
        }

        public void setCharacterName(String CharacterName) {
            characterName.setText(CharacterName);
        }



    }
}