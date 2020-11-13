package com.nexustech.comicfire.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;

import static android.content.Context.MODE_PRIVATE;
import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class PopupLayouts {

    public static void showFact(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("FACT", MODE_PRIVATE);
        boolean isFactShowed = preferences.getBoolean("IS_SHOWED", false);
        String popupShowedDay = preferences.getString("DAY", "");

        if (!isFactShowed && !popupShowedDay.equals(Utils.getCurrentDay())) {


            View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_fact, null);
            AlertDialog dialog = Utils.configDialog(context, rowView);
            TextView tvComment = rowView.findViewById(R.id.tv_fact);
            TextView tvCancel = rowView.findViewById(R.id.tv_close);
            DatabaseReference factRef;
            factRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Fact");
            factRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String edittedComment = dataSnapshot.child("Fact").getValue().toString();
                    tvComment.setText(edittedComment);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("FACT", MODE_PRIVATE).edit();
                    editor.putBoolean("IS_SHOWED", true);
                    editor.putString("DAY", Utils.getCurrentDay());
                    editor.apply();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    public static void showFactOnClick(Context context) {

            View rowView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_fact, null);
            AlertDialog dialog = Utils.configDialog(context, rowView);
            TextView tvComment = rowView.findViewById(R.id.tv_fact);
            TextView tvCancel = rowView.findViewById(R.id.tv_close);
            DatabaseReference factRef;
            factRef = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE).child("Fact");
            factRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String edittedComment = dataSnapshot.child("Fact").getValue().toString();
                    tvComment.setText(edittedComment);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
}
