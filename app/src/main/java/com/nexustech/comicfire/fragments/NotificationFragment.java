package com.nexustech.comicfire.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.nexustech.comicfire.activities.MemeDetailsActivity;
import com.nexustech.comicfire.activities.ViewAllMemesActivity;
import com.nexustech.comicfire.domains.Info;
import com.nexustech.comicfire.domains.Memes;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class NotificationFragment extends Fragment {

    RecyclerView rvInfo;
    DatabaseReference infoRef;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rowView= inflater.inflate(R.layout.fragment_notification, container, false);

        rvInfo = rowView.findViewById(R.id.rv_info);
        rvInfo.setHasFixedSize(true);
        // rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        // Set the layout manager to your recyclerview
        rvInfo.setLayoutManager(mLayoutManager);

        infoRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Info");
        showAllInfo();
        return rowView;
    }

    private void showAllInfo() {


        Query memeQuery = infoRef.orderByKey();
        FirebaseRecyclerAdapter<Info, InfoViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Info, InfoViewHolder>(
                        Info.class,
                        R.layout.layout_info,
                        InfoViewHolder.class,
                        memeQuery

                ) {
                    @Override
                    protected void populateViewHolder(InfoViewHolder infoViewHolder, Info model, int position) {
                        String memeKey = getRef(position).getKey();

                        infoViewHolder.setTitle(model.getTitle());
                        infoViewHolder.setInfo(model.getInfo());
                        infoViewHolder.setImage(model.getType());
                    }
                };

        rvInfo.setAdapter(firebaseRecyclerAdapter);

    }

    public static class InfoViewHolder extends RecyclerView.ViewHolder {
        View cfView;
        TextView tvTitle, tvInfo;
        ImageView ivInfo;

        public InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            cfView = itemView;

            tvInfo=cfView.findViewById(R.id.tv_info);
            tvTitle=cfView.findViewById(R.id.tv_title);
            ivInfo=cfView.findViewById(R.id.iv_info_image);


        }

        public void setTitle(String title) {
            tvTitle.setText(title);
        }

        public void setInfo(String info) {
            tvInfo.setText(info);
        }

        public void setImage(String type) {
            if ("QUIZ".equals(type)) {
                Picasso.get().load(R.drawable.quiz_cover).into(ivInfo);
            } else if ("MEME".equals(type)) {
                Picasso.get().load(R.drawable.meme_cover).into(ivInfo);
            }else {
                Picasso.get().load(R.drawable.logo_croped).into(ivInfo);
            }
        }
    }
}