package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextClock;
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
import com.nexustech.comicfire.domains.Memes;
import com.nexustech.comicfire.domains.QuizDetails;
import com.nexustech.comicfire.utils.SwipeListener;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.HandleActions.updatePoints;

public class QuizCompetitionActivity extends AppCompatActivity {

    private DatabaseReference cfQuizRef, cfQuizDetailsRef;
    ImageView ivCoverImage;
    TextView  tvTitle;
    String quizId;
    String answer, correctAnswer,imageUrl,title;
    int position, points;
    TextView tvQuestion, tvNext, tvResult;
    // ImageView memeImage;
    // Date createdDate, expireDate;
    RadioGroup radioGroup;
    RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    ConstraintLayout constraintLayout;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_competition);
        Utils.setTopBar(getWindow(), getResources());
        ivCoverImage = findViewById(R.id.iv_cover);
        tvTitle = findViewById(R.id.tv_title);
        tvQuestion = findViewById(R.id.tv_question);
        tvNext = findViewById(R.id.tv_next);
        tvResult = findViewById(R.id.tv_result);
        rbOption1 = findViewById(R.id.rb_option1);
        rbOption2 = findViewById(R.id.rb_option2);
        rbOption3 = findViewById(R.id.rb_option3);
        rbOption4 = findViewById(R.id.rb_option4);
        constraintLayout = findViewById(R.id.con_layout);
        radioGroup = findViewById(R.id.radio);

        // rvQuiz.setHasFixedSize(true);
        // LinearLayoutManager horizontalYalayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        //rvQuiz.setLayoutManager(horizontalYalayoutManager);
        // Set the layout manager to your recyclerview

        quizId = getIntent().getStringExtra("REF_KEY");
        position = getIntent().getIntExtra("POSITION", 0);
        points = getIntent().getIntExtra("POINTS", 0);
        imageUrl=getIntent().getStringExtra("IMAGE");
        title=getIntent().getStringExtra("TITLE");

        tvTitle.setText(title);
        Picasso.get().load(imageUrl).into(ivCoverImage);
        if (position > 10) {
            Toast.makeText(this, "Open Popup for points", Toast.LENGTH_SHORT).show();
            radioGroup.setVisibility(View.INVISIBLE);
            tvNext.setVisibility(View.INVISIBLE);
            tvResult.setVisibility(View.INVISIBLE);
            openPopup();
        } else {
            cfQuizRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Quiz").child(quizId).child(String.valueOf(position));
            cfQuizDetailsRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("QuizDetails").child(quizId).child("Contestants");

            //showAllMemes();

            cfQuizRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        rbOption1.setText(dataSnapshot.child("Option1").getValue().toString());
                        rbOption2.setText(dataSnapshot.child("Option2").getValue().toString());
                        rbOption3.setText(dataSnapshot.child("Option3").getValue().toString());
                        rbOption4.setText(dataSnapshot.child("Option4").getValue().toString());
                        tvQuestion.setText(dataSnapshot.child("Question").getValue().toString());
                        correctAnswer = dataSnapshot.child("Answer").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tvQuestion.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
            }


            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    switch (checkedId) {
                        case R.id.rb_option1:
                            answer = rbOption1.getText().toString();
                            break;
                        case R.id.rb_option2:
                            answer = rbOption2.getText().toString();
                            break;
                        case R.id.rb_option3:
                            answer = rbOption3.getText().toString();
                            break;
                        case R.id.rb_option4:
                            answer = rbOption4.getText().toString();
                            break;
                    }

                }
            });
            constraintLayout.setOnTouchListener(new SwipeListener(this) {
                public void onSwipeLeft() {
                    nextPage();
                }
            });

            tvResult.setText("Result : " + points + "/10");

            tvNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextPage();
                }
            });
        }

    }

    private void openPopup() {
        updatePoints(getPoints(points),Utils.getCurrentUser());
        View rowView = LayoutInflater.from(QuizCompetitionActivity.this).inflate(R.layout.alert_dialog_quiz_result, null);
        AlertDialog dialog = Utils.configDialog(QuizCompetitionActivity.this, rowView);
        TextView tvResult=rowView.findViewById(R.id.tv_result);
        TextView tvPoints=rowView.findViewById(R.id.tv_points);
        TextView tvClose=rowView.findViewById(R.id.tv_close);
        tvResult.setText("Your result is "+points+"/10");
        tvPoints.setText("You got "+getPoints(points)+" points");
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Utils.openAnotherActivity(QuizCompetitionActivity.this,BottomBarActivity.class);
            }
        });
        dialog.show();
    }

    public int getPoints(int points){
        int newPoints;
       if (points>=7){
           newPoints= points*15;
       }else if (points>=4){
           newPoints = points*10;
       }else if (points>=1){
           newPoints=points*5;
       }else {
           newPoints=2;
       }
       return newPoints;
    }
    public void nextPage() {
        if (TextUtils.isEmpty(answer)) {
            Toast.makeText(this, "Please select one answer", Toast.LENGTH_SHORT).show();
        } else {
            if (answer.equals(correctAnswer)) {
                points++;
            }
            if (position == 10) {
                int total = points * 10;
                String totalPoints=String.valueOf(total);
                String finalPoints;
                if (totalPoints.length()==2){
                    finalPoints="0"+totalPoints;
                }else if (totalPoints.length()==1){
                   finalPoints="00"+totalPoints;
                }else {
                    finalPoints=totalPoints;
                }
                HashMap hashMap = new HashMap();
                hashMap.put("UserId", Utils.getCurrentUser());
                hashMap.put("Points", finalPoints);
                cfQuizDetailsRef.child(Utils.getCurrentUser()).updateChildren(hashMap);
            }

            Intent intent = new Intent(QuizCompetitionActivity.this, QuizCompetitionActivity.class);
            intent.putExtra("REF_KEY", quizId);
            intent.putExtra("POSITION", position + 1);
            intent.putExtra("POINTS", points);
            intent.putExtra("IMAGE", imageUrl);
            intent.putExtra("TITLE", title);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
        }
    }
}