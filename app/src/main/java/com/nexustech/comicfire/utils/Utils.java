package com.nexustech.comicfire.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;


import com.google.firebase.auth.FirebaseAuth;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.activities.CreateNewPostActivity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static int CURRENT_NAVIGATION_BAR =R.id.navigation_home ;

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
    public static String createRandomId(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String curDate = dateFormat.format(new Date());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String curTime = timeFormat.format(new Date());
        return curDate.replace("-", "")+curTime.replace("-", "");

    }
    public static String getDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String curDate = dateFormat.format(new Date());
        return curDate;
    }

    public static String gettime(){
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
            if (comment.toLowerCase().contains(offensiveWord)){
                return true;
            }
        }
        return false;
    }
    public static String getCurrentDay(){
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date date = new Date();
        String dayOfTheWeek = sdf.format(date);
        return dayOfTheWeek;
    }

    public static void openAnotherActivity(Context context, Class targetActivity) {
        Intent intent=new Intent(context,targetActivity);
        context.startActivity(intent);
    }

    public static String getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();

    }


    public  static AlertDialog configDialog(Context context, View view){
        AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(context, R.style.AlertDialogTheme).setCancelable(false);

        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            //dialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;

        }
        return dialog;
    }
    public static boolean isCurrentUser(String userId){
     return userId.equals(getCurrentUser());
    }

}
