package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.adapters.PostAdapter;
import com.nexustech.comicfire.adapters.UserPostsAdapter;
import com.nexustech.comicfire.domains.UserPosts;

import java.util.ArrayList;
import java.util.List;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class UsersPostsActivity extends AppCompatActivity {

    TextView tvCreateNew;
    RecyclerView rvPosts;

    DatabaseReference listCatsRef;

    final int ITEM_LOAD_COUNT = 5;
    int tota_item = 0, last_visible_item;

    UserPostsAdapter catagoryAdapter;
    boolean isLoading = false, isMaxData = false;
    String last_node = "", last_key = "";
    String userId;

    DatabaseReference postRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_posts);

        rvPosts = findViewById(R.id.rv_posts);
        userId=getIntent().getStringExtra("UserId");

        postRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("User").child(userId).child("MyPosts");
        getLastItem();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(UsersPostsActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        // Set the layout manager to your recyclerview

        rvPosts.setLayoutManager(mLayoutManager);


        catagoryAdapter = new UserPostsAdapter(UsersPostsActivity.this);
        rvPosts.setAdapter(catagoryAdapter);
        getCats();

        rvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                tota_item = mLayoutManager.getItemCount();
                last_visible_item = mLayoutManager.findLastCompletelyVisibleItemPosition();

                if (!isLoading && tota_item <= ((last_visible_item + ITEM_LOAD_COUNT))) {
                    getCats();
                    isLoading = true;
                }

            }
        });

        catagoryAdapter.notifyDataSetChanged();
    }



    private void getCats() {

        if (!isMaxData) {
            Query query;
            if (TextUtils.isEmpty(last_node))

                query = postRef
                        .orderByKey()
                        .limitToFirst(ITEM_LOAD_COUNT);

            else

                query = postRef
                        .orderByKey()
                        .startAt(last_node)
                        .limitToFirst(ITEM_LOAD_COUNT);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()) {
                        List<UserPosts> newCats = new ArrayList<>();
                        for (DataSnapshot catSnapShot : snapshot.getChildren()) {
                            newCats.add(catSnapShot.getValue(UserPosts.class));
                        }
                        last_node = newCats.get(newCats.size() - 1).getPostKey();

                        if (!last_node.equals(last_key))
                            newCats.remove(newCats.size() - 1);
                        else
                            last_node = "end";

                        catagoryAdapter.addAll(newCats);
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

        Query getLastKey = postRef
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

                Toast.makeText(UsersPostsActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}