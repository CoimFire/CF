package com.nexustech.comicfire.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.activities.QuizCompetitionActivity;
import com.nexustech.comicfire.activities.QuizDetailsActivity;
import com.nexustech.comicfire.domains.Posts;
import com.nexustech.comicfire.domains.Quiz;
import com.nexustech.comicfire.utils.HandleActions;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.HandleActions.deletComment;
import static com.nexustech.comicfire.utils.HandleActions.editComment;
import static com.nexustech.comicfire.utils.Utils.openAnotherActivity;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder>{
    List<Quiz> mQuizList;
    Context context;
    String[] menu;
    Activity activity;

    public QuizAdapter(Context context, Activity activity) {
        this.mQuizList = new ArrayList<>();
        this.context = context;
        this.activity = activity;

    }

    public void addAll(List<Quiz> newQuiz) {

        int initSize = newQuiz.size();
        mQuizList.addAll(newQuiz);
        notifyItemRangeChanged(initSize, newQuiz.size());

    }

    public String getLastItemId() {
        return mQuizList.get(mQuizList.size() - 1).getQuizName();
    }


    @NonNull
    @Override
    public QuizAdapter.QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_display_meme, parent, false);

        return new QuizAdapter.QuizViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizAdapter.QuizViewHolder holder, int position) {
        holder.tvQuizName.setText(mQuizList.get(position).getQuizName());
        Picasso.get().load(mQuizList.get(position).getQuizImage()).into(holder.ivQuizImage);
        holder.showCounter(mQuizList.get(position).getQuizName(),mQuizList.get(position).getCreatedDate());

        holder.cfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, QuizDetailsActivity.class);
                intent.putExtra("QuizId", mQuizList.get(position).getQuizName());
                intent.putExtra("Title", mQuizList.get(position).getQuizName());
                intent.putExtra("CoverImage", mQuizList.get(position).getQuizImage());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mQuizList.size();
    }

    public class QuizViewHolder extends RecyclerView.ViewHolder {

        View cfView;
        TextView tvQuizName, timer, go, tvTimerLabel;
        ImageView ivQuizImage;
        Date createdDate, expireDate;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;

            tvQuizName = cfView.findViewById(R.id.meme_name);
            ivQuizImage = cfView.findViewById(R.id.meme_cover);
            timer = cfView.findViewById(R.id.timer);
            go = cfView.findViewById(R.id.go);
            tvTimerLabel = cfView.findViewById(R.id.timerlabel);
        }
        public void showCounter(String memeKey, String date1) {

            SimpleDateFormat format = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date2 = format.format(new Date());

            try {
                createdDate = format.parse(date1);
                expireDate = format.parse(date2);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            long createdDateMilli = createdDate.getTime() + 432000000;
            long expireDateMilli = expireDate.getTime();
            long difference = createdDateMilli - expireDateMilli;


            new CountDownTimer(difference, 1000) {
                public void onTick(long millisUntilFinish) {
                    int seconds = (int) (millisUntilFinish / 1000) % 60;
                    int minutes = (int) (millisUntilFinish / (1000 * 60)) % 60;
                    int hours = (int) (millisUntilFinish / (1000 * 60 * 60)) % 24;
                    int days = (int) (millisUntilFinish / (1000 * 60 * 60)) / 24;

                    String ctText = String.format("%02d : %02d : %02d : %02d", days, hours, minutes, seconds);

                    timer.setText(ctText);
                }

                public void onFinish() {
                    timer.setText("Expired");
                    // accept.setVisibility(View.INVISIBLE);
                    //timerText.setVisibility(View.INVISIBLE);
                    //- designStatus.setVisibility(View.VISIBLE);
                }
            }.start();

        }
    }


}
