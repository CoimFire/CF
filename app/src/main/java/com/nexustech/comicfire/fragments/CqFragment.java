package com.nexustech.comicfire.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nexustech.comicfire.R;
import com.nexustech.comicfire.activities.ViewAllMemesActivity;
import com.nexustech.comicfire.utils.Utils;

import static com.nexustech.comicfire.utils.PopupLayouts.showFact;
import static com.nexustech.comicfire.utils.PopupLayouts.showFactOnClick;

public class CqFragment extends Fragment {

    TextView tvMeme, tvQuiz,tvFact;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cq, container, false);
        tvMeme = rootView.findViewById(R.id.tv_meme_competition);
        tvQuiz = rootView.findViewById(R.id.tv_quiz_competition);
        tvFact=rootView.findViewById(R.id.tv_fact_of_day);

        tvFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFactOnClick(getContext());
            }
        });

        tvMeme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utils.openAnotherActivity(getContext(), ViewAllMemesActivity.class);
            }
        });


        return rootView;
    }
}