package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.domains.Memes;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class ViewAllMemesActivity extends AppCompatActivity {

    private DatabaseReference cfMemeCoverRef;
    private RecyclerView rvMeme;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_memes);
        Utils.setTopBar(getWindow(),getResources());

        rvMeme = findViewById(R.id.rv_memes);
        rvMeme.setHasFixedSize(true);
        // rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        // Set the layout manager to your recyclerview
        rvMeme.setLayoutManager(mLayoutManager);

        cfMemeCoverRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Memes");
        showAllMemes();

    }

    private void showAllMemes() {

        Query memeQuery = cfMemeCoverRef.orderByChild("Counter");
        FirebaseRecyclerAdapter<Memes, MemeViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Memes, MemeViewHolder>(
                        Memes.class,
                        R.layout.layout_display_meme,
                        MemeViewHolder.class,
                        memeQuery

                ) {
                    @Override
                    protected void populateViewHolder(MemeViewHolder memeViewHolder, Memes model, int position) {
                        String memeKey = getRef(position).getKey();
                        memeViewHolder.setMemeName(model.getMemeName());
                        memeViewHolder.setCoverImage(model.getMemeImage());
                        String state = model.getState();
                        if ("Finished".equals(state)) {
                            memeViewHolder.timer.setText("Competition Ended");
                            memeViewHolder.tvTimerLabel.setVisibility(View.INVISIBLE);
                            memeViewHolder.go.setVisibility(View.INVISIBLE);

                        } else {
                            memeViewHolder.showCounter(memeKey, model.getCreatedDate());
                        }
                        memeViewHolder.setCount(memeKey);
                        memeViewHolder.go.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ViewAllMemesActivity.this, MemeDetailsActivity.class);
                                intent.putExtra("MemeId", memeKey);
                                intent.putExtra("Title", model.getMemeName());
                                intent.putExtra("CoverImage", model.getMemeImage());
                                startActivity(intent);
                            }
                        });
                    }
                };

        rvMeme.setAdapter(firebaseRecyclerAdapter);

    }

    public static class MemeViewHolder extends RecyclerView.ViewHolder {
        View cfView;
        TextView tvMemeName, timer, go, tvTimerLabel,tvCount;
        ImageView memeImage;
        Date createdDate, expireDate;

        public MemeViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;
            tvMemeName = cfView.findViewById(R.id.meme_name);
            memeImage = cfView.findViewById(R.id.meme_cover);
            timer = cfView.findViewById(R.id.timer);
            go = cfView.findViewById(R.id.go);
            tvTimerLabel = cfView.findViewById(R.id.timerlabel);
            tvCount=cfView.findViewById(R.id.tv_count);

        }

        public void setMemeName(String memeName) {
            tvMemeName.setText(memeName);
        }

        public void setCoverImage(String memeCoverImageUrl) {
            Picasso.get().load(memeCoverImageUrl).
                    into(memeImage);
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
                    tvTimerLabel.setVisibility(View.INVISIBLE);
                    // accept.setVisibility(View.INVISIBLE);
                    //timerText.setVisibility(View.INVISIBLE);
                    //- designStatus.setVisibility(View.VISIBLE);
                }
            }.start();

        }

        private long getTimeUnit(long timeInMills, TimeUnit timeUnit) {
            return timeUnit.convert(timeInMills, TimeUnit.MILLISECONDS);
        }
        public void setCount(String memeKey){
           DatabaseReference cfMemeCoverRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Memes").child(memeKey).child("ChildMemes");
           cfMemeCoverRef.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if (dataSnapshot.exists()){
                       long count=dataSnapshot.getChildrenCount();
                       tvCount.setText(String.valueOf(count));
                   }else {
                       tvCount.setText("0");
                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
        }
    }


}