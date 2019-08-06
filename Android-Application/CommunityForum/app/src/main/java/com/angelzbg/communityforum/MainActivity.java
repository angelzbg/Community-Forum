package com.angelzbg.communityforum;

import android.content.res.Resources;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.angelzbg.communityforum.utils.SoundHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    // Firebase
    private DatabaseReference dbRootReference;

    // Display metrix
    private int width, height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase
        dbRootReference = FirebaseDatabase.getInstance().getReference();

        // Display metrix
        width  = Resources.getSystem().getDisplayMetrics().widthPixels;
        height = Resources.getSystem().getDisplayMetrics().heightPixels;
        if(width > height){
            int temp = width;
            width = height;
            height = temp;
        }

        /* Resizing the Main Menu [ START ] */
        findViewById(R.id.main_CL_MainMenu).getLayoutParams().height = (int) (height / 13.33);
        findViewById(R.id.main_CL_MainMenu).setPadding(height/80, height/80, height/80, height/80);
        findViewById(R.id.main_CL_MainMenu).setElevation(height/80); // другите /160 !
        /* Resizing the Main Menu [  END  ] */

        /* ImageButtons MainMenu onClickListeners [ START ] */
        findViewById(R.id.main_IB_Drawer_MainMenu).setOnClickListener(menuSwitcher);
        findViewById(R.id.main_IB_Saved_MainMenu).setOnClickListener(menuSwitcher);
        findViewById(R.id.main_IB_Home_MainMenu).setOnClickListener(menuSwitcher);
        findViewById(R.id.main_IB_Notifications_MainMenu).setOnClickListener(menuSwitcher);
        findViewById(R.id.main_IB_Search_MainMenu).setOnClickListener(menuSwitcher);
        /* ImageButtons MainMenu onClickListeners [  END  ] */

    } // onCreate()

    /* MenuSwitch [ START ] */
    View.OnClickListener menuSwitcher = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.main_IB_Drawer_MainMenu:
                    // open Drawer
                    break;
                case R.id.main_IB_Saved_MainMenu:
                    findViewById(R.id.main_SV_Posts).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Home_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_Notifications).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Notifications_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_SavedPosts).setVisibility(View.VISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Saved_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    SoundHelper.playSound(getApplicationContext(), SoundHelper.TYPE_MENU_SWITCH);
                    break;
                case R.id.main_IB_Home_MainMenu:
                    findViewById(R.id.main_SV_Notifications).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Notifications_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_SavedPosts).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Saved_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_Posts).setVisibility(View.VISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Home_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    SoundHelper.playSound(getApplicationContext(), SoundHelper.TYPE_MENU_SWITCH);
                    break;
                case R.id.main_IB_Notifications_MainMenu:
                    findViewById(R.id.main_SV_SavedPosts).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Home_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_Posts).setVisibility(View.INVISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Saved_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.darkerGray));

                    findViewById(R.id.main_SV_Notifications).setVisibility(View.VISIBLE);
                    ((ImageButton)findViewById(R.id.main_IB_Notifications_MainMenu)).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    SoundHelper.playSound(getApplicationContext(), SoundHelper.TYPE_MENU_SWITCH);
                    break;
                case R.id.main_IB_Search_MainMenu:
                    // go to search activity
                    break;
            }
        }
    };
    /* MenuSwitch [  END  ] */



} // MainActivity{}