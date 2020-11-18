package com.nexustech.comicfire.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.telephony.CellSignalStrength;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;

public class Utils {
    public static int CURRENT_NAVIGATION_BAR = R.id.navigation_home;
    public  static boolean isEmpty;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void setTopBar(Window window, Resources resources) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Drawable background = resources.getDrawable(R.drawable.main_theme);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(resources.getColor(android.R.color.transparent));
            window.setNavigationBarColor(resources.getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static String createRandomId() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String curDate = dateFormat.format(new Date());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String curTime = timeFormat.format(new Date());
        return curDate.replace("-", "") + curTime.replace("-", "");

    }

    public static String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String curDate = dateFormat.format(new Date());
        return curDate;
    }

    public static String gettime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String curTime = timeFormat.format(new Date());
        return curTime;
    }


    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public static Boolean filterComment(String comment) {
        String[] offensiveWords = OffensiveWordlist.wordsList;
        for (String offensiveWord : offensiveWords) {
            if (comment.toLowerCase().contains(offensiveWord)) {
                return true;
            }
        }
        return false;
    }

    public static String getCurrentDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date date = new Date();
        String dayOfTheWeek = sdf.format(date);
        return dayOfTheWeek;
    }

    public static void openAnotherActivity(Context context, Class targetActivity) {
        Intent intent = new Intent(context, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static String getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();

    }


    public static AlertDialog configDialog(Context context, View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.AlertDialogTheme).setCancelable(false);

        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            //dialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;

        }
        return dialog;
    }

    public static boolean isCurrentUser(String userId) {
        return userId.equals(getCurrentUser());
    }

    public static void setDialogPosition(AlertDialog dialog) {

        dialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;
    }

    public static void showEmpty(View rootView, DatabaseReference databaseReference) {

        View view = rootView.findViewById(R.id.empty_view);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChildren()) {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void showEmptyInHome(View rootView) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(RELEASE_TYPE)
                .child("User").child(Utils.getCurrentUser());

        View view = rootView.findViewById(R.id.empty_view);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    isEmpty= !dataSnapshot.hasChild("MyPosts");
                    isEmpty= !dataSnapshot.hasChild("Followings");
                    if (isEmpty){
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);

    }
}
