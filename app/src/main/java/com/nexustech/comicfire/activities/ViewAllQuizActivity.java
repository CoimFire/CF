package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.adapters.PostAdapter;
import com.nexustech.comicfire.adapters.QuizAdapter;
import com.nexustech.comicfire.domains.Posts;
import com.nexustech.comicfire.domains.Quiz;
import com.nexustech.comicfire.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class ViewAllQuizActivity extends AppCompatActivity {


    private DatabaseReference cfQuizRef;
    private RecyclerView rvQuiz;

    final int ITEM_LOAD_COUNT = 5;
    int tota_item = 0, last_visible_item;

    QuizAdapter quizAdapter;
    boolean isLoading = false, isMaxData = false;
    String last_node = "", last_key = "";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_quiz);
        Utils.setTopBar(getWindow(), getResources());

        rvQuiz = findViewById(R.id.rv_quiz);


        cfQuizRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("QuizDetails");
        getLastItem();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        // Set the layout manager to your recyclerview

        rvQuiz.setLayoutManager(mLayoutManager);


        quizAdapter = new QuizAdapter(this, this);
        rvQuiz.setAdapter(quizAdapter);

        showAllQuiz();


        rvQuiz.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                tota_item = mLayoutManager.getItemCount();
                last_visible_item = mLayoutManager.findLastCompletelyVisibleItemPosition();

                if (!isLoading && tota_item <= ((last_visible_item + ITEM_LOAD_COUNT))) {
                    showAllQuiz();
                    isLoading = true;
                }

            }
        });

        quizAdapter.notifyDataSetChanged();

    }


    private void showAllQuiz() {

        if (!isMaxData) {
            Query query;
            if (TextUtils.isEmpty(last_node))

                query = cfQuizRef
                        .orderByKey()
                        .limitToFirst(ITEM_LOAD_COUNT);

            else

                query = cfQuizRef
                        .orderByKey()
                        .startAt(last_node)
                        .limitToFirst(ITEM_LOAD_COUNT);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()) {
                        List<Quiz> newCats = new ArrayList<>();
                        for (DataSnapshot catSnapShot : snapshot.getChildren()) {
                            newCats.add(catSnapShot.getValue(Quiz.class));
                        }
                        last_node = newCats.get(newCats.size() - 1).getQuizName();

                        if (!last_node.equals(last_key))
                            newCats.remove(newCats.size() - 1);
                        else
                            last_node = "end";

                        quizAdapter.addAll(newCats);
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

        Query getLastKey = cfQuizRef
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

                Toast.makeText(ViewAllQuizActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}