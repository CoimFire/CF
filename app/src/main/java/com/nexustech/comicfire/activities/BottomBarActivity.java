package com.nexustech.comicfire.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nexustech.comicfire.R;
import com.nexustech.comicfire.fragments.CqFragment;
import com.nexustech.comicfire.fragments.FriendsFragment;
import com.nexustech.comicfire.fragments.HomeFragment;
import com.nexustech.comicfire.fragments.NotificationFragment;
import com.nexustech.comicfire.fragments.ProfileFragment;
import com.nexustech.comicfire.fragments.RefreshableFragment;
import com.nexustech.comicfire.utils.Utils;

import java.util.List;

import static com.nexustech.comicfire.utils.Constants.RELEASE_TYPE;
import static com.nexustech.comicfire.utils.PopupLayouts.showFact;

public class BottomBarActivity extends AppCompatActivity {

    private Fragment home;
    private Fragment friends;
    private Fragment profile;
    private Fragment cq;
    private Fragment notifications;

    int FRAGMENT, id;

    String currentDay, popupShowedDay;
    private BottomNavigationView navigation;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_bar);
        Utils.setTopBar(getWindow(), getResources());
        showFact(this);

        profile = new ProfileFragment();
        friends = new FriendsFragment();
        cq = new CqFragment();
        home = new HomeFragment();
        notifications = new NotificationFragment();
        //  myinfo = new MyInfoFragment();
        // notification = new NotificationsFragment();
        navigation = findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }



    @Override
    protected void onResume() {
        super.onResume();
        navigation.getMenu().findItem(Utils.CURRENT_NAVIGATION_BAR).setChecked(true);
        selectFragment();
        showFact(this);

    }

    private void selectFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            transaction.hide(fragment);
        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(Utils.CURRENT_NAVIGATION_BAR + "");
        if (fragment != null) {
            transaction.show(fragment);
        }

        if (R.id.navigation_home == Utils.CURRENT_NAVIGATION_BAR) {
            if (fragment == null) {
                transaction.add(R.id.nav_host_fragment, home, Utils.CURRENT_NAVIGATION_BAR + "").show(home);
            }
        } else if (R.id.navigation_friends == Utils.CURRENT_NAVIGATION_BAR) {
            if (fragment == null) {
                transaction.add(R.id.nav_host_fragment, friends, Utils.CURRENT_NAVIGATION_BAR + "").show(friends);
            }
        } else if (R.id.navigation_profile == Utils.CURRENT_NAVIGATION_BAR) {
            if (fragment == null) {
                transaction.add(R.id.nav_host_fragment, profile, Utils.CURRENT_NAVIGATION_BAR + "").show(profile);
            }
        } else if (R.id.navigation_cq == Utils.CURRENT_NAVIGATION_BAR) {
            if (fragment == null) {
                transaction.add(R.id.nav_host_fragment, cq, Utils.CURRENT_NAVIGATION_BAR + "").show(cq);
            }
        } else if (R.id.navigation_notifications == Utils.CURRENT_NAVIGATION_BAR) {
            if (fragment == null) {
                transaction.add(R.id.nav_host_fragment, notifications, Utils.CURRENT_NAVIGATION_BAR + "").show(notifications);
            }
        }/* else if (R.id.navigation_notification == CrasherAppUtil.CURRENT_NAVIGATION_BAR) {
            if (fragment == null) {
                transaction.add(R.id.content, notification, CrasherAppUtil.CURRENT_NAVIGATION_BAR + "").show(notification);
            }
        }
         */ else {
            if (fragment == null) {
                transaction.add(R.id.nav_host_fragment, home, Utils.CURRENT_NAVIGATION_BAR + "").show(home);
            }
        }

        transaction.setCustomAnimations(android.R.animator.fade_in,
                android.R.animator.fade_out);
        transaction.commitAllowingStateLoss();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        if (item.isChecked()) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(Utils.CURRENT_NAVIGATION_BAR + "");
            if (fragment instanceof RefreshableFragment) {
                ((RefreshableFragment) fragment).refresh();
                return true;
            }
        }
        Utils.CURRENT_NAVIGATION_BAR = item.getItemId();
        item.setChecked(true);
        selectFragment();
        return true;
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        System.exit(1);
    }
}