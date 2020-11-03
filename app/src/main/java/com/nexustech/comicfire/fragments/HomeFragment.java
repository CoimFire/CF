package com.nexustech.comicfire.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.activities.CreateNewPostActivity;
import com.nexustech.comicfire.adapters.PostAdapter;
import com.nexustech.comicfire.domains.Posts;
import com.nexustech.comicfire.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;


public class HomeFragment extends Fragment {

    TextView tvCreateNew;
    RecyclerView rvPosts;

    DatabaseReference listCatsRef;

    final int ITEM_LOAD_COUNT = 5;
    int tota_item = 0, last_visible_item;

    PostAdapter catagoryAdapter;
    boolean isLoading = false, isMaxData = false;
    String last_node = "", last_key = "";

    DatabaseReference postRef;

    @RequiresApi(api = Build.VERSION_CODES.M)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        tvCreateNew = root.findViewById(R.id.tv_create_new);
        tvCreateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.openAnotherActivity(getContext(), CreateNewPostActivity.class);
            }
        });

        postRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Posts");
        rvPosts = root.findViewById(R.id.rv_posts);

        getLastItem();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        // Set the layout manager to your recyclerview

        rvPosts.setLayoutManager(mLayoutManager);


        catagoryAdapter = new PostAdapter(getContext(),getActivity());
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

        return root;
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
                        List<Posts> newCats = new ArrayList<>();
                        for (DataSnapshot catSnapShot : snapshot.getChildren()) {
                            newCats.add(catSnapShot.getValue(Posts.class));
                        }
                        last_node = newCats.get(newCats.size() - 1).getPostId();

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

                Toast.makeText(getContext(), "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

}