package com.nexustech.comicfire.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexustech.comicfire.R;
import com.nexustech.comicfire.activities.ViewAllCharsActivity;
import com.nexustech.comicfire.activities.ViewAllMemesActivity;
import com.nexustech.comicfire.activities.ViewAllQuizActivity;
import com.nexustech.comicfire.utils.RoundedCorners;
import com.nexustech.comicfire.utils.Utils;
import com.squareup.picasso.Picasso;

import static com.nexustech.comicfire.utils.PopupLayouts.showFactOnClick;
import static com.nexustech.comicfire.utils.Utils.openAnotherActivity;

public class CqFragment extends Fragment {

    TextView tvMeme, tvQuiz,tvFact;
    ImageView ivMeme, ivQuiz,ivFact,ivHeroShop;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cq, container, false);
        tvMeme = rootView.findViewById(R.id.tv_meme_competition);
        tvQuiz = rootView.findViewById(R.id.tv_quiz_competition);
        tvFact=rootView.findViewById(R.id.tv_fact_of_day);

        ivMeme = rootView.findViewById(R.id.iv_meme_competition);
        ivQuiz = rootView.findViewById(R.id.iv_quiz_competition);
        ivFact=rootView.findViewById(R.id.iv_fact_of_day);
        ivHeroShop=rootView.findViewById(R.id.iv_hero_shop);


        Picasso.get().load(R.drawable.fact_cover).fit().transform(new RoundedCorners(20,20)).into(ivFact);
        Picasso.get().load(R.drawable.point_background_image).fit().transform(new RoundedCorners(20,20)).into(ivHeroShop);
        Picasso.get().load(R.drawable.quiz_cover).fit().transform(new RoundedCorners(20,20)).into(ivQuiz);
        Picasso.get().load(R.drawable.meme_cover).fit().transform(new RoundedCorners(20,20)).into(ivMeme);


        ivHeroShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Utils.goToAllCharActivity(getContext(), ViewAllCharsActivity.class);

            }
        });

        tvFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFactOnClick(getContext());
            }
        });

        tvMeme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openAnotherActivity(getContext(), ViewAllMemesActivity.class);
            }
        });

        tvQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAnotherActivity(getContext(), ViewAllQuizActivity.class);
            }
        });
        ivFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFactOnClick(getContext());
            }
        });

        ivMeme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openAnotherActivity(getContext(), ViewAllMemesActivity.class);
            }
        });

        ivQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAnotherActivity(getContext(), ViewAllQuizActivity.class);
            }
        });


        return rootView;
    }
}